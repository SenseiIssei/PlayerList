package jp.sensei.playerlist;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ScrollablePanel {
    private final List<UIComponent> components = new ArrayList<>();
    private int yOffset = 40;

    public void addComponent(UIComponent component) {
        components.add(component);
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        int y = yOffset;
        for (UIComponent c : components) {
            c.render(context, mouseX, mouseY, y);
            y += c.getHeight() + 6;
        }
    }

    public boolean mouseClicked(double mx, double my) {
        int y = yOffset;
        for (UIComponent c : components) {
            if (c.mouseClicked(mx, my, y)) return true;
            y += c.getHeight() + 6;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY) {
        for (UIComponent c : components) {
            if (c.mouseDragged(mouseX, mouseY, deltaX, deltaY)) return true;
        }
        return false;
    }

    public boolean mouseReleased() {
        for (UIComponent c : components) {
            c.mouseReleased();
        }
        return false;
    }

    public int getContentHeight() {
        int total = 0;
        for (UIComponent c : components) {
            total += c.getHeight() + 6;
        }
        return total + yOffset;
    }
}
