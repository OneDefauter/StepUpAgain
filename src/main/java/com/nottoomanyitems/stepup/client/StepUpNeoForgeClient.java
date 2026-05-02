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
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new StepUpConfigScreen(parent));
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
