package com.nottoomanyitems.stepup;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;

public final class StepChanger implements ClientTickEvents.EndTick {
    private static final double DEFAULT_STEP_HEIGHT = 0.6D;
    private static final double STEP_UP_HEIGHT = 1.25D;

    private static final int MODE_STEP_UP = 0;
    private static final int MODE_DISABLED = 1;
    private static final int MODE_VANILLA = 2;

    private KeyBinding toggleKey;
    private int autoJumpState = StepUpConfig.getDefaultState();
    private String serverKey = StepUpConfig.LOCAL_SERVER_KEY;
    private boolean announceState;

    public void initialize() {
        toggleKey = KeyBindingHelper.registerKeyBinding(createToggleKeyBinding());
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

        while (toggleKey.wasPressed()) {
            autoJumpState = (autoJumpState + 1) % 3;
            StepUpConfig.setServerState(serverKey, autoJumpState);
            announceState = true;
        }

        updateAutoJump(client);
        updateStepHeight(player);

        if (announceState) {
            player.sendMessage(buildStatusMessage(), false);
            announceState = false;
        }
    }

    private void updateAutoJump(MinecraftClient client) {
        SimpleOption<Boolean> option = client.options.getAutoJump();
        boolean shouldEnableAutoJump = autoJumpState == MODE_VANILLA;

        if (option.getValue() != shouldEnableAutoJump) {
            option.setValue(shouldEnableAutoJump);
        }
    }

    private void updateStepHeight(ClientPlayerEntity player) {
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attribute == null) {
            return;
        }

        double targetStepHeight = autoJumpState == MODE_STEP_UP && !player.isSneaking()
                ? STEP_UP_HEIGHT
                : DEFAULT_STEP_HEIGHT;

        if (attribute.getBaseValue() != targetStepHeight) {
            attribute.setBaseValue(targetStepHeight);
        }
    }

    private KeyBinding createToggleKeyBinding() {
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
                        "key.stepup.toggle",
                        InputUtil.Type.KEYSYM,
                        GLFW_KEY_J,
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
            return "key.categories.misc";
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
                return method.invoke(null, "key.categories.misc");
            }

            if (parameterType == Identifier.class) {
                return method.invoke(null, Identifier.ofVanilla("misc"));
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
            case MODE_STEP_UP -> Text.translatable("mod.stepup.enabled").formatted(Formatting.GREEN);
            case MODE_DISABLED -> Text.translatable("mod.stepup.disabled").formatted(Formatting.RED);
            case MODE_VANILLA -> Text.empty()
                    .append(Text.translatable("mod.stepup.minecraft"))
                    .append(Text.literal(" "))
                    .append(Text.translatable("mod.stepup.autojump"))
                    .append(Text.literal(" "))
                    .append(Text.translatable("mod.stepup.enabled"))
                    .formatted(Formatting.GREEN);
            default -> Text.translatable("mod.stepup.disabled").formatted(Formatting.RED);
        });
    }
}
