package com.terraformersmc.modmenu.api;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Consumer;

public interface ModMenuApi {
    static Screen createModsScreen(Screen previous) {
        return previous;
    }

    static Text createModsButtonText() {
        return Text.empty();
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
