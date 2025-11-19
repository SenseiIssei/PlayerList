package jp.sensei.playerlist;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class RadarRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static final int BORDER_SIZE = 2;
    private static final int PLAYER_DOT_SIZE_MIN = 2;

    private record BlockColorKey(BlockState blockState, int biomeTint) {}

    private static final Map<BlockColorKey, Integer> colorCache = new ConcurrentHashMap<>();

    private static int[][] generatedColors = null;
    private static int cachedWidth = 0;
    private static int cachedHeight = 0;
    private static long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 1000;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future<?> generationTask = null;
    private static volatile boolean generationInProgress = false;

    public static void init() {
        generatedColors = null;
        cachedWidth = 0;
        cachedHeight = 0;
        lastUpdateTime = 0;
        colorCache.clear();
    }

    public static void shutdown() {
        executor.shutdownNow();
    }

    public static void render(DrawContext context, boolean showCompass) {
        if (client.world == null || client.player == null || !PlayerListConfig.config.enabled) return;
        if (!PlayerListConfig.config.minimapEnabled) return;

        if (PlayerListConfig.config.radarModeCircular) {
            renderRadar(context, showCompass);
        } else {
            renderMinimap(context, showCompass);
        }
    }

    private static void renderRadar(DrawContext context, boolean showCompass) {
        int x = PlayerListConfig.config.radarX;
        int y = PlayerListConfig.config.radarY;
        int width = PlayerListConfig.config.radarWidth;
        int height = PlayerListConfig.config.radarHeight;
        float zoom = PlayerListConfig.config.radarZoom;

        World world = client.world;
        Vec3d playerPos = client.player.getPos();

        // Background + border
        context.fill(x, y, x + width, y + height, PlayerListConfig.config.radarBackgroundColor);
        int borderColor = 0xFF555555;
        context.fill(x, y, x + width, y + BORDER_SIZE, borderColor);
        context.fill(x, y + height - BORDER_SIZE, x + width, y + height, borderColor);
        context.fill(x, y, x + BORDER_SIZE, y + height, borderColor);
        context.fill(x + width - BORDER_SIZE, y, x + width, y + height, borderColor);

        if (showCompass) {
            drawCompass(context, x, y, width, height);
        }

        List<PlayerListEntry> entries = List.copyOf(client.getNetworkHandler().getPlayerList());
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        for (PlayerListEntry entry : entries) {
            UUID id = entry.getProfile().getId();
            if (id.equals(client.player.getUuid())) continue;
            if (PlayerTracker.isExcluded(entry.getProfile().getName())) continue;

            Entity target = world.getPlayerByUuid(id);
            if (target == null) continue;

            Vec3d targetPos = target.getPos();

            double dx = targetPos.x - playerPos.x;
            double dz = targetPos.z - playerPos.z;
            double dy = targetPos.y - playerPos.y;

            double maxDistance = zoom;
            if (Math.sqrt(dx * dx + dz * dz) > maxDistance) continue;

            float px = (float) (dx / zoom * (width / 2f));
            float py = (float) (dz / zoom * (height / 2f));

            px = clamp(px, -width / 2f + BORDER_SIZE, width / 2f - BORDER_SIZE);
            py = clamp(py, -height / 2f + BORDER_SIZE, height / 2f - BORDER_SIZE);

            int drawX = (int) (centerX + px);
            int drawY = (int) (centerY + py);

            int dotSize = Math.max(PLAYER_DOT_SIZE_MIN, PlayerListConfig.config.playerDotSize);
            context.fill(drawX - dotSize / 2, drawY - dotSize / 2, drawX + dotSize / 2, drawY + dotSize / 2, PlayerListConfig.config.radarDotColor);

            if (Math.abs(dy) >= 2.0) {
                String arrow = dy > 0 ? "↑" : "↓";
                float fontScale = PlayerListConfig.config.minimapFontScale;
                context.getMatrices().push();
                context.getMatrices().translate(drawX + dotSize / 2 + 2, drawY - 7, 0);
                context.getMatrices().scale(fontScale, fontScale, 1f);
                context.drawText(client.textRenderer, Text.literal(arrow), 0, 0, PlayerListConfig.config.radarArrowColor, true);
                context.getMatrices().pop();
            }

            if (PlayerListConfig.config.radarShowNames) {
                String display = entry.getProfile().getName();
                if (PlayerListConfig.config.showDistance) {
                    display += String.format(" (%.1fm)", Math.sqrt(dx * dx + dz * dz));
                }
                float fontScale = PlayerListConfig.config.minimapFontScale;
                context.getMatrices().push();
                context.getMatrices().translate(drawX + dotSize / 2 + 5, drawY - 4, 0);
                context.getMatrices().scale(fontScale, fontScale, 1f);
                context.drawText(client.textRenderer, Text.literal(display), 0, 0, PlayerListConfig.config.hudTextColor, false);
                context.getMatrices().pop();
            }

            if (PlayerListConfig.config.focusTarget != null && PlayerListConfig.config.focusTarget.equals(id)) {
                context.drawText(client.textRenderer, Text.literal("*"), drawX - dotSize - 4, drawY - 4, PlayerListConfig.config.focusHighlightColor, true);
            }

            if (PlayerListConfig.config.stickyTrackerEnabled
                    && PlayerListConfig.config.stickyTarget != null
                    && PlayerListConfig.config.stickyTarget.equals(id)) {
                context.drawText(client.textRenderer, Text.literal("▶"), drawX - dotSize - 4, drawY + 4, PlayerListConfig.config.stickyArrowColor, true);
                int distMeters = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
                context.drawText(client.textRenderer, Text.literal(distMeters + "m"), drawX + dotSize + 6, drawY + 4, PlayerListConfig.config.stickyArrowColor, false);
            }
        }
    }

    private static void renderMinimap(DrawContext context, boolean showCompass) {
        int x = PlayerListConfig.config.radarX;
        int y = PlayerListConfig.config.radarY;
        int width = PlayerListConfig.config.radarWidth;
        int height = PlayerListConfig.config.radarHeight;
        float zoom = PlayerListConfig.config.minimapZoom;
        int pixelStep = PlayerListConfig.config.minimapPixelStep;

        World world = client.world;
        BlockPos playerBlockPos = client.player.getBlockPos();

        // Background + border
        context.fill(x, y, x + width, y + height, PlayerListConfig.config.radarBackgroundColor);
        int borderColor = 0xFF555555;
        context.fill(x, y, x + width, y + BORDER_SIZE, borderColor);
        context.fill(x, y + height - BORDER_SIZE, x + width, y + height, borderColor);
        context.fill(x, y, x + BORDER_SIZE, y + height, borderColor);
        context.fill(x + width - BORDER_SIZE, y, x + width, y + height, borderColor);

        long now = System.currentTimeMillis();
        if (generatedColors == null || cachedWidth != width || cachedHeight != height || now - lastUpdateTime > UPDATE_INTERVAL_MS) {
            if (!generationInProgress) {
                updateMinimapColorsAsync(world, playerBlockPos, width, height, zoom, pixelStep);
                cachedWidth = width;
                cachedHeight = height;
                lastUpdateTime = now;
            }
        }

        if (generatedColors != null) {
            drawCachedMinimap(context, x, y, width, height, generatedColors, pixelStep);
        }

        if (showCompass) {
            drawCompass(context, x, y, width, height);
        }

        drawPlayers(context, x, y, width, height, zoom);
    }

    private static void updateMinimapColorsAsync(World world, BlockPos center, int width, int height, float zoom, int pixelStep) {
        generationInProgress = true;

        generationTask = executor.submit(() -> {
            int pixelsWide = width / pixelStep + 1;
            int pixelsHigh = height / pixelStep + 1;
            int[][] colors = new int[pixelsWide][pixelsHigh];

            for (int px = 0; px < pixelsWide; px++) {
                for (int py = 0; py < pixelsHigh; py++) {
                    int worldX = center.getX() + Math.round((px * pixelStep - width / 2f) * zoom);
                    int worldZ = center.getZ() + Math.round((py * pixelStep - height / 2f) * zoom);
                    int worldY = center.getY();

                    BlockPos topBlockPos = findTopBlockPos(world, worldX, worldZ, worldY);
                    if (topBlockPos == null) {
                        colors[px][py] = 0xFF000000;
                        continue;
                    }

                    BlockState blockState = world.getBlockState(topBlockPos);

                    int biomeTint = getBiomeTint(blockState, world, topBlockPos);
                    BlockColorKey key = new BlockColorKey(blockState, biomeTint);

                    Integer cachedColor = colorCache.get(key);
                    if (cachedColor != null) {
                        colors[px][py] = cachedColor;
                        continue;
                    }

                    Sprite sprite = getTopFaceSprite(blockState);
                    int baseColor;

                    if (sprite != null) {
                        baseColor = sampleSpriteColor(sprite, 0.5f, 0.5f);
                    } else {
                        baseColor = 0xFF888888;
                    }

                    float lightLevel = getLightLevel(world, topBlockPos);

                    int finalColor = applyBiomeTintAndLighting(baseColor, biomeTint, lightLevel);

                    colors[px][py] = finalColor;
                    colorCache.put(key, finalColor);
                }
            }
            generatedColors = colors;
            generationInProgress = false;
        });
    }

    private static int getBiomeTint(BlockState blockState, World world, BlockPos pos) {
        var provider = ColorProviderRegistry.BLOCK.get(blockState.getBlock());
        if (provider != null) {
            int tint = provider.getColor(blockState, world, pos, 0);
            if (tint != 0) return tint;
        }
        return 0xFFFFFF; // no tint (white)
    }

    private static float getLightLevel(World world, BlockPos pos) {
        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);
        float light = Math.min(1.0f, (blockLight + skyLight) / 30f);
        return Math.max(0.5f, light);
    }

    private static int applyBiomeTintAndLighting(int baseColor, int biomeTint, float lightLevel) {
        int r = ((baseColor >> 16) & 0xFF);
        int g = ((baseColor >> 8) & 0xFF);
        int b = (baseColor & 0xFF);

        int tr = ((biomeTint >> 16) & 0xFF);
        int tg = ((biomeTint >> 8) & 0xFF);
        int tb = (biomeTint & 0xFF);

        r = (r * tr) / 255;
        g = (g * tg) / 255;
        b = (b * tb) / 255;

        r = Math.min(255, (int)(r * lightLevel));
        g = Math.min(255, (int)(g * lightLevel));
        b = Math.min(255, (int)(b * lightLevel));

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    private static Sprite getTopFaceSprite(BlockState blockState) {
        BakedModel model = client.getBlockRenderManager().getModels().getModel(blockState);
        if (model == null) return null;

        List<BakedQuad> quads = model.getQuads(blockState, Direction.UP, client.world.random);
        if (quads.isEmpty()) return null;

        return quads.get(0).getSprite();
    }

    private static int sampleSpriteColor(Sprite sprite, float u, float v) {
        if (sprite == null) return 0xFF888888;

        // Sprite does NOT have getWidth/getHeight/getPixels in 1.21 Fabric Yarn
        // So we cannot sample pixels directly here
        // Instead, fallback to average color approximation:
        return approximateSpriteColor(sprite);
    }

    private static int approximateSpriteColor(Sprite sprite) {
        // Cannot sample pixels directly in Fabric 1.21 Yarn
        // As an approximation, return white with sprite's average color (if possible)
        // For now, just return white with full opacity as fallback:
        return 0xFFFFFFFF;
    }

    private static void drawCachedMinimap(DrawContext context, int x, int y, int width, int height, int[][] colors, int pixelStep) {
        if (colors == null) return;
        int pixelsWide = colors.length;
        int pixelsHigh = colors[0].length;

        for (int px = 0; px < pixelsWide; px++) {
            for (int py = 0; py < pixelsHigh; py++) {
                int color = colors[px][py];
                if ((color & 0xFF000000) == 0) continue;
                int drawX = x + px * pixelStep;
                int drawY = y + py * pixelStep;
                context.fill(drawX, drawY, drawX + pixelStep, drawY + pixelStep, color);
            }
        }
    }

    private static BlockPos findTopBlockPos(World world, int x, int z, int centerY) {
        int minY = Math.max(0, centerY - 20);
        int maxY = Math.min(world.getTopY() - 1, centerY + 20);

        for (int y = maxY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!world.isAir(pos)) return pos;
        }
        return null;
    }

    public static void drawCompass(DrawContext context, int x, int y, int width, int height) {
        float playerYaw = client.player.getYaw() % 360f;
        if (playerYaw < 0) playerYaw += 360f;

        String[] dirs = {"N", "E", "S", "W"};
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        float radius = Math.min(width, height) / 2f - 10;

        float fontScale = PlayerListConfig.config.minimapFontScale;
        for (int i = 0; i < dirs.length; i++) {
            float angleDeg = i * 90 - playerYaw;
            double angleRad = Math.toRadians(angleDeg);

            int labelX = (int) (centerX + radius * Math.sin(angleRad));
            int labelY = (int) (centerY - radius * Math.cos(angleRad));

            int textWidth = client.textRenderer.getWidth(dirs[i]);
            int textHeight = 9;

            context.getMatrices().push();
            context.getMatrices().translate(labelX - textWidth / 2f, labelY - textHeight / 2f, 0);
            context.getMatrices().scale(fontScale, fontScale, 1f);
            context.drawText(client.textRenderer, Text.literal(dirs[i]), 0, 0, 0xFFFFFFFF, true);
            context.getMatrices().pop();
        }
    }

    private static void drawPlayers(DrawContext context, int x, int y, int width, int height, float zoom) {
        if (client.world == null || client.player == null) return;

        World world = client.world;
        Vec3d playerPos = client.player.getPos();
        List<PlayerListEntry> entries = List.copyOf(client.getNetworkHandler().getPlayerList());

        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        int selfDotSize = Math.max(PLAYER_DOT_SIZE_MIN, PlayerListConfig.config.selfDotSize);
        int playerDotSize = Math.max(PLAYER_DOT_SIZE_MIN, PlayerListConfig.config.playerDotSize);

        // Draw self dot (green)
        context.fill((int) (centerX - selfDotSize / 2), (int) (centerY - selfDotSize / 2), (int) (centerX + selfDotSize / 2), (int) (centerY + selfDotSize / 2), 0xFF00FF00);

        for (PlayerListEntry entry : entries) {
            UUID id = entry.getProfile().getId();
            if (id.equals(client.player.getUuid())) continue;
            if (PlayerTracker.isExcluded(entry.getProfile().getName())) continue;

            Entity target = world.getPlayerByUuid(id);
            if (target == null) continue;

            Vec3d targetPos = target.getPos();

            double dx = targetPos.x - playerPos.x;
            double dz = targetPos.z - playerPos.z;
            double dy = targetPos.y - playerPos.y;

            double maxDistance = zoom * (width / 2f);
            if (Math.sqrt(dx * dx + dz * dz) > maxDistance) continue;

            float px = (float) (dx / zoom);
            float py = (float) (dz / zoom);

            px = clamp(px, -width / 2f + BORDER_SIZE, width / 2f - BORDER_SIZE);
            py = clamp(py, -height / 2f + BORDER_SIZE, height / 2f - BORDER_SIZE);

            int drawX = (int) (centerX + px);
            int drawY = (int) (centerY + py);

            context.fill(drawX - playerDotSize / 2, drawY - playerDotSize / 2, drawX + playerDotSize / 2, drawY + playerDotSize / 2, PlayerListConfig.config.radarDotColor);

            if (Math.abs(dy) >= 2.0) {
                String arrow = dy > 0 ? "↑" : "↓";
                float fontScale = PlayerListConfig.config.minimapFontScale;
                context.getMatrices().push();
                context.getMatrices().translate(drawX + playerDotSize / 2 + 2, drawY - 7, 0);
                context.getMatrices().scale(fontScale, fontScale, 1f);
                context.drawText(client.textRenderer, Text.literal(arrow), 0, 0, PlayerListConfig.config.radarArrowColor, true);
                context.getMatrices().pop();
            }

            if (PlayerListConfig.config.radarShowNames) {
                String display = entry.getProfile().getName();
                if (PlayerListConfig.config.showDistance) {
                    display += String.format(" (%.1fm)", Math.sqrt(dx * dx + dz * dz));
                }
                float fontScale = PlayerListConfig.config.minimapFontScale;
                context.getMatrices().push();
                context.getMatrices().translate(drawX + playerDotSize / 2 + 5, drawY - 4, 0);
                context.getMatrices().scale(fontScale, fontScale, 1f);
                context.drawText(client.textRenderer, Text.literal(display), 0, 0, PlayerListConfig.config.hudTextColor, false);
                context.getMatrices().pop();
            }

            if (PlayerListConfig.config.focusTarget != null && PlayerListConfig.config.focusTarget.equals(id)) {
                context.drawText(client.textRenderer, Text.literal("*"), drawX - playerDotSize - 4, drawY - 4, PlayerListConfig.config.focusHighlightColor, true);
            }

            if (PlayerListConfig.config.stickyTrackerEnabled
                    && PlayerListConfig.config.stickyTarget != null
                    && PlayerListConfig.config.stickyTarget.equals(id)) {
                context.drawText(client.textRenderer, Text.literal("▶"), drawX - playerDotSize - 4, drawY + 4, PlayerListConfig.config.stickyArrowColor, true);
                int distMeters = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
                context.drawText(client.textRenderer, Text.literal(distMeters + "m"), drawX + playerDotSize + 6, drawY + 4, PlayerListConfig.config.stickyArrowColor, false);
            }
        }
    }

    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}