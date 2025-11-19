package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

public class SelectTrackedPlayerScreen extends Screen {
    private final Screen parent;
    private final boolean stickyMode;

    public SelectTrackedPlayerScreen(Screen parent, boolean stickyMode) {
        super(Text.literal(stickyMode ? "Sticky-Spieler auswählen" : "Fokus-Spieler auswählen"));
        this.parent = parent;
        this.stickyMode = stickyMode;
    }

    @Override
    protected void init() {
        this.clearChildren();
        List<PlayerListEntry> visible = PlayerTracker.getSortedVisiblePlayers();

        int centerX = width / 2;
        int y = 40;

        for (PlayerListEntry entry : visible) {
            UUID uuid = entry.getProfile().getId();
            String name = entry.getProfile().getName();

            this.addDrawableChild(ButtonWidget.builder(Text.literal(name), btn -> {
                if (stickyMode) {
                    PlayerListConfig.config.stickyTarget = uuid;
                    PlayerListConfig.config.stickyTrackerEnabled = true;
                } else {
                    PlayerListConfig.config.focusTarget = uuid;
                }
                PlayerListConfig.save();
                MinecraftClient.getInstance().setScreen(parent);
            }).dimensions(centerX - 100, y, 200, 20).build());

            y += 24;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Abbrechen"), btn -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX - 100, height - 40, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
