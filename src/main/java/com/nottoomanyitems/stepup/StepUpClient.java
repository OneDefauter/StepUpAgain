package com.nottoomanyitems.stepup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;

public final class StepUpClient {
    private StepUpClient() {
    }

    public static void initialize() {
        StepUpConfig.load();

        RegisterKeyMappingsEvent.BUS.addListener(StepUpClient::registerKeyMappings);
        TickEvent.ClientTickEvent.Post.BUS.addListener(StepUpClient::handleClientTick);
        ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(StepUpClient::handleServerJoin);
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> StepUp.STEP_CHANGER.handleDisconnect());
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        StepUp.STEP_CHANGER.registerKeyMapping(event);
    }

    private static void handleClientTick(TickEvent.ClientTickEvent.Post event) {
        StepUp.STEP_CHANGER.onEndTick(Minecraft.getInstance());
    }

    private static void handleServerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        StepUp.STEP_CHANGER.handleServerJoin(resolveServerKey(Minecraft.getInstance().getCurrentServer()));
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
