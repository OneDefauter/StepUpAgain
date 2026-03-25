package com.nottoomanyitems.stepup;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;

import com.mojang.blaze3d.platform.InputConstants;

public final class StepChanger implements ClientTickEvents.EndTick {
    private static final double DEFAULT_STEP_HEIGHT = 0.6D;
    private static final double STEP_UP_HEIGHT = 1.25D;

    private static final int MODE_STEP_UP = 0;
    private static final int MODE_DISABLED = 1;
    private static final int MODE_VANILLA = 2;

    private KeyMapping toggleKey;
    private int autoJumpState = StepUpConfig.getDefaultState();
    private String serverKey = StepUpConfig.LOCAL_SERVER_KEY;
    private boolean announceState;

    public void initialize() {
        toggleKey = KeyMappingHelper.registerKeyMapping(createToggleKeyBinding());
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
    public void onEndTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        while (toggleKey.consumeClick()) {
            autoJumpState = (autoJumpState + 1) % 3;
            StepUpConfig.setServerState(serverKey, autoJumpState);
            announceState = true;
        }

        updateAutoJump(client);
        updateStepHeight(player);

        if (announceState) {
            player.sendSystemMessage(buildStatusMessage());
            announceState = false;
        }
    }

    private void updateAutoJump(Minecraft client) {
        OptionInstance<Boolean> option = client.options.autoJump();
        boolean shouldEnableAutoJump = autoJumpState == MODE_VANILLA;

        if (option.get() != shouldEnableAutoJump) {
            option.set(shouldEnableAutoJump);
        }
    }

    private void updateStepHeight(LocalPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.STEP_HEIGHT);
        if (attribute == null) {
            return;
        }

        double targetStepHeight = autoJumpState == MODE_STEP_UP && !player.isShiftKeyDown()
                ? STEP_UP_HEIGHT
                : DEFAULT_STEP_HEIGHT;

        if (attribute.getBaseValue() != targetStepHeight) {
            attribute.setBaseValue(targetStepHeight);
        }
    }

    private KeyMapping createToggleKeyBinding() {
        return new KeyMapping(
                "key.stepup.toggle",
                InputConstants.Type.KEYSYM,
                GLFW_KEY_J,
                KeyMapping.Category.MISC
        );
    }

    private Component buildStatusMessage() {
        MutableComponent prefix = Component.empty()
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(StepUpClient.MOD_NAME).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_AQUA));

        return prefix.append(switch (autoJumpState) {
            case MODE_STEP_UP -> Component.translatable("mod.stepup.enabled").withStyle(ChatFormatting.GREEN);
            case MODE_DISABLED -> Component.translatable("mod.stepup.disabled").withStyle(ChatFormatting.RED);
            case MODE_VANILLA -> Component.empty()
                    .append(Component.translatable("mod.stepup.minecraft"))
                    .append(Component.literal(" "))
                    .append(Component.translatable("mod.stepup.autojump"))
                    .append(Component.literal(" "))
                    .append(Component.translatable("mod.stepup.enabled"))
                    .withStyle(ChatFormatting.GREEN);
            default -> Component.translatable("mod.stepup.disabled").withStyle(ChatFormatting.RED);
        });
    }
}
