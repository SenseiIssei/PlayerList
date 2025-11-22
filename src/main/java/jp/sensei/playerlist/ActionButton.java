package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class ActionButton implements UIComponent {
    private final MinecraftClient client = MinecraftClient.getInstance();

    private final String label;
    private final Runnable action;

    private final int height = 20;

    public ActionButton(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, int yOffset, int width) {
        int x = 12;
        int cardH = height;
        int cardW = width;
        boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= yOffset && mouseY <= yOffset + cardH;
        int bg = hovered ? 0xFF1E40AF : 0xFF2563EB;
        context.fill(x, yOffset, x + cardW, yOffset + cardH, bg);
        int textW = client.textRenderer.getWidth(label);
        context.drawText(client.textRenderer, Text.literal(label), x + (cardW - textW) / 2, yOffset + (getHeight() - 8) / 2, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int yOffset, int width) {
        int x = 12;
        int cardW = width;
        if (mouseX >= x && mouseX <= x + cardW && mouseY >= yOffset && mouseY <= yOffset + height) {
            action.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY, int width) { return false; }

    @Override
    public void mouseReleased() {}

    @Override
    public int getHeight() { return PlayerListConfig.config.menuRowHeight; }
}
