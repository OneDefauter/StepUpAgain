package com.nottoomanyitems.stepup.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nottoomanyitems.stepup.StepUpConfig;
import com.nottoomanyitems.stepup.StepUpMode;
import com.nottoomanyitems.stepup.StepUpNeoForge;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.lang.reflect.Method;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;

public final class StepChanger {
    private static final double DEFAULT_STEP_HEIGHT = 0.6D;
    private static final double STEP_UP_HEIGHT = 1.25D;
    private static final String STEPUP_KEY_CATEGORY = "key.categories.stepup.controls";

    private KeyMapping toggleModeKey;
    private KeyMapping toggleVanillaCycleKey;
    private int autoJumpState = StepUpConfig.getDefaultState();
    private String serverKey = StepUpConfig.LOCAL_SERVER_KEY;
    private boolean announceState;
    private Boolean lastAutoJumpOptionValue;
    private Object stepUpKeyCategory;
    private boolean warnedAboutChatMethod;

    public void registerKeyMappings(RegisterKeyMappingsEvent event) {
        toggleModeKey = createToggleKeyBinding("key.stepup.toggle", GLFW_KEY_J);
        toggleVanillaCycleKey = createToggleKeyBinding("key.stepup.toggle_vanilla_cycle", GLFW_KEY_K);
        event.register(toggleModeKey);
        event.register(toggleVanillaCycleKey);
    }

    public void handleServerJoin(String serverKey) {
        this.serverKey = serverKey;
        this.autoJumpState = StepUpConfig.getServerState(serverKey);
        this.announceState = true;
        this.lastAutoJumpOptionValue = null;
        StepUpNeoForge.LOGGER.info("Loaded StepUp state {} for {}", autoJumpState, serverKey);
    }

    public void handleDisconnect() {
        this.serverKey = StepUpConfig.LOCAL_SERVER_KEY;
        this.autoJumpState = StepUpConfig.getDefaultState();
        this.announceState = false;
        this.lastAutoJumpOptionValue = null;
    }

    public void handleClientTick(Minecraft client) {
        syncStateWithConfig();
        handleAutoJumpOptionChange(client);

        LocalPlayer player = client.player;
        if (player == null) {
            updateAutoJump(client);
            return;
        }

        Component cycleMessage = null;
        while (toggleVanillaCycleKey != null && toggleVanillaCycleKey.consumeClick()) {
            boolean enabled = !StepUpConfig.isVanillaAutoJumpModeEnabled();
            StepUpConfig.setVanillaAutoJumpModeEnabled(enabled);
            syncStateWithConfig();
            cycleMessage = buildVanillaCycleMessage(enabled);
        }

        while (toggleModeKey != null && toggleModeKey.consumeClick()) {
            setState(StepUpConfig.getNextState(autoJumpState), true);
        }

        updateAutoJump(client);
        updateStepHeight(player);

        if (cycleMessage != null) {
            sendClientMessage(player, cycleMessage);
        }

        if (announceState) {
            sendClientMessage(player, buildStatusMessage());
            announceState = false;
        }
    }

    private void handleAutoJumpOptionChange(Minecraft client) {
        OptionInstance<Boolean> option = client.options.autoJump();
        boolean currentValue = option.get();

        if (lastAutoJumpOptionValue == null) {
            lastAutoJumpOptionValue = currentValue;
            return;
        }

        if (currentValue == lastAutoJumpOptionValue) {
            return;
        }

        setState(getNextAutoJumpControlsState(currentValue), true);
        lastAutoJumpOptionValue = currentValue;
    }

    private int getNextAutoJumpControlsState(boolean currentValue) {
        if (!currentValue) {
            return autoJumpState == StepUpMode.VANILLA_AUTO_JUMP ? StepUpMode.DISABLED : autoJumpState;
        }

        if (autoJumpState == StepUpMode.DISABLED) {
            return StepUpMode.STEP_UP;
        }

        if (autoJumpState == StepUpMode.STEP_UP && StepUpConfig.isVanillaAutoJumpModeEnabled()) {
            return StepUpMode.VANILLA_AUTO_JUMP;
        }

        return StepUpMode.DISABLED;
    }

    public void updateControlsAutoJumpLabel(Minecraft client, Screen screen) {
        OptionsList optionsList = findControlsOptionsList(screen);
        if (optionsList == null) {
            return;
        }

        if (lastAutoJumpOptionValue == null) {
            lastAutoJumpOptionValue = client.options.autoJump().get();
        }

        syncControlsAutoJumpWidget(client, optionsList);
    }

    public void handleControlsAutoJumpClick(
            Minecraft client,
            Screen screen,
            double mouseX,
            double mouseY,
            int button,
            boolean wasHandled
    ) {
        if (button != GLFW_MOUSE_BUTTON_LEFT || !wasHandled) {
            return;
        }

        OptionsList optionsList = findControlsOptionsList(screen);
        if (optionsList == null) {
            return;
        }

        AbstractWidget autoJumpWidget = optionsList.findOption(client.options.autoJump());
        if (autoJumpWidget == null || !autoJumpWidget.isMouseOver(mouseX, mouseY)) {
            return;
        }

        handleAutoJumpOptionChange(client);
        updateAutoJump(client);
        syncControlsAutoJumpWidget(client, optionsList);
    }

    private OptionsList findControlsOptionsList(Screen screen) {
        if (!(screen instanceof ControlsScreen)) {
            return null;
        }

        for (GuiEventListener listener : screen.children()) {
            if (listener instanceof OptionsList optionsList) {
                return optionsList;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private void syncControlsAutoJumpWidget(Minecraft client, OptionsList optionsList) {
        OptionInstance<Boolean> autoJumpOption = client.options.autoJump();
        AbstractWidget autoJumpWidget = optionsList.findOption(autoJumpOption);
        if (autoJumpWidget == null) {
            return;
        }

        if (autoJumpWidget instanceof CycleButton<?> cycleButton) {
            ((CycleButton<Boolean>) cycleButton).setValue(autoJumpOption.get());
        }

        autoJumpWidget.setMessage(buildControlsAutoJumpLabel());
    }

    private void updateAutoJump(Minecraft client) {
        OptionInstance<Boolean> option = client.options.autoJump();
        boolean shouldEnableAutoJump = autoJumpState == StepUpMode.VANILLA_AUTO_JUMP;

        if (option.get() != shouldEnableAutoJump) {
            option.set(shouldEnableAutoJump);
        }

        lastAutoJumpOptionValue = shouldEnableAutoJump;
    }

    private void updateStepHeight(LocalPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.STEP_HEIGHT);
        if (attribute == null) {
            return;
        }

        double targetStepHeight = autoJumpState == StepUpMode.STEP_UP && !player.isShiftKeyDown()
                ? STEP_UP_HEIGHT
                : DEFAULT_STEP_HEIGHT;

        if (attribute.getBaseValue() != targetStepHeight) {
            attribute.setBaseValue(targetStepHeight);
        }
    }

    private KeyMapping createToggleKeyBinding(String translationKey, int defaultKey) {
        try {
            return KeyMapping.class
                    .getConstructor(String.class, InputConstants.Type.class, int.class, String.class)
                    .newInstance(translationKey, InputConstants.Type.KEYSYM, defaultKey, STEPUP_KEY_CATEGORY);
        } catch (NoSuchMethodException exception) {
            return createCategoryKeyBinding(translationKey, defaultKey);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not create StepUp key mapping " + translationKey, exception);
        }
    }

    private KeyMapping createCategoryKeyBinding(String translationKey, int defaultKey) {
        try {
            Object category = getStepUpKeyCategory();
            return KeyMapping.class
                    .getConstructor(String.class, InputConstants.Type.class, int.class, category.getClass())
                    .newInstance(translationKey, InputConstants.Type.KEYSYM, defaultKey, category);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not create StepUp key mapping " + translationKey, exception);
        }
    }

    private Object getStepUpKeyCategory() throws ReflectiveOperationException {
        if (stepUpKeyCategory != null) {
            return stepUpKeyCategory;
        }

        Class<?> categoryClass = Class.forName("net.minecraft.client.KeyMapping$Category");
        Object categoryId = createCategoryIdentifier();
        stepUpKeyCategory = categoryClass
                .getMethod("register", categoryId.getClass())
                .invoke(null, categoryId);
        return stepUpKeyCategory;
    }

    private Object createCategoryIdentifier() throws ReflectiveOperationException {
        for (String className : new String[]{
                "net.minecraft.resources.Identifier",
                "net.minecraft.resources.ResourceLocation"
        }) {
            try {
                Class<?> identifierClass = Class.forName(className);
                return identifierClass
                        .getMethod("fromNamespaceAndPath", String.class, String.class)
                        .invoke(null, StepUpNeoForge.MOD_ID, "controls");
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException("Could not find Identifier or ResourceLocation.");
    }

    private void sendClientMessage(LocalPlayer player, Component message) {
        try {
            Method method = player.getClass().getMethod("displayClientMessage", Component.class, boolean.class);
            method.invoke(player, message, false);
            return;
        } catch (ReflectiveOperationException exception) {
        }

        try {
            Method method = player.getClass().getMethod("sendMessage", Component.class, boolean.class);
            method.invoke(player, message, false);
            return;
        } catch (ReflectiveOperationException exception) {
        }

        if (!warnedAboutChatMethod) {
            warnedAboutChatMethod = true;
            StepUpNeoForge.LOGGER.warn("Could not find a compatible client chat method for StepUp status messages.");
        }
    }

    private Component buildStatusMessage() {
        MutableComponent prefix = Component.empty()
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(StepUpNeoForge.MOD_NAME).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_AQUA));

        return prefix.append(switch (autoJumpState) {
            case StepUpMode.STEP_UP -> Component.translatable("mod.stepup.enabled").withStyle(ChatFormatting.GREEN);
            case StepUpMode.DISABLED -> Component.translatable("mod.stepup.disabled").withStyle(ChatFormatting.RED);
            case StepUpMode.VANILLA_AUTO_JUMP -> Component.empty()
                    .append(Component.translatable("mod.stepup.minecraft"))
                    .append(Component.literal(" "))
                    .append(Component.translatable("mod.stepup.autojump"))
                    .append(Component.literal(" "))
                    .append(Component.translatable("mod.stepup.enabled"))
                    .withStyle(ChatFormatting.GREEN);
            default -> Component.translatable("mod.stepup.disabled").withStyle(ChatFormatting.RED);
        });
    }

    private Component buildControlsAutoJumpLabel() {
        return CommonComponents.optionNameValue(
                Component.translatable("options.autoJump"),
                buildControlsAutoJumpValueLabel()
        );
    }

    private Component buildControlsAutoJumpValueLabel() {
        return switch (StepUpConfig.coerceState(autoJumpState)) {
            case StepUpMode.STEP_UP -> Component.translatable("mod.stepup.mode.stepup");
            case StepUpMode.VANILLA_AUTO_JUMP -> Component.translatable("mod.stepup.mode.vanilla");
            default -> Component.translatable("mod.stepup.mode.off");
        };
    }

    private Component buildVanillaCycleMessage(boolean enabled) {
        MutableComponent prefix = Component.empty()
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(StepUpNeoForge.MOD_NAME).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_AQUA));

        return prefix.append(Component.translatable(
                enabled ? "mod.stepup.config.allowvanillamode.enabled" : "mod.stepup.config.allowvanillamode.disabled"
        ).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private void syncStateWithConfig() {
        int coercedState = StepUpConfig.coerceState(autoJumpState);
        if (coercedState == autoJumpState) {
            return;
        }

        setState(coercedState, true);
    }

    private void setState(int state, boolean announce) {
        autoJumpState = StepUpConfig.coerceState(state);
        StepUpConfig.setServerState(serverKey, autoJumpState);
        announceState = announceState || announce;
    }
}
