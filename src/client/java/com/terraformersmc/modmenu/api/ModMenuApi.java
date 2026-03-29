package com.terraformersmc.modmenu.api;

import com.nottoomanyitems.stepup.StepUpTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public interface ModMenuApi {
    static Screen createModsScreen(Screen previous) {
        return previous;
    }

    static Text createModsButtonText() {
        return StepUpTexts.empty();
    }

    default ConfigScreenFactory<?> getModConfigScreenFactory() {
        return null;
    }

    default UpdateChecker getUpdateChecker() {
        return null;
    }

    default Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return Collections.emptyMap();
    }

    default Map<String, UpdateChecker> getProvidedUpdateCheckers() {
        return Collections.emptyMap();
    }

    default void attachModpackBadges(Consumer<String> consumer) {
    }
}
