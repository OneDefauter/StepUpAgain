package com.nottoomanyitems.stepup;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(StepUpNeoForge.MOD_ID)
public final class StepUpNeoForge {
    public static final String MOD_ID = "stepup";
    public static final String MOD_NAME = "StepUp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public StepUpNeoForge() {
    }
}
