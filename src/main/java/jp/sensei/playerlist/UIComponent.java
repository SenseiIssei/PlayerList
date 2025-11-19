package jp.sensei.playerlist;

import net.minecraft.client.gui.DrawContext;

public interface UIComponent {
    void render(DrawContext context, int mouseX, int mouseY, int yOffset);
    boolean mouseClicked(double mouseX, double mouseY, int yOffset);
    boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY);
    void mouseReleased();
    int getHeight();
}
