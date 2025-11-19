package jp.sensei.playerlist;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.function.IntSupplier;

public class DragArea {
    private final MinecraftClient client = MinecraftClient.getInstance();

    private final IntSupplier xSupplier;
    private final IntSupplier ySupplier;
    private final IntSupplier widthSupplier;
    private final IntSupplier heightSupplier;

    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    public DragArea(IntSupplier xSupplier, IntSupplier ySupplier, IntSupplier widthSupplier, IntSupplier heightSupplier) {
        this.xSupplier = xSupplier;
        this.ySupplier = ySupplier;
        this.widthSupplier = widthSupplier;
        this.heightSupplier = heightSupplier;
    }

    public void render(DrawContext context) {
        int x = xSupplier.getAsInt();
        int y = ySupplier.getAsInt();
        int w = widthSupplier.getAsInt();

        context.fill(x, y, x + w, y + 15, 0x80000000);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("Drag here"), x + w / 2, y + 4, 0xCCCCCC);
    }

    public boolean mouseClicked(double mx, double my) {
        int x = xSupplier.getAsInt();
        int y = ySupplier.getAsInt();
        int w = widthSupplier.getAsInt();

        if (mx >= x && mx <= x + w && my >= y && my <= y + 15) {
            dragging = true;
            dragOffsetX = (int) mx - x;
            dragOffsetY = (int) my - y;
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public int getDragOffsetX() {
        return dragOffsetX;
    }

    public int getDragOffsetY() {
        return dragOffsetY;
    }
}
