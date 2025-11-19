package jp.sensei.playerlist;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class TabBar {
    public enum Tab {
        GENERAL("General"),
        HUD("HUD Layout & Scaling"),
        RADAR("Radar & Minimap"),
        TRACKED("Tracked Players"),
        THEMES("Themes");

        public final String displayName;
        Tab(String displayName) { this.displayName = displayName; }
    }

    private final PlayerListScreen parent;
    private final int width, height;

    private Tab selectedTab = Tab.GENERAL;

    public TabBar(PlayerListScreen parent, int width, int height) {
        this.parent = parent;
        this.width = width;
        this.height = height;
    }

    public Tab getSelectedTab() { return selectedTab; }

    public void render(DrawContext context, int mouseX, int mouseY) {
        TextRenderer textRenderer = parent.getTextRenderer();

        int tabWidth = Math.min(100, width / Tab.values().length);
        int startX = (width - (tabWidth * Tab.values().length + (Tab.values().length - 1) * 5)) / 2;
        int y = 10;

        for (Tab tab : Tab.values()) {
            int x = startX + tab.ordinal() * (tabWidth + 5);
            int bgColor = (tab == selectedTab) ? 0xAA5555FF : 0xAA333333;
            context.fill(x, y, x + tabWidth, y + height - 5, bgColor);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(tab.displayName), x + tabWidth / 2, y + (height - 5) / 2 - 4, 0xFFFFFF);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        int tabWidth = Math.min(100, width / Tab.values().length);
        int startX = (width - (tabWidth * Tab.values().length + (Tab.values().length - 1) * 5)) / 2;
        int y = 10;

        for (Tab tab : Tab.values()) {
            int x = startX + tab.ordinal() * (tabWidth + 5);
            if (mouseX >= x && mouseX <= x + tabWidth && mouseY >= y && mouseY <= y + height - 5) {
                selectedTab = tab;
                return true;
            }
        }
        return false;
    }
}
