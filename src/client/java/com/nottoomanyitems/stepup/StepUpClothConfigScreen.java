package com.nottoomanyitems.stepup;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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
            invokeTextOrStringMethod(configBuilderClass, builder, "setTitle", "title.stepup.config");

            Object entryBuilder = configBuilderClass.getMethod("entryBuilder").invoke(builder);
            Object category = invokeTextOrStringMethod(
                    configBuilderClass,
                    builder,
                    "getOrCreateCategory",
                    "mod.stepup.config.category.general"
            );

            Object toggleBuilder = invokeToggleBuilder(
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

    private static Object invokeTextOrStringMethod(Class<?> ownerType, Object target, String methodName, String translationKey)
            throws ReflectiveOperationException {
        for (Method method : ownerType.getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType == Text.class) {
                return method.invoke(target, StepUpTexts.translatable(translationKey));
            }

            if (parameterType == String.class) {
                return method.invoke(target, translationKey);
            }
        }

        throw new NoSuchMethodException(ownerType.getName() + "#" + methodName);
    }

    private static Object invokeToggleBuilder(Object entryBuilder, String translationKey, boolean currentValue)
            throws ReflectiveOperationException {
        for (Method method : entryBuilder.getClass().getMethods()) {
            if (!method.getName().equals("startBooleanToggle") || method.getParameterCount() != 2) {
                continue;
            }

            Class<?> labelType = method.getParameterTypes()[0];
            Class<?> valueType = method.getParameterTypes()[1];
            if (valueType != boolean.class) {
                continue;
            }

            if (labelType == Text.class) {
                return method.invoke(entryBuilder, StepUpTexts.translatable(translationKey), currentValue);
            }

            if (labelType == String.class) {
                return method.invoke(entryBuilder, translationKey, currentValue);
            }
        }

        throw new NoSuchMethodException(entryBuilder.getClass().getName() + "#startBooleanToggle");
    }

    private static void applyTooltip(Object toggleBuilder) throws ReflectiveOperationException {
        Text textTooltip = StepUpTexts.translatable("mod.stepup.config.allowvanillamode.tooltip");
        String stringTooltip = I18n.translate("mod.stepup.config.allowvanillamode.tooltip");
        for (Method method : toggleBuilder.getClass().getMethods()) {
            if (!method.getName().equals("setTooltip") || method.getParameterCount() != 1) {
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType.isArray() && parameterType.getComponentType() == Text.class) {
                method.invoke(toggleBuilder, (Object) new Text[]{textTooltip});
                return;
            }

            if (parameterType == Text.class) {
                method.invoke(toggleBuilder, textTooltip);
                return;
            }

            if (parameterType.isArray() && parameterType.getComponentType() == String.class) {
                method.invoke(toggleBuilder, (Object) new String[]{stringTooltip});
                return;
            }

            if (parameterType == String.class) {
                method.invoke(toggleBuilder, stringTooltip);
                return;
            }
        }
    }
}
