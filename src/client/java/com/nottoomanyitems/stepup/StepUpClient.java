package com.nottoomanyitems.stepup;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ServerInfo;
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
                STEP_CHANGER.handleServerJoin(resolveServerKey(client.getCurrentServerEntry())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> STEP_CHANGER.handleDisconnect());
    }

    private static String resolveServerKey(ServerInfo serverInfo) {
        if (serverInfo == null) {
            return StepUpConfig.LOCAL_SERVER_KEY;
        }

        if (serverInfo.address != null && !serverInfo.address.isBlank()) {
            return serverInfo.address;
        }

        if (serverInfo.name != null && !serverInfo.name.isBlank()) {
            return serverInfo.name;
        }

        return "unknown-server";
    }
}
