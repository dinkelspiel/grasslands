package dev.keii.grasslands.client;

public final class GrassColorUtil {
    private static final FastNoiseLite COLOR_NOISE = new FastNoiseLite();

    private GrassColorUtil() {
    }

    public static void applyConfig() {
        GrasslandsConfig cfg = GrasslandsConfig.get();
        COLOR_NOISE.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        COLOR_NOISE.SetFrequency(cfg.colorNoiseFrequency);
        COLOR_NOISE.SetSeed(cfg.colorNoiseSeed);
    }

    public static int colorTier(float worldX, float worldY, float worldZ) {
        float n = COLOR_NOISE.GetNoise(worldX, worldY, worldZ);
        double v = (n + 1.0) * 0.5; // remap [-1,1] [0,1]
        if (v < 0.333)
            return 0;
        if (v < 0.666)
            return 1;
        return 2;
    }

    public static int applyTier(int baseColor, int tier) {
        GrasslandsConfig cfg = GrasslandsConfig.get();

        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        switch (tier) {
            case 0:
                r = Math.min(255, (int) (r * cfg.colorLighten));
                g = Math.min(255, (int) (g * cfg.colorLighten));
                b = Math.min(255, (int) (b * cfg.colorLighten));
                break;
            case 2:
                r = (int) (r * cfg.colorDarken);
                g = (int) (g * cfg.colorDarken);
                b = (int) (b * cfg.colorDarken);
                break;
            default:
                break;
        }

        return (r << 16) | (g << 8) | b;
    }
}
