package com.nottoomanyitems.stepup;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class StepUpClient implements ClientModInitializer {
    public static final String MOD_ID = "stepup";
    public static final String MOD_NAME = "StepUp";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final StepChanger STEP_CHANGER = new StepChanger();
    private boolean connected;
    private String currentServerKey = StepUpConfig.LOCAL_SERVER_KEY;

    @Override
    public void onInitializeClient() {
        StepUpConfig.load();
        STEP_CHANGER.initialize();
        ClientTickCallback.EVENT.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        syncConnectionState(client);
        STEP_CHANGER.onEndTick(client);
    }

    private void syncConnectionState(MinecraftClient client) {
        if (client.player == null) {
            if (connected) {
                connected = false;
                currentServerKey = StepUpConfig.LOCAL_SERVER_KEY;
                STEP_CHANGER.handleDisconnect();
            }
            return;
        }

        String resolvedServerKey = resolveServerKey(client.getCurrentServerEntry());
        if (!connected || !resolvedServerKey.equals(currentServerKey)) {
            connected = true;
            currentServerKey = resolvedServerKey;
            STEP_CHANGER.handleServerJoin(resolvedServerKey);
        }
    }

    private static String resolveServerKey(ServerInfo serverInfo) {
        if (serverInfo == null) {
            return StepUpConfig.LOCAL_SERVER_KEY;
        }

        if (!isBlank(serverInfo.address)) {
            return serverInfo.address;
        }

        if (!isBlank(serverInfo.name)) {
            return serverInfo.name;
        }

        return "unknown-server";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
