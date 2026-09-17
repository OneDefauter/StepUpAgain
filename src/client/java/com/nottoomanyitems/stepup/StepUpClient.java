package com.nottoomanyitems.stepup;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.multiplayer.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StepUpClient implements ClientModInitializer {
    public static final String MOD_ID = "stepup";
    public static final String MOD_NAME = "StepUp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final StepChanger STEP_CHANGER = new StepChanger();

    @Override
    public void onInitializeClient() {
        StepUpConfig.load();
        STEP_CHANGER.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(STEP_CHANGER);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                STEP_CHANGER.handleServerJoin(resolveServerKey(client.getCurrentServer())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> STEP_CHANGER.handleDisconnect());
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            STEP_CHANGER.updateControlsAutoJumpLabel(client, screen);
            ScreenMouseEvents.afterMouseClick(screen).register((clickedScreen, mouseButtonEvent, handled) -> {
                    STEP_CHANGER.handleControlsAutoJumpClick(
                            client,
                            clickedScreen,
                            mouseButtonEvent.x(),
                            mouseButtonEvent.y(),
                            mouseButtonEvent.button()
                    );
                    return handled;
            });
        });
    }

    private static String resolveServerKey(ServerData serverInfo) {
        if (serverInfo == null) {
            return StepUpConfig.LOCAL_SERVER_KEY;
        }

        if (serverInfo.ip != null && !serverInfo.ip.isBlank()) {
            return serverInfo.ip;
        }

        if (serverInfo.name != null && !serverInfo.name.isBlank()) {
            return serverInfo.name;
        }

        return "unknown-server";
    }
}
