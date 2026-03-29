package io.github.prospector.modmenu.api;

import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public interface ModMenuApi {
    String getModId();

    default Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return screen -> null;
    }
}
