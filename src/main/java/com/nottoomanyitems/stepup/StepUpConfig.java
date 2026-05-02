package com.nottoomanyitems.stepup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StepUpConfig {
    public static final String LOCAL_SERVER_KEY = "singleplayer";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("stepup.json");

    private static ConfigData data = new ConfigData();

    private StepUpConfig() {
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                if (loaded != null) {
                    data = loaded;
                }
            } catch (IOException | JsonParseException exception) {
                StepUp.LOGGER.warn("Could not read StepUp config {}, using defaults.", CONFIG_PATH, exception);
            }
        }

        sanitize();
        save();
    }

    public static int getDefaultState() {
        return coerceState(data.defaultState);
    }

    public static int getServerState(String serverKey) {
        return coerceState(data.servers.getOrDefault(serverKey, getDefaultState()));
    }

    public static boolean isVanillaAutoJumpModeEnabled() {
        return data.allowVanillaAutoJumpMode == null || data.allowVanillaAutoJumpMode;
    }

    public static void setVanillaAutoJumpModeEnabled(boolean enabled) {
        if (isVanillaAutoJumpModeEnabled() == enabled && data.allowVanillaAutoJumpMode != null) {
            return;
        }

        data.allowVanillaAutoJumpMode = enabled;
        save();
    }

    public static int getNextState(int currentState) {
        int modeCount = isVanillaAutoJumpModeEnabled()
                ? StepUpMode.MODE_COUNT_WITH_VANILLA
                : StepUpMode.MODE_COUNT_WITHOUT_VANILLA;

        return (coerceState(currentState) + 1) % modeCount;
    }

    public static void setServerState(String serverKey, int state) {
        if (serverKey == null || serverKey.isBlank()) {
            return;
        }

        int normalizedState = coerceState(state);
        if (normalizedState == getDefaultState()) {
            data.servers.remove(serverKey);
        } else {
            data.servers.put(serverKey, normalizedState);
        }
        save();
    }

    public static int coerceState(int state) {
        int normalizedState = normalizeState(state);
        if (!isVanillaAutoJumpModeEnabled() && normalizedState == StepUpMode.VANILLA_AUTO_JUMP) {
            return StepUpMode.DISABLED;
        }

        return normalizedState;
    }

    private static void sanitize() {
        if (data.allowVanillaAutoJumpMode == null) {
            data.allowVanillaAutoJumpMode = true;
        }
        data.defaultState = coerceState(data.defaultState);
        if (data.servers == null) {
            data.servers = new LinkedHashMap<>();
            return;
        }

        Map<String, Integer> sanitizedServers = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : data.servers.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }
            sanitizedServers.put(key, coerceState(entry.getValue()));
        }
        data.servers = sanitizedServers;
    }

    private static int normalizeState(Integer value) {
        if (value == null) {
            return StepUpMode.STEP_UP;
        }

        return switch (value) {
            case StepUpMode.STEP_UP, StepUpMode.DISABLED, StepUpMode.VANILLA_AUTO_JUMP -> value;
            default -> StepUpMode.STEP_UP;
        };
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException exception) {
            StepUp.LOGGER.warn("Could not save StepUp config {}", CONFIG_PATH, exception);
        }
    }

    private static final class ConfigData {
        private Boolean allowVanillaAutoJumpMode = true;
        private int defaultState = StepUpMode.STEP_UP;
        private Map<String, Integer> servers = new LinkedHashMap<>();
    }
}
