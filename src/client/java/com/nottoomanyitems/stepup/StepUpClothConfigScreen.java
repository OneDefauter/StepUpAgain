package com.nottoomanyitems.stepup;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.gui.screen.Screen;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class StepUpClothConfigScreen {
    private StepUpClothConfigScreen() {
    }

    public static Screen create(Screen parent) {
        try {
            Class<?> configBuilderClass = Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
            Object builder = configBuilderClass.getMethod("create").invoke(null);
            configBuilderClass.getMethod("setParentScreen", Screen.class).invoke(builder, parent);
            configBuilderClass.getMethod("setTitle", String.class)
                    .invoke(builder, "title.stepup.config");

            Object entryBuilder = configBuilderClass.getMethod("entryBuilder").invoke(builder);
            Object category = configBuilderClass.getMethod("getOrCreateCategory", String.class)
                    .invoke(builder, "mod.stepup.config.category.general");

            Object toggleBuilder = entryBuilder.getClass()
                    .getMethod("startBooleanToggle", String.class, boolean.class)
                    .invoke(
                            entryBuilder,
                            "mod.stepup.config.allowvanillamode",
                            StepUpConfig.isVanillaAutoJumpModeEnabled()
                    );

            invokeIfPresent(toggleBuilder, "setDefaultValue", boolean.class, true);
            applyTooltip(toggleBuilder);
            toggleBuilder.getClass()
                    .getMethod("setSaveConsumer", Consumer.class)
                    .invoke(toggleBuilder, (Consumer<Boolean>) StepUpConfig::setVanillaAutoJumpModeEnabled);

            Object entry = toggleBuilder.getClass().getMethod("build").invoke(toggleBuilder);
            Class<?> abstractEntryClass = Class.forName("me.shedaniel.clothconfig2.api.AbstractConfigListEntry");
            category.getClass().getMethod("addEntry", abstractEntryClass).invoke(category, entry);

            return (Screen) configBuilderClass.getMethod("build").invoke(builder);
        } catch (ReflectiveOperationException exception) {
            StepUpClient.LOGGER.warn("Failed to create the StepUp Cloth Config screen.", exception);
            return null;
        }
    }

    private static void invokeIfPresent(Object target, String methodName, Class<?> parameterType, Object argument) {
        try {
            target.getClass().getMethod(methodName, parameterType).invoke(target, argument);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void applyTooltip(Object toggleBuilder) throws ReflectiveOperationException {
        String tooltip = I18n.translate("mod.stepup.config.allowvanillamode.tooltip");
        for (Method method : toggleBuilder.getClass().getMethods()) {
            if (!method.getName().equals("setTooltip") || method.getParameterCount() != 1) {
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType.isArray() && parameterType.getComponentType() == String.class) {
                method.invoke(toggleBuilder, (Object) new String[]{tooltip});
                return;
            }
        }
    }
}
