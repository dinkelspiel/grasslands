package dev.keii.grasslands.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BillboardRenderer {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("grasslands",
            "textures/grass_blades.png");
    private static final double[][] QUADRANT_OFFSETS = {
            { 0.25, 0.25 }, // NW
            { 0.75, 0.25 }, // NE
            { 0.25, 0.75 }, // SW
            { 0.75, 0.75 }, // SE
    };

    private static final FastNoiseLite jitterNoise = new FastNoiseLite();
    private static final FastNoiseLite windNoise = new FastNoiseLite();

    private static final float[][][] UV_ROTATIONS = {
            { { 0, 1 }, { 1, 1 }, { 1, 0 }, { 0, 0 } }, // 0deg
            { { 1, 1 }, { 1, 0 }, { 0, 0 }, { 0, 1 } }, // 90deg
            { { 1, 0 }, { 0, 0 }, { 0, 1 }, { 1, 1 } }, // 180deg
            { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 1, 0 } }, // 270deg
    };

    public static void applyConfig() {
        GrasslandsConfig cfg = GrasslandsConfig.get();

        jitterNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        jitterNoise.SetFrequency(cfg.jitterNoiseFrequency);
        jitterNoise.SetSeed(cfg.jitterNoiseSeed);

        windNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        windNoise.SetFrequency(cfg.windNoiseFrequency);
        windNoise.SetSeed(cfg.windNoiseSeed);
    }

    public static void register() {
        applyConfig();
        WorldRenderEvents.AFTER_ENTITIES.register(BillboardRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        GrasslandsConfig cfg = GrasslandsConfig.get();

        if (!cfg.enableBlades)
            return;

        Camera camera = context.gameRenderer().getMainCamera();
        PoseStack poseStack = context.matrices();
        MultiBufferSource consumers = context.consumers();
        ClientLevel level = Minecraft.getInstance().level;

        if (consumers == null || level == null)
            return;

        Vec3 camPos = camera.position();
        BlockPos playerPos = camera.blockPosition();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.entityCutoutNoCull(TEXTURE));
        float half = 0.5f;

        // Wind: diagonal time offset so gusts scroll across the world
        float time = (float) (System.nanoTime() / 1_000_000_000.0) * cfg.windSpeed;
        float windOffsetX = time * 0.7071f; // diagonal ~= cos(45deg)
        float windOffsetZ = time * 0.7071f; // diagonal ~= sin(45deg)

        int scanRadius = cfg.scanRadius;
        int minX = playerPos.getX() - scanRadius;
        int maxX = playerPos.getX() + scanRadius;
        int minZ = playerPos.getZ() - scanRadius;
        int maxZ = playerPos.getZ() + scanRadius;
        int minY = Math.max(level.getMinY(), playerPos.getY() - scanRadius);
        int maxY = Math.min(level.getMaxY(), playerPos.getY() + scanRadius);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    mutablePos.set(x, y, z);

                    if (!level.getBlockState(mutablePos).is(Blocks.GRASS_BLOCK))
                        continue;

                    // Sample light level one block above (where the blades sit)
                    mutablePos.set(x, y + 1, z);

                    // Skip if there's a solid block or snow above the grass
                    net.minecraft.world.level.block.state.BlockState aboveState = level.getBlockState(mutablePos);
                    if (aboveState.isSolidRender() || aboveState.is(Blocks.SNOW))
                        continue;

                    int light = LevelRenderer.getLightColor(level, mutablePos);
                    mutablePos.set(x, y, z);

                    // Get biome grass color at this position
                    int grassColor = BiomeColors.getAverageGrassColor(level, mutablePos);

                    // Place a billboard in the centre of each quadrant with jitter
                    for (int qi = 0; qi < QUADRANT_OFFSETS.length; qi++) {
                        double[] offset = QUADRANT_OFFSETS[qi];
                        // Use simplex noise for jitter (offset by quadrant to decorrelate)
                        float jitterX = x + qi * 1000f;
                        float jitterZ = z + qi * 1000f;
                        double jx = jitterNoise.GetNoise(jitterX, (float) y, jitterZ) * cfg.jitterRange;
                        double jz = jitterNoise.GetNoise(jitterX + 500f, (float) y, jitterZ + 500f) * cfg.jitterRange;
                        double bx = x + offset[0] + jx - camPos.x;
                        double by = y + 1.0 - camPos.y;
                        double bz = z + offset[1] + jz - camPos.z;

                        // Simplex noise-based color variation per blade
                        float bladeWorldX = (float) (x + offset[0] + jx);
                        float bladeWorldZ = (float) (z + offset[1] + jz);
                        int tier = GrassColorUtil.colorTier(bladeWorldX, (float) y, bladeWorldZ);
                        int tinted = GrassColorUtil.applyTier(grassColor, tier);
                        int cr = (tinted >> 16) & 0xFF;
                        int cg = (tinted >> 8) & 0xFF;
                        int cb = tinted & 0xFF;

                        // Height boost based on color tier
                        float heightBoost = tier == 2 ? cfg.heightBoostDarker
                                : tier == 1 ? cfg.heightBoostNormal : 0.0f;
                        float topY = half + heightBoost;

                        // Wind sway
                        float windX = windNoise.GetNoise(bladeWorldX + windOffsetX, (float) y,
                                bladeWorldZ + windOffsetZ) * cfg.windStrength;
                        float windZ = windNoise.GetNoise(bladeWorldX + windOffsetX + 300f, (float) y,
                                bladeWorldZ + windOffsetZ + 300f) * cfg.windStrength;

                        poseStack.pushPose();
                        poseStack.translate(bx, by, bz);
                        poseStack.mulPose(camera.rotation());

                        Matrix4f matrix = poseStack.last().pose();

                        // Pick a random 90deg UV rotation per blade using jitter noise channel 3
                        float rotNoise = jitterNoise.GetNoise(jitterX + 750f, (float) y, jitterZ + 750f);
                        int rot = ((int) ((rotNoise + 1.0f) * 2.0f)) & 3; // 0-3
                        float[][] uv = UV_ROTATIONS[rot];

                        // Bottom vertices stay anchored
                        buffer.addVertex(matrix, -half, -half, 0)
                                .setColor(cr, cg, cb, 255)
                                .setUv(uv[0][0], uv[0][1])
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(light)
                                .setNormal(0, 0, 1);

                        buffer.addVertex(matrix, half, -half, 0)
                                .setColor(cr, cg, cb, 255)
                                .setUv(uv[1][0], uv[1][1])
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(light)
                                .setNormal(0, 0, 1);

                        // Top vertices sway with wind
                        buffer.addVertex(matrix, half + windX, topY, windZ)
                                .setColor(cr, cg, cb, 255)
                                .setUv(uv[2][0], uv[2][1])
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(light)
                                .setNormal(0, 0, 1);

                        buffer.addVertex(matrix, -half + windX, topY, windZ)
                                .setColor(cr, cg, cb, 255)
                                .setUv(uv[3][0], uv[3][1])
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(light)
                                .setNormal(0, 0, 1);

                        poseStack.popPose();
                    }
                }
            }
        }
    }
}
