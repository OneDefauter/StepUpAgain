package com.nottoomanyitems.stepup;

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;

public final class StepChanger {
    private static final double DEFAULT_STEP_HEIGHT = 0.6D;
    private static final double STEP_UP_HEIGHT = 1.25D;
    private static final String KEY_CATEGORY = "key.categories.stepup";

    private KeyBinding toggleKey;
    private KeyBinding toggleVanillaModeKey;
    private int autoJumpState = StepUpConfig.getDefaultState();
    private String serverKey = StepUpConfig.LOCAL_SERVER_KEY;
    private boolean announceState;
    private boolean announceVanillaModeSetting;
    private Method legacySetStepHeightMethod;
    private Field legacyStepHeightField;
    private boolean legacySetStepHeightResolved;
    private Method attributeAccessorMethod;
    private Object stepHeightAttribute;
    private Method attributeGetBaseValueMethod;
    private Method attributeSetBaseValueMethod;
    private boolean attributeStepHeightResolved;
    private Method autoJumpAccessorMethod;
    private Method autoJumpGetValueMethod;
    private Method autoJumpSetValueMethod;
    private Field autoJumpField;
    private boolean autoJumpAccessResolved;
    private Method currentStepHeightMethod;
    private boolean currentStepHeightResolved;

    public void initialize() {
        KeyBindingRegistry.INSTANCE.addCategory(KEY_CATEGORY);
        toggleKey = registerKeyBinding("toggle", GLFW_KEY_J);
        toggleVanillaModeKey = registerKeyBinding("toggle_vanilla_mode", GLFW_KEY_K);
    }

    public void handleServerJoin(String serverKey) {
        this.serverKey = serverKey;
        this.autoJumpState = StepUpConfig.getServerState(serverKey);
        this.announceState = true;
        StepUpClient.LOGGER.info("Loaded StepUp state {} for {}", autoJumpState, serverKey);
    }

    public void handleDisconnect() {
        this.serverKey = StepUpConfig.LOCAL_SERVER_KEY;
        this.autoJumpState = StepUpConfig.getDefaultState();
        this.announceState = false;
    }

    public void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        syncStateWithConfig();

        while (toggleVanillaModeKey.wasPressed()) {
            toggleVanillaModeSetting();
        }

        while (toggleKey.wasPressed()) {
            autoJumpState = StepUpConfig.getNextState(autoJumpState);
            StepUpConfig.setServerState(serverKey, autoJumpState);
            announceState = true;
        }

        updateAutoJump(client);
        updateStepHeight(player);

        if (announceVanillaModeSetting) {
            player.sendMessage(buildVanillaModeSettingMessage());
            announceVanillaModeSetting = false;
        }

        if (announceState) {
            player.sendMessage(buildStatusMessage());
            announceState = false;
        }
    }

    private void updateAutoJump(MinecraftClient client) {
        boolean shouldEnableAutoJump = autoJumpState == StepUpMode.VANILLA_AUTO_JUMP;
        if (!applyAutoJumpSetting(client.options, shouldEnableAutoJump)) {
            throw new IllegalStateException("Unsupported auto-jump API for this Minecraft version");
        }
    }

    private void updateStepHeight(ClientPlayerEntity player) {
        double targetStepHeight = autoJumpState == StepUpMode.STEP_UP && !player.isSneaking()
                ? STEP_UP_HEIGHT
                : DEFAULT_STEP_HEIGHT;
        float targetStepHeightFloat = (float) targetStepHeight;
        float currentStepHeight = readCurrentStepHeight(player);

        if (!Float.isNaN(currentStepHeight) && Float.compare(currentStepHeight, targetStepHeightFloat) == 0) {
            return;
        }

        if (tryUpdateLegacyStepHeight(player, targetStepHeightFloat) || tryUpdateAttributeStepHeight(player, targetStepHeight)) {
            return;
        }

        throw new IllegalStateException("Unsupported step height API for this Minecraft version");
    }

    private KeyBinding registerKeyBinding(String keyName, int keyCode) {
        KeyBinding keyBinding = FabricKeyBinding.Builder.create(
                new Identifier(StepUpClient.MOD_ID, keyName),
                InputUtil.Type.KEYSYM,
                keyCode,
                KEY_CATEGORY
        ).build();
        KeyBindingRegistry.INSTANCE.register((FabricKeyBinding) keyBinding);
        return keyBinding;
    }

    private void toggleVanillaModeSetting() {
        StepUpConfig.setVanillaAutoJumpModeEnabled(!StepUpConfig.isVanillaAutoJumpModeEnabled());
        autoJumpState = StepUpConfig.coerceState(autoJumpState);
        StepUpConfig.setServerState(serverKey, autoJumpState);
        announceVanillaModeSetting = true;
        announceState = true;
    }

    private Text buildStatusMessage() {
        Text prefix = StepUpTexts.empty()
                .append(StepUpTexts.literal("[").formatted(Formatting.DARK_AQUA))
                .append(StepUpTexts.literal(StepUpClient.MOD_NAME).formatted(Formatting.YELLOW))
                .append(StepUpTexts.literal("] ").formatted(Formatting.DARK_AQUA));

        Text message;
        switch (autoJumpState) {
            case StepUpMode.STEP_UP:
                message = StepUpTexts.translatable("mod.stepup.enabled").formatted(Formatting.GREEN);
                break;
            case StepUpMode.VANILLA_AUTO_JUMP:
                message = StepUpTexts.empty()
                        .append(StepUpTexts.translatable("mod.stepup.minecraft"))
                        .append(StepUpTexts.literal(" "))
                        .append(StepUpTexts.translatable("mod.stepup.autojump"))
                        .append(StepUpTexts.literal(" "))
                        .append(StepUpTexts.translatable("mod.stepup.enabled"))
                        .formatted(Formatting.GREEN);
                break;
            case StepUpMode.DISABLED:
            default:
                message = StepUpTexts.translatable("mod.stepup.disabled").formatted(Formatting.RED);
                break;
        }

        return prefix.append(message);
    }

    private Text buildVanillaModeSettingMessage() {
        Text prefix = StepUpTexts.empty()
                .append(StepUpTexts.literal("[").formatted(Formatting.DARK_AQUA))
                .append(StepUpTexts.literal(StepUpClient.MOD_NAME).formatted(Formatting.YELLOW))
                .append(StepUpTexts.literal("] ").formatted(Formatting.DARK_AQUA));

        Text message = StepUpConfig.isVanillaAutoJumpModeEnabled()
                ? StepUpTexts.translatable("mod.stepup.config.allowvanillamode.enabled").formatted(Formatting.GREEN)
                : StepUpTexts.translatable("mod.stepup.config.allowvanillamode.disabled").formatted(Formatting.RED);

        return prefix.append(message);
    }

    private void syncStateWithConfig() {
        int coercedState = StepUpConfig.coerceState(autoJumpState);
        if (coercedState == autoJumpState) {
            return;
        }

        autoJumpState = coercedState;
        StepUpConfig.setServerState(serverKey, autoJumpState);
        announceState = true;
    }

    private boolean tryUpdateLegacyStepHeight(ClientPlayerEntity player, float targetStepHeight) {
        if (!legacySetStepHeightResolved) {
            legacySetStepHeightResolved = true;
            legacySetStepHeightMethod = findPublicMethod(player.getClass(), float.class, "setStepHeight", "method_49477");
            legacyStepHeightField = findDeclaredField(player.getClass(), "stepHeight", "field_6013");
        }

        try {
            if (legacySetStepHeightMethod != null) {
                legacySetStepHeightMethod.invoke(player, targetStepHeight);
                return true;
            }

            if (legacyStepHeightField != null) {
                legacyStepHeightField.setFloat(player, targetStepHeight);
                return true;
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to update legacy step height", exception);
        }

        return false;
    }

    private boolean applyAutoJumpSetting(Object options, boolean enabled) {
        resolveAutoJumpAccess(options.getClass());

        try {
            if (autoJumpAccessorMethod != null) {
                Object option = autoJumpAccessorMethod.invoke(options);
                if (option == null) {
                    return false;
                }

                if (autoJumpGetValueMethod == null || autoJumpSetValueMethod == null) {
                    autoJumpGetValueMethod = findPublicMethod(option.getClass(), "getValue", "method_41753", "method_32603");
                    autoJumpSetValueMethod = findCompatibleMethod(option.getClass(), Boolean.valueOf(enabled), "setValue", "method_41748", "method_32605");
                }

                if (autoJumpGetValueMethod != null && autoJumpSetValueMethod != null) {
                    boolean current = ((Boolean) autoJumpGetValueMethod.invoke(option)).booleanValue();
                    if (current != enabled) {
                        autoJumpSetValueMethod.invoke(option, Boolean.valueOf(enabled));
                    }
                    return true;
                }
            }

            if (autoJumpField != null) {
                if (autoJumpField.getBoolean(options) != enabled) {
                    autoJumpField.setBoolean(options, enabled);
                }
                return true;
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to update auto-jump state", exception);
        }

        return false;
    }

    private boolean tryUpdateAttributeStepHeight(ClientPlayerEntity player, double targetStepHeight) {
        resolveAttributeStepHeightAccess(player.getClass());
        if (attributeAccessorMethod == null || stepHeightAttribute == null) {
            return false;
        }

        try {
            Object attributeInstance = attributeAccessorMethod.invoke(player, stepHeightAttribute);
            if (attributeInstance == null) {
                return false;
            }

            if (attributeGetBaseValueMethod == null || attributeSetBaseValueMethod == null) {
                attributeGetBaseValueMethod = findPublicMethod(attributeInstance.getClass(), "getBaseValue", "method_6201");
                attributeSetBaseValueMethod = findPublicMethod(attributeInstance.getClass(), double.class, "setBaseValue", "method_6192");
            }

            if (attributeGetBaseValueMethod == null || attributeSetBaseValueMethod == null) {
                return false;
            }

            double currentStepHeight = ((Number) attributeGetBaseValueMethod.invoke(attributeInstance)).doubleValue();
            if (Double.compare(currentStepHeight, targetStepHeight) != 0) {
                attributeSetBaseValueMethod.invoke(attributeInstance, targetStepHeight);
            }

            return true;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to update attribute-based step height", exception);
        }
    }

    private void resolveAttributeStepHeightAccess(Class<?> playerType) {
        if (attributeStepHeightResolved) {
            return;
        }

        attributeStepHeightResolved = true;

        for (String className : new String[] { "net.minecraft.entity.attribute.EntityAttributes", "net.minecraft.class_5134" }) {
            try {
                Class<?> entityAttributesClass = Class.forName(className);
                Field genericStepHeightField = findPublicField(entityAttributesClass, "GENERIC_STEP_HEIGHT", "field_47761");
                if (genericStepHeightField == null) {
                    continue;
                }

                Object resolvedStepHeightAttribute = genericStepHeightField.get(null);
                Method resolvedAccessorMethod = findCompatibleMethod(
                        playerType,
                        resolvedStepHeightAttribute,
                        "getAttributeInstance",
                        "method_5996",
                        "method_27734"
                );

                if (resolvedAccessorMethod != null) {
                    stepHeightAttribute = resolvedStepHeightAttribute;
                    attributeAccessorMethod = resolvedAccessorMethod;
                    return;
                }
            } catch (ReflectiveOperationException ignored) {
                // Try the next namespace variant.
            }
        }

        stepHeightAttribute = null;
        attributeAccessorMethod = null;
    }

    private Method findPublicMethod(Class<?> ownerType, Class<?> parameterType, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                return ownerType.getMethod(methodName, parameterType);
            } catch (NoSuchMethodException ignored) {
                // Try the next namespace variant.
            }
        }

        return null;
    }

    private Method findPublicMethod(Class<?> ownerType, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                return ownerType.getMethod(methodName);
            } catch (NoSuchMethodException ignored) {
                // Try the next namespace variant.
            }
        }

        return null;
    }

    private Method findCompatibleMethod(Class<?> ownerType, Object argument, String... methodNames) {
        for (Method method : ownerType.getMethods()) {
            if (!matchesName(method.getName(), methodNames) || method.getParameterCount() != 1) {
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            if (argument != null && !parameterType.isInstance(argument) && !parameterType.isAssignableFrom(argument.getClass())) {
                continue;
            }

            return method;
        }

        return null;
    }

    private void resolveAutoJumpAccess(Class<?> optionsType) {
        if (autoJumpAccessResolved) {
            return;
        }

        autoJumpAccessResolved = true;
        autoJumpAccessorMethod = findPublicMethod(optionsType, "getAutoJump", "method_42423");
        autoJumpField = findDeclaredField(optionsType, "autoJump", "field_1848");
    }

    private float readCurrentStepHeight(ClientPlayerEntity player) {
        if (!currentStepHeightResolved) {
            currentStepHeightResolved = true;
            currentStepHeightMethod = findPublicMethod(player.getClass(), "getStepHeight", "method_49476");
        }

        try {
            if (currentStepHeightMethod != null) {
                return ((Number) currentStepHeightMethod.invoke(player)).floatValue();
            }

            if (legacyStepHeightField != null) {
                return legacyStepHeightField.getFloat(player);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read the current step height", exception);
        }

        return Float.NaN;
    }

    private Field findPublicField(Class<?> ownerType, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                return ownerType.getField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // Try the next namespace variant.
            }
        }

        return null;
    }

    private Field findDeclaredField(Class<?> ownerType, String... fieldNames) {
        Class<?> currentType = ownerType;
        while (currentType != null) {
            for (String fieldName : fieldNames) {
                try {
                    Field field = currentType.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                    // Try the next namespace variant.
                }
            }

            currentType = currentType.getSuperclass();
        }

        return null;
    }

    private boolean matchesName(String candidate, String... expectedNames) {
        for (String expectedName : expectedNames) {
            if (candidate.equals(expectedName)) {
                return true;
            }
        }

        return false;
    }
}
