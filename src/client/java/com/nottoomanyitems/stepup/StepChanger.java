package com.nottoomanyitems.stepup;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;

public final class StepChanger implements ClientTickEvents.EndTick {
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

    public void initialize() {
        toggleKey = KeyBindingHelper.registerKeyBinding(createKeyBinding("key.stepup.toggle", GLFW_KEY_J));
        toggleVanillaModeKey = KeyBindingHelper.registerKeyBinding(createKeyBinding("key.stepup.toggle_vanilla_mode", GLFW_KEY_K));
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

    @Override
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
            player.sendMessage(buildVanillaModeSettingMessage(), false);
            announceVanillaModeSetting = false;
        }

        if (announceState) {
            player.sendMessage(buildStatusMessage(), false);
            announceState = false;
        }
    }

    private void updateAutoJump(MinecraftClient client) {
        SimpleOption<Boolean> option = client.options.getAutoJump();
        boolean shouldEnableAutoJump = autoJumpState == StepUpMode.VANILLA_AUTO_JUMP;

        if (option.getValue() != shouldEnableAutoJump) {
            option.setValue(shouldEnableAutoJump);
        }
    }

    private void updateStepHeight(ClientPlayerEntity player) {
        double targetStepHeight = autoJumpState == StepUpMode.STEP_UP && !player.isSneaking()
                ? STEP_UP_HEIGHT
                : DEFAULT_STEP_HEIGHT;
        float targetStepHeightFloat = (float) targetStepHeight;

        if (player.getStepHeight() == targetStepHeightFloat) {
            return;
        }

        if (tryUpdateLegacyStepHeight(player, targetStepHeightFloat) || tryUpdateAttributeStepHeight(player, targetStepHeight)) {
            return;
        }

        throw new IllegalStateException("Unsupported step height API for this Minecraft version");
    }

    private KeyBinding createKeyBinding(String translationKey, int keyCode) {
        try {
            for (Constructor<?> constructor : KeyBinding.class.getConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length != 4
                        || parameterTypes[0] != String.class
                        || parameterTypes[1] != InputUtil.Type.class
                        || parameterTypes[2] != int.class) {
                    continue;
                }

                return (KeyBinding) constructor.newInstance(
                        translationKey,
                        InputUtil.Type.KEYSYM,
                        keyCode,
                        resolveCategoryArgument(parameterTypes[3])
                );
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create StepUp key binding", exception);
        }

        throw new IllegalStateException("Unsupported KeyBinding constructor for this Minecraft version");
    }

    private Object resolveCategoryArgument(Class<?> categoryType) throws ReflectiveOperationException {
        if (categoryType == String.class) {
            return KEY_CATEGORY;
        }

        for (Field field : categoryType.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != categoryType) {
                continue;
            }

            Object candidate = field.get(null);
            if (candidate != null && candidate.toString().toLowerCase().contains("misc")) {
                return candidate;
            }
        }

        for (Method method : categoryType.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    || method.getReturnType() != categoryType
                    || method.getParameterCount() != 1) {
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType == String.class) {
                return method.invoke(null, KEY_CATEGORY);
            }

            if (parameterType == Identifier.class) {
                return method.invoke(null, new Identifier(StepUpClient.MOD_ID, "stepup"));
            }
        }

        throw new IllegalStateException("Unsupported KeyBinding category type: " + categoryType.getName());
    }

    private Text buildStatusMessage() {
        MutableText prefix = Text.empty()
                .append(Text.literal("[").formatted(Formatting.DARK_AQUA))
                .append(Text.literal(StepUpClient.MOD_NAME).formatted(Formatting.YELLOW))
                .append(Text.literal("] ").formatted(Formatting.DARK_AQUA));

        return prefix.append(switch (autoJumpState) {
            case StepUpMode.STEP_UP -> Text.translatable("mod.stepup.enabled").formatted(Formatting.GREEN);
            case StepUpMode.DISABLED -> Text.translatable("mod.stepup.disabled").formatted(Formatting.RED);
            case StepUpMode.VANILLA_AUTO_JUMP -> Text.empty()
                    .append(Text.translatable("mod.stepup.minecraft"))
                    .append(Text.literal(" "))
                    .append(Text.translatable("mod.stepup.autojump"))
                    .append(Text.literal(" "))
                    .append(Text.translatable("mod.stepup.enabled"))
                    .formatted(Formatting.GREEN);
            default -> Text.translatable("mod.stepup.disabled").formatted(Formatting.RED);
        });
    }

    private Text buildVanillaModeSettingMessage() {
        MutableText prefix = Text.empty()
                .append(Text.literal("[").formatted(Formatting.DARK_AQUA))
                .append(Text.literal(StepUpClient.MOD_NAME).formatted(Formatting.YELLOW))
                .append(Text.literal("] ").formatted(Formatting.DARK_AQUA));

        Text message = StepUpConfig.isVanillaAutoJumpModeEnabled()
                ? Text.translatable("mod.stepup.config.allowvanillamode.enabled").formatted(Formatting.GREEN)
                : Text.translatable("mod.stepup.config.allowvanillamode.disabled").formatted(Formatting.RED);

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

    private void toggleVanillaModeSetting() {
        StepUpConfig.setVanillaAutoJumpModeEnabled(!StepUpConfig.isVanillaAutoJumpModeEnabled());
        autoJumpState = StepUpConfig.coerceState(autoJumpState);
        StepUpConfig.setServerState(serverKey, autoJumpState);
        announceVanillaModeSetting = true;
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
