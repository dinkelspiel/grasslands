package dev.keii.grasslands.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GrasslandsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("grasslands.json");

    private static GrasslandsConfig INSTANCE;

    public boolean enableBlades = true;
    public int scanRadius = 32;

    public float colorLighten = 1.1f;
    public float colorDarken = 0.9f;
    public float colorNoiseFrequency = 0.25f;
    public int colorNoiseSeed = 12345;

    public double jitterRangePixels = 3.0;
    public float jitterNoiseFrequency = 1.0f;
    public int jitterNoiseSeed = 67890;

    public float windStrengthPixels = 3.0f;
    public float windSpeed = 0.8f;
    public float windNoiseFrequency = 0.15f;
    public int windNoiseSeed = 24680;

    public float heightBoostNormalPixels = 3.0f;

    public float heightBoostDarkerPixels = 6.0f;

    public transient double jitterRange;
    public transient float windStrength;
    public transient float heightBoostNormal;
    public transient float heightBoostDarker;

    public void computeDerived() {
        jitterRange = jitterRangePixels / 16.0;
        windStrength = windStrengthPixels / 16.0f;
        heightBoostNormal = heightBoostNormalPixels / 16.0f;
        heightBoostDarker = heightBoostDarkerPixels / 16.0f;
    }

    public static GrasslandsConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, GrasslandsConfig.class);
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                System.err.println("[Grasslands] Failed to load config, using defaults: " + e.getMessage());
                INSTANCE = new GrasslandsConfig();
            }
        } else {
            INSTANCE = new GrasslandsConfig();
        }
        INSTANCE.computeDerived();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            System.err.println("[Grasslands] Failed to save config: " + e.getMessage());
        }
    }
}
