package com.nottoomanyitems.stepup;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Method;

public final class StepUpModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!hasClothConfig()) {
            return screen -> null;
        }

        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        try {
            Class<?> screenFactoryClass = Class.forName("com.nottoomanyitems.stepup.StepUpClothConfigScreen");
            Method createMethod = screenFactoryClass.getMethod("create", Screen.class);
            return (Screen) createMethod.invoke(null, parent);
        } catch (ReflectiveOperationException exception) {
            StepUpClient.LOGGER.warn("Failed to create the StepUp config screen.", exception);
            return null;
        }
    }

    private boolean hasClothConfig() {
        FabricLoader loader = FabricLoader.getInstance();
        return loader.isModLoaded("cloth-config2") || loader.isModLoaded("cloth-config");
    }
}
