package jp.sensei.playerlist;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ScrollablePanel {
    private final List<UIComponent> components = new ArrayList<>();
    private int yOffset = 12;

    public void addComponent(UIComponent component) {
        components.add(component);
    }

    public void render(DrawContext context, int mouseX, int mouseY, int availableWidth) {
        int y = yOffset;
        int padding = 8;
        int gap = 12;
        int innerW = Math.max(120, availableWidth - padding * 2);
        int columns = (availableWidth >= 900) ? 3 : (availableWidth >= 520 ? 2 : 1);
        int colW = (innerW - (columns - 1) * gap) / columns;

        
        for (int i = 0; i < components.size(); i += columns) {
            int baseIndex = i;
            for (int col = 0; col < columns; col++) {
                int idx = baseIndex + col;
                if (idx >= components.size()) break;
                UIComponent comp = components.get(idx);
                int colOffset = col * (colW + gap);
                comp.render(context, mouseX - colOffset, mouseY, y, colW);
            }

            int rowH = 0;
            for (int col = 0; col < columns; col++) {
                int idx = i + col;
                if (idx >= components.size()) break;
                rowH = Math.max(rowH, components.get(idx).getHeight());
            }
            y += rowH + 6;
        }
    }

    public boolean mouseClicked(double mx, double my, int availableWidth) {
        int y = yOffset;
        int padding = 8;
        int gap = 12;
        int innerW = Math.max(120, availableWidth - padding * 2);
        int columns = (availableWidth >= 900) ? 3 : (availableWidth >= 520 ? 2 : 1);
        int colW = (innerW - (columns - 1) * gap) / columns;

        for (int i = 0; i < components.size(); i += columns) {
            for (int col = 0; col < columns; col++) {
                int idx = i + col;
                if (idx >= components.size()) break;
                int colOffset = col * (colW + gap);
                if (components.get(idx).mouseClicked(mx - colOffset, my, y, colW)) return true;
            }

            int rowH = 0;
            for (int col = 0; col < columns; col++) {
                int idx = i + col;
                if (idx >= components.size()) break;
                rowH = Math.max(rowH, components.get(idx).getHeight());
            }
            y += rowH + 6;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY, int availableWidth) {
        int padding = 8;
        int gap = 12;
        int innerW = Math.max(120, availableWidth - padding * 2);
        int columns = (availableWidth >= 900) ? 3 : (availableWidth >= 520 ? 2 : 1);
        int colW = (innerW - (columns - 1) * gap) / columns;

        for (int i = 0; i < components.size(); i += columns) {
            for (int col = 0; col < columns; col++) {
                int idx = i + col;
                if (idx >= components.size()) break;
                int colOffset = col * (colW + gap);
                if (components.get(idx).mouseDragged(mouseX - colOffset, mouseY, deltaX, deltaY, colW)) return true;
            }
        }
        return false;
    }

    public boolean mouseReleased() {
        for (UIComponent c : components) {
            c.mouseReleased();
        }
        return false;
    }

    public int getContentHeight(int availableWidth) {
        int padding = 8;
        int gap = 12;
        int innerW = Math.max(120, availableWidth - padding * 2);
        int columns = (availableWidth >= 900) ? 3 : (availableWidth >= 520 ? 2 : 1);
        int total = 0;
        for (int i = 0; i < components.size(); i += columns) {
            int rowH = 0;
            for (int col = 0; col < columns; col++) {
                int idx = i + col;
                if (idx >= components.size()) break;
                rowH = Math.max(rowH, components.get(idx).getHeight());
            }
            total += rowH + 6;
        }
        return total + yOffset;
    }
}
