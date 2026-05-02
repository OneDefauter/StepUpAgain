package com.nottoomanyitems.stepup;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(StepUp.MOD_ID)
public final class StepUp {
    public static final String MOD_ID = "stepup";
    public static final String MOD_NAME = "StepUp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final StepChanger STEP_CHANGER = new StepChanger();

    public StepUp(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist.isClient()) {
            StepUpClient.initialize();
            context.getContainer().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(parent -> new StepUpConfigScreen(parent))
            );
        }
    }
}
