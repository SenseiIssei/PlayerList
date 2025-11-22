package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Screen to select a tracked player. Supports search and pagination so long lists don't overlap.
 */

public class SelectTrackedPlayerScreen extends Screen {
    private final Screen parent;
    private final boolean stickyMode;
    private List<PlayerListEntry> allEntries = new ArrayList<>();
    private List<PlayerListEntry> filtered = new ArrayList<>();
    private TextFieldWidget searchField;
    private int pageIndex = 0;
    private int pageSize = 8;

    public SelectTrackedPlayerScreen(Screen parent, boolean stickyMode) {
        super(Text.literal(stickyMode ? "Sticky-Spieler auswählen" : "Fokus-Spieler auswählen"));
        this.parent = parent;
        this.stickyMode = stickyMode;
    }

    @Override
    protected void init() {
        this.clearChildren();
        allEntries = PlayerTracker.getSortedVisiblePlayers();
        filtered = new ArrayList<>(allEntries);

        int centerX = width / 2;

        searchField = new TextFieldWidget(this.textRenderer, centerX - 140, 26, 280, 20, Text.literal("search"));
        searchField.setChangedListener(s -> {
            applyFilter(s);
            pageIndex = 0;
            rebuildList();
        });
        this.addSelectableChild(searchField);

        rebuildList();
    }

    private void applyFilter(String q) {
        if (q == null || q.isBlank()) {
            filtered = new ArrayList<>(allEntries);
            return;
        }
        String low = q.toLowerCase();
        filtered = new ArrayList<>();
        for (PlayerListEntry e : allEntries) {
            if (e.getProfile().getName().toLowerCase().contains(low)) filtered.add(e);
        }
    }

    private void rebuildList() {
        this.clearChildren();
        this.addSelectableChild(searchField);

        int centerX = width / 2;
        int topY = 56;
        int buttonH = 20;
        int spacing = 6;

        int available = height - topY - 80;
        pageSize = Math.max(4, available / (buttonH + spacing));

        int start = pageIndex * pageSize;
        int end = Math.min(filtered.size(), start + pageSize);

        int y = topY;
        for (int i = start; i < end; i++) {
            PlayerListEntry entry = filtered.get(i);
            UUID uuid = entry.getProfile().getId();
            String name = entry.getProfile().getName();

            int idx = i - start;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(name), btn -> {
                if (stickyMode) {
                    PlayerListConfig.config.stickyTarget = uuid;
                    PlayerListConfig.config.stickyTrackerEnabled = true;
                } else {
                    PlayerListConfig.config.focusTarget = uuid;
                }
                PlayerListConfig.save();
                MinecraftClient.getInstance().setScreen(parent);
            }).dimensions(centerX - 100, y + idx * (buttonH + spacing), 200, buttonH).build());
        }

        int navY = height - 40;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Prev"), b -> {
            if (pageIndex > 0) pageIndex--; rebuildList();
        }).dimensions(centerX - 150, navY, 80, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Next"), b -> {
            if ((pageIndex + 1) * pageSize < filtered.size()) pageIndex++; rebuildList();
        }).dimensions(centerX + 70, navY, 80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX - 40, navY, 80, 20).build());
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
