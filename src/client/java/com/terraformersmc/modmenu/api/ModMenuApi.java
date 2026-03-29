package com.terraformersmc.modmenu.api;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.function.Consumer;

public interface ModMenuApi {
    static Screen createModsScreen(Screen previous) {
        return previous;
    }

    static Component createModsButtonText() {
        return Component.empty();
    }

    default ConfigScreenFactory<?> getModConfigScreenFactory() {
        return null;
    }

    default UpdateChecker getUpdateChecker() {
        return null;
    }

    default Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return Map.of();
    }

    default Map<String, UpdateChecker> getProvidedUpdateCheckers() {
        return Map.of();
    }

    default void attachModpackBadges(Consumer<String> consumer) {
    }
}
