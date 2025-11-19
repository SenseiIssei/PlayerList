package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.IntSupplier;

public class ResizeHandle {
    private final MinecraftClient client = MinecraftClient.getInstance();

    private final IntSupplier xSupplier;
    private final IntSupplier ySupplier;
    private final IntSupplier widthSupplier;
    private final IntSupplier heightSupplier;

    private boolean dragging = false;

    public ResizeHandle(IntSupplier xSupplier, IntSupplier ySupplier, IntSupplier widthSupplier, IntSupplier heightSupplier) {
        this.xSupplier = xSupplier;
        this.ySupplier = ySupplier;
        this.widthSupplier = widthSupplier;
        this.heightSupplier = heightSupplier;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        int x = xSupplier.getAsInt() + widthSupplier.getAsInt() - 10;
        int y = ySupplier.getAsInt() + heightSupplier.getAsInt() - 10;

        context.drawText(client.textRenderer, Text.literal("+"), x, y, 0xFFFFFF, true);
    }

    public boolean mouseClicked(double mx, double my) {
        int x = xSupplier.getAsInt() + widthSupplier.getAsInt() - 10;
        int y = ySupplier.getAsInt() + heightSupplier.getAsInt() - 10;

        if (mx >= x && mx <= x + 10 && my >= y && my <= y + 10) {
            dragging = true;
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
}
