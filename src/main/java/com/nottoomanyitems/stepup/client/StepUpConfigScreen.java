package com.nottoomanyitems.stepup.client;

import com.nottoomanyitems.stepup.StepUpConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class StepUpConfigScreen extends Screen {
    private final Screen parent;
    private Button toggleButton;

    public StepUpConfigScreen(Screen parent) {
        super(Component.translatable("title.stepup.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = this.height / 4;

        StringWidget titleWidget = new StringWidget(this.title, this.font);
        titleWidget.setPosition(centerX - 100, 20);
        titleWidget.setMaxWidth(200);
        addRenderableOnly(titleWidget);

        MultiLineTextWidget tooltipWidget = new MultiLineTextWidget(
                Component.translatable("mod.stepup.config.allowvanillamode.tooltip.short"),
                this.font
        );
        tooltipWidget.setCentered(true);
        tooltipWidget.setMaxWidth(240);
        tooltipWidget.setPosition(centerX - 120, 42);
        addRenderableOnly(tooltipWidget);

        MultiLineTextWidget hintWidget = new MultiLineTextWidget(
                Component.translatable("mod.stepup.config.togglehint"),
                this.font
        );
        hintWidget.setCentered(true);
        hintWidget.setMaxWidth(240);
        hintWidget.setPosition(centerX - 120, 62);
        addRenderableOnly(hintWidget);

        toggleButton = addRenderableWidget(Button.builder(buildToggleLabel(), button -> {
            StepUpConfig.setVanillaAutoJumpModeEnabled(!StepUpConfig.isVanillaAutoJumpModeEnabled());
            toggleButton.setMessage(buildToggleLabel());
        }).bounds(centerX - 100, top + 40, 200, 20).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(centerX - 100, top + 120, 200, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private Component buildToggleLabel() {
        return Component.translatable("mod.stepup.config.allowvanillamode")
                .append(Component.literal(": "))
                .append(Component.translatable(
                        StepUpConfig.isVanillaAutoJumpModeEnabled()
                                ? "mod.stepup.enabled"
                                : "mod.stepup.disabled"
                ));
    }
}
