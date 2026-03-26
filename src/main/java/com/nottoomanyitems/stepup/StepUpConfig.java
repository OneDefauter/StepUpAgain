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
        return normalizeState(data.defaultState);
    }

    public static int getServerState(String serverKey) {
        return normalizeState(data.servers.getOrDefault(serverKey, getDefaultState()));
    }

    public static void setServerState(String serverKey, int state) {
        if (serverKey == null || serverKey.isBlank()) {
            return;
        }

        int normalizedState = normalizeState(state);
        if (normalizedState == getDefaultState()) {
            data.servers.remove(serverKey);
        } else {
            data.servers.put(serverKey, normalizedState);
        }
        save();
    }

    private static void sanitize() {
        data.defaultState = normalizeState(data.defaultState);
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
            sanitizedServers.put(key, normalizeState(entry.getValue()));
        }
        data.servers = sanitizedServers;
    }

    private static int normalizeState(Integer value) {
        if (value == null) {
            return 0;
        }

        return switch (value) {
            case 0, 1, 2 -> value;
            default -> 0;
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
        private int defaultState = 0;
        private Map<String, Integer> servers = new LinkedHashMap<>();
    }
}
