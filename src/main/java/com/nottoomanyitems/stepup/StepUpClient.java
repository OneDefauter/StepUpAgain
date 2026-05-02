package com.nottoomanyitems.stepup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
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
        ScreenEvent.Init.Post.BUS.addListener(StepUpClient::handleScreenInit);
        ScreenEvent.MouseButtonPressed.Post.BUS.addListener(StepUpClient::handleScreenMouseClick);
        ScreenEvent.Render.Pre.BUS.addListener(StepUpClient::handleScreenRender);
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

    private static void handleScreenInit(ScreenEvent.Init.Post event) {
        StepUp.STEP_CHANGER.updateControlsAutoJumpLabel(Minecraft.getInstance(), event.getScreen());
    }

    private static void handleScreenMouseClick(ScreenEvent.MouseButtonPressed.Post event) {
        StepUp.STEP_CHANGER.handleControlsAutoJumpClick(
                Minecraft.getInstance(),
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getButton(),
                event.wasHandled()
        );
    }

    private static void handleScreenRender(ScreenEvent.Render.Pre event) {
        StepUp.STEP_CHANGER.updateControlsAutoJumpLabel(Minecraft.getInstance(), event.getScreen());
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
