package jp.sensei.playerlist;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class TabBar {
    public enum Tab {
        GENERAL("General"),
        HUD("HUD & Scaling"),
        RADAR("Radar & Minimap"),
        TRACKED("Tracked Players");

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

        int stripY = 8;
        int stripH = height - 6;
        context.fill(10, stripY, width - 10, stripY + stripH, 0x88000000);

        int tabWidth = Math.min(110, (width - 40) / Tab.values().length);
        int gap = 8;
        int startX = (width - (tabWidth * Tab.values().length + (Tab.values().length - 1) * gap)) / 2;
        int y = stripY + 4;

        int accent = 0xFF3B82F6;
        int inactiveBg = 0xAA222222;
        int inactiveText = 0xFFDDDDDD;

        for (Tab tab : Tab.values()) {
            int x = startX + tab.ordinal() * (tabWidth + gap);
            if (tab == selectedTab) {
                context.fill(x - 2, y - 2, x + tabWidth + 2, y + (height - 12) + 2, accent);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(tab.displayName), x + tabWidth / 2, y + (height - 12) / 2 - 4, 0xFFFFFFFF);
            } else {
                context.fill(x, y, x + tabWidth, y + (height - 12), inactiveBg);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(tab.displayName), x + tabWidth / 2, y + (height - 12) / 2 - 4, inactiveText);
            }
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
