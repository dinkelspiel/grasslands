package dev.keii.grasslands.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.block.Blocks;

public class GrasslandsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        GrasslandsConfig.load();

        GrassColorUtil.applyConfig();
        BillboardRenderer.register();

        ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> {
            if (level == null || pos == null) {
                return 0x7CBD6B; // fallback vanilla green
            }

            int base = BiomeColors.getAverageGrassColor(level, pos);
            int tier = GrassColorUtil.colorTier(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f);
            return GrassColorUtil.applyTier(base, tier);
        }, Blocks.GRASS_BLOCK);
    }
}
