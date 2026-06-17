package com.nottoomanyitems.stepup.client;

import com.nottoomanyitems.stepup.StepUpConfig;
import com.nottoomanyitems.stepup.StepUpNeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = StepUpNeoForge.MOD_ID, dist = Dist.CLIENT)
public final class StepUpNeoForgeClient {
    private final StepChanger stepChanger = new StepChanger();

    public StepUpNeoForgeClient(IEventBus modEventBus, ModContainer modContainer) {
        StepUpConfig.load();

        modEventBus.addListener(this::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onClientPlayerLoggingIn);
        NeoForge.EVENT_BUS.addListener(this::onClientPlayerLoggingOut);
        NeoForge.EVENT_BUS.addListener(this::onScreenInit);
        NeoForge.EVENT_BUS.addListener(this::onScreenMouseClick);
        NeoForge.EVENT_BUS.addListener(this::onScreenRender);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        stepChanger.registerKeyMappings(event);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        stepChanger.handleClientTick(Minecraft.getInstance());
    }

    private void onClientPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        stepChanger.handleServerJoin(resolveServerKey(Minecraft.getInstance().getCurrentServer()));
    }

    private void onClientPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        stepChanger.handleDisconnect();
    }

    private void onScreenInit(ScreenEvent.Init.Post event) {
        stepChanger.updateControlsAutoJumpLabel(Minecraft.getInstance(), event.getScreen());
    }

    private void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Post event) {
        stepChanger.handleControlsAutoJumpClick(
                Minecraft.getInstance(),
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getButton(),
                true
        );
    }

    private void onScreenRender(ScreenEvent.Render.Pre event) {
        stepChanger.updateControlsAutoJumpLabel(Minecraft.getInstance(), event.getScreen());
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
