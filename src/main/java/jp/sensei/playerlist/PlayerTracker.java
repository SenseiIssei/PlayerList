package jp.sensei.playerlist;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class PlayerTracker {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Set<UUID> previousPlayers = new HashSet<>();
    private static final List<PlayerListEntry> currentPlayers = new ArrayList<>();
    private static final Map<UUID, String> playerNameCache = new HashMap<>();
    private static float alpha = 0f;
    private static float currentHudHeight = 0f;

    private static final Map<Rect, UUID> nameClickZones = new HashMap<>();

    private static final KeyBinding toggleRadarKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.playerlist.toggle_radar", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.playerlist")
    );

    private static class Rect {
        int x, y, w, h;
        Rect(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
        boolean contains(int px, int py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    public static void init() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderHud(context));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleRadarKey.wasPressed()) {
                PlayerListConfig.config.minimapEnabled = !PlayerListConfig.config.minimapEnabled;
                PlayerListConfig.save();
            }

            boolean leftClick = client.mouse.wasLeftButtonClicked();
            boolean rightClick = client.mouse.wasRightButtonClicked();

            if (leftClick || rightClick) {
                int mx = (int) client.mouse.getX();
                int my = (int) client.mouse.getY();

                for (Map.Entry<Rect, UUID> entry : nameClickZones.entrySet()) {
                    Rect rect = entry.getKey();
                    UUID clickedUuid = entry.getValue();

                    if (rect.contains(mx, my)) {
                        if (leftClick) {
                            PlayerListConfig.config.focusTarget = clickedUuid;
                            showNotification("Focus player set", Formatting.YELLOW);
                        } else {
                            PlayerListConfig.config.stickyTarget = clickedUuid;
                            PlayerListConfig.config.stickyTrackerEnabled = true;
                            showNotification("Sticky tracker enabled", Formatting.AQUA);
                        }
                        PlayerListConfig.save();
                        break;
                    }
                }
            }
            nameClickZones.clear();
        });

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (client.getNetworkHandler() == null) continue;

                    List<PlayerListEntry> entries = new ArrayList<>(client.getNetworkHandler().getPlayerList());
                    Set<UUID> currentUUIDs = new HashSet<>();
                    List<PlayerListEntry> newPlayers = new ArrayList<>();

                    for (PlayerListEntry entry : entries) {
                        String name = entry.getProfile().getName();
                        if (isExcluded(name)) continue;

                        UUID id = entry.getProfile().getId();
                        currentUUIDs.add(id);
                        newPlayers.add(entry);

                        if (!previousPlayers.contains(id)) {
                            showNotification("[+1] " + name, Formatting.GREEN);
                            playerNameCache.put(id, name);
                        }
                    }

                    for (UUID old : previousPlayers) {
                        if (!currentUUIDs.contains(old)) {
                            String oldName = playerNameCache.getOrDefault(old, "Unknown");
                            showNotification("[-1] " + oldName, Formatting.RED);
                            playerNameCache.remove(old);
                        }
                    }

                    previousPlayers.clear();
                    previousPlayers.addAll(currentUUIDs);

                    synchronized (currentPlayers) {
                        currentPlayers.clear();
                        currentPlayers.addAll(newPlayers);
                        if (PlayerListConfig.config.sortMode == PlayerListConfig.Config.SortMode.NAME) {
                            currentPlayers.sort(Comparator.comparing(e -> e.getProfile().getName()));
                        } else if (PlayerListConfig.config.sortMode == PlayerListConfig.Config.SortMode.DISTANCE
                                && client.player != null && client.world != null) {
                            currentPlayers.sort(Comparator.comparingDouble(e -> {
                                Entity target = client.world.getPlayerByUuid(e.getProfile().getId());
                                return (target != null) ? client.player.squaredDistanceTo(target) : Double.MAX_VALUE;
                            }));
                        }
                    }
                } catch (InterruptedException ignored) {}
            }
        }, "PlayerTracker-Thread").start();
    }

    public static List<PlayerListEntry> getSortedVisiblePlayers() {
        synchronized (currentPlayers) {
            return currentPlayers.stream()
                    .filter(entry -> !isExcluded(entry.getProfile().getName()))
                    .filter(entry -> !PlayerListConfig.config.hideSelf || !entry.getProfile().getId().equals(client.player.getUuid()))
                    .sorted(Comparator.comparing(e -> e.getProfile().getName()))
                    .toList();
        }
    }

    public static boolean isExcluded(String name) {
        String trimmed = name.trim();
        if (trimmed.contains("slot_")) {
            try {
                int start = trimmed.indexOf("slot_") + 5;
                int end = start;
                while (end < trimmed.length() && Character.isDigit(trimmed.charAt(end))) {
                    end++;
                }
                int num = Integer.parseInt(trimmed.substring(start, end));
                if (num >= 11 && num <= 90) return true;
            } catch (Exception ignored) {}
        }
        return PlayerListConfig.config.excludedNames.contains(trimmed);
    }

    private static void renderHud(DrawContext context) {
        if (!PlayerListConfig.config.enabled || client.getDebugHud().shouldShowDebugHud()) return;

        int x = PlayerListConfig.config.hudX;
        int y = PlayerListConfig.config.hudY;
        int width = Math.max(PlayerListConfig.config.windowWidth, 60);
        int height = PlayerListConfig.config.windowHeight;

        float baseWidth = PlayerListConfig.config.baseWidth;
        float baseHeight = PlayerListConfig.config.baseHeight;
        float scaleW = width / baseWidth;
        float scaleH = height / baseHeight;
        float scale = Math.max(PlayerListConfig.config.minScale, Math.min(scaleW, scaleH));

        float fontScale = scale * PlayerListConfig.config.fontScaleMultiplier;
        int iconSize = (int)(10 * scale * PlayerListConfig.config.iconScale);
        int lineHeight = Math.max((int)(14 * scale * PlayerListConfig.config.lineHeightScale), 10);
        int titleHeight = (int)(16 * scale);
        int footerHeight = (int)(14 * scale);
        int padding = (int)(6 * scale * PlayerListConfig.config.paddingScale);
        int sectionSpacing = (int)(6 * scale * PlayerListConfig.config.sectionSpacingScale);
        int textYOffset = (int)(2 * scale);

        boolean showDistance = PlayerListConfig.config.showDistance;
        int columnGap = (int)(4 * scale);
        int distanceColumnWidth = showDistance ? (int)(width * 0.3f) : 0;
        int playerColumnWidth = width - distanceColumnWidth - (showDistance ? columnGap : 0);

        synchronized (currentPlayers) {
            List<PlayerListEntry> visible = currentPlayers.stream()
                    .filter(entry -> !isExcluded(entry.getProfile().getName()))
                    .filter(entry -> !PlayerListConfig.config.hideSelf || !entry.getProfile().getId().equals(client.player.getUuid()))
                    .toList();

            int maxLines = (height - titleHeight - footerHeight - padding * 2 - sectionSpacing * 2) / lineHeight;
            int displayCount = Math.min(PlayerListConfig.config.maxVisiblePlayers, Math.min(maxLines, visible.size()));
            int footerSpacer = footerHeight + padding;
            int targetHeight = titleHeight + sectionSpacing + displayCount * lineHeight + sectionSpacing + footerSpacer;

            if (currentHudHeight < targetHeight) {
                currentHudHeight += (targetHeight - currentHudHeight) * 0.2f;
            } else {
                currentHudHeight = targetHeight;
            }

            alpha = Math.min(1f, alpha + 0.05f);
            int backgroundColor = (int)(alpha * 160) << 24;
            context.fill(x, y, x + width, y + (int) currentHudHeight, backgroundColor);

            
            Text title = Text.literal("PlayerList (" + visible.size() + ")").formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
            int titleX = x + (int)((width - client.textRenderer.getWidth(title) * fontScale) / 2);
            drawScaledText(context, title, titleX, y + padding, fontScale, 0xFFFFFF);

            
            int headersY = y + padding + titleHeight + sectionSpacing;
            drawScaledText(context, Text.literal("Player:"), x + 4, headersY, fontScale, 0xCCCCCC);
            if (showDistance) {
                drawScaledText(context, Text.literal("Distance:"), x + width - distanceColumnWidth - 4, headersY, fontScale, 0xCCCCCC);
            }

            int listStartY = headersY + lineHeight;
            int textColor = 0xCCCCCC;

            for (int i = 0; i < displayCount && i < visible.size(); i++) {
                PlayerListEntry entry = visible.get(i);
                String name = entry.getProfile().getName();
                UUID uuid = entry.getProfile().getId();
                int drawY = listStartY + i * lineHeight;

                
                if (i % 2 == 1) {
                    int rowAlpha = 30;
                    context.fill(x + 1, drawY - 1, x + width - 1, drawY + lineHeight - 1, (rowAlpha << 24));
                }

                Identifier skin = client.getSkinProvider().getSkinTextures(entry.getProfile()).texture();
                context.drawTexture(skin, x + 3, drawY, 8, 8, iconSize, iconSize, 64, 64);

                
                String arrow = "";
                if (client.player != null) {
                    Entity target = client.world.getPlayerByUuid(uuid);
                    if (target != null) {
                        double dy = target.getY() - client.player.getY();
                        if (dy > 2.0) arrow = " ↑";
                        else if (dy < -2.0) arrow = " ↓";
                    }
                }

                int maxNameWidth = playerColumnWidth - iconSize - 10;
                String trimmed = name;
                if (client.textRenderer.getWidth(name) * fontScale > maxNameWidth) {
                    trimmed = client.textRenderer.trimToWidth(name, (int)(maxNameWidth / fontScale) - 3) + "...";
                }

                int textX = x + iconSize + 6;
                drawScaledText(context, Text.literal(trimmed + arrow), textX, drawY + textYOffset, fontScale, textColor);
                nameClickZones.put(new Rect(textX, drawY + textYOffset, 100, lineHeight), uuid);

                
                if (showDistance && client.player != null) {
                    Entity target = client.world.getPlayerByUuid(uuid);
                    if (target != null) {
                        double dist = Math.sqrt(client.player.squaredDistanceTo(target));
                        String distStr = String.format("%.1f m", dist);
                        int distTextWidth = (int)(client.textRenderer.getWidth(distStr) * fontScale);
                        int distX = x + width - 4 - distTextWidth;
                        drawScaledText(context, Text.literal(distStr), distX, drawY + textYOffset, fontScale, 0xCCCCCC);
                    }
                }
            }

            
            // Text footer = Text.literal("Made by SenseiIssei").formatted(Formatting.DARK_PURPLE);
            // float footerScale = Math.min(1f, scale * 0.9f * PlayerListConfig.config.fontScaleMultiplier);
            // int footerX = x + (int)(width - client.textRenderer.getWidth(footer) * footerScale) - 4;
            // int footerY = listStartY + displayCount * lineHeight + sectionSpacing;
            // drawScaledText(context, footer, footerX, footerY, footerScale, 0xFFFFFF);
        }

        
        if (PlayerListConfig.config.stickyTrackerEnabled && PlayerListConfig.config.stickyTarget != null && client.player != null && client.world != null) {
            Entity target = client.world.getPlayerByUuid(PlayerListConfig.config.stickyTarget);
            if (target != null) {
                Vec3d from = client.player.getPos();
                Vec3d to = target.getPos();
                double dx = to.x - from.x;
                double dz = to.z - from.z;
                double dy = to.y - from.y;

                double distance = Math.sqrt(dx * dx + dz * dz);
                double angle = Math.atan2(dz, dx);
                double playerYawRad = Math.toRadians(client.player.getYaw());
                double relativeAngle = angle - playerYawRad;

                int centerX = client.getWindow().getScaledWidth() / 2;
                int centerY = client.getWindow().getScaledHeight() / 2;
                int radius = 40;

                int arrowX = centerX + (int)(Math.cos(relativeAngle) * radius);
                int arrowY = centerY + (int)(Math.sin(relativeAngle) * radius);

                String arrowSymbol = "➤";
                String distStr = String.format("%.1f m", distance);
                context.drawText(client.textRenderer, Text.literal(arrowSymbol), arrowX - 3, arrowY - 5, PlayerListConfig.config.radarArrowColor, true);
                context.drawText(client.textRenderer, Text.literal(distStr), arrowX - distStr.length() * 3, arrowY + 5, 0xFFFFFF, false);
            }
        }
    }

    private static void drawScaledText(DrawContext context, Text text, int x, int y, float scale, int color) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1f);
        context.drawText(client.textRenderer, text, 0, 0, color, false);
        context.getMatrices().pop();
    }

    private static void showNotification(String msg, Formatting color) {
        if (msg.contains("[-1]") || msg.contains("[+1]")) {
            String name = msg.replace("[-1]", "").replace("[+1]", "").trim();
            if (isExcluded(name)) return;
        }

        client.execute(() -> {
            if (client.player != null) {
                Text message = Text.literal(msg).formatted(color);
                if (PlayerListConfig.config.bigNotifications) {
                    message = Text.literal("==> " + msg + " <==").formatted(color, Formatting.BOLD);
                }
                client.inGameHud.setOverlayMessage(message, false);
            }
        });
    }

    private static String getNameFromUUID(UUID uuid) {
        if (client.getNetworkHandler() == null) return "Unknown";
        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            if (entry.getProfile().getId().equals(uuid)) {
                return entry.getProfile().getName();
            }
        }
        return playerNameCache.getOrDefault(uuid, "Unknown");
    }
}
