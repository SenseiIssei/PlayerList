package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ColorPickerScreen extends Screen {
    private final Screen parent;
    private final String label;
    private final Consumer<Integer> setter;

    private int r, g, b, a;
    private int x, y;
    private boolean draggingR, draggingG, draggingB, draggingA;

    public ColorPickerScreen(Screen parent, String label, int initialColor, Consumer<Integer> setter) {
        super(Text.literal("Color Picker – " + label));
        this.parent = parent;
        this.label = label;
        this.setter = setter;

        a = (initialColor >> 24) & 0xFF;
        r = (initialColor >> 16) & 0xFF;
        g = (initialColor >> 8) & 0xFF;
        b = (initialColor) & 0xFF;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        x = centerX - 100;
        y = 60;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("✔ Apply"), btn -> {
            int color = (a << 24) | (r << 16) | (g << 8) | b;
            setter.accept(color);
            PlayerListConfig.save();
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX - 100, y + 160, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("✖ Cancel"), btn ->
                MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(centerX + 5, y + 160, 95, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Color: " + label), width / 2, 20, 0xFFFFFF);

        drawSlider(context, x, y + 0,  r, "Red",    0xFF0000, draggingR);
        drawSlider(context, x, y + 30, g, "Green",  0x00FF00, draggingG);
        drawSlider(context, x, y + 60, b, "Blue",   0x0000FF, draggingB);
        drawSlider(context, x, y + 90, a, "Alpha",  0xAAAAAA, draggingA);

        // Live Color Preview
        int preview = (a << 24) | (r << 16) | (g << 8) | b;
        context.fill(x + 220, y, x + 270, y + 120, preview);
        context.drawBorder(x + 220, y, 50, 120, 0xFFFFFFFF);

        // Presets
        drawPreset(context, x + 10, y + 130, 0xFF0000FF, mouseX, mouseY); // Blue
        drawPreset(context, x + 40, y + 130, 0xFFFF0000, mouseX, mouseY); // Red
        drawPreset(context, x + 70, y + 130, 0xFF00FF00, mouseX, mouseY); // Green
        drawPreset(context, x + 100, y + 130, 0xFFFFFFFF, mouseX, mouseY); // White
        drawPreset(context, x + 130, y + 130, 0xA0000000, mouseX, mouseY); // Semi-black

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSlider(DrawContext context, int x, int y, int value, String label, int color, boolean dragging) {
        context.drawText(textRenderer, Text.literal(label + ": " + value), x, y - 10, 0xFFFFFF, false);
        context.fill(x, y, x + 200, y + 10, 0xFF222222);
        context.fill(x, y, x + value * 200 / 255, y + 10, color | 0xFF000000);
        context.fill(x + value * 200 / 255 - 1, y - 2, x + value * 200 / 255 + 1, y + 12, 0xFFFFFFFF);
    }

    private void drawPreset(DrawContext context, int px, int py, int color, int mouseX, int mouseY) {
        context.fill(px, py, px + 20, py + 20, color);
        context.drawBorder(px, py, 20, 20, 0xFFFFFFFF);

        if (mouseX >= px && mouseX <= px + 20 && mouseY >= py && mouseY <= py + 20 && MinecraftClient.getInstance().mouse.wasLeftButtonClicked()) {
            a = (color >> 24) & 0xFF;
            r = (color >> 16) & 0xFF;
            g = (color >> 8) & 0xFF;
            b = (color) & 0xFF;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return false;

        if (within(mx, my, x, y, 200, 10)) draggingR = true;
        else if (within(mx, my, x, y + 30, 200, 10)) draggingG = true;
        else if (within(mx, my, x, y + 60, 200, 10)) draggingB = true;
        else if (within(mx, my, x, y + 90, 200, 10)) draggingA = true;

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingR = draggingG = draggingB = draggingA = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingR) r = clamp((int) ((mx - x) * 255 / 200));
        if (draggingG) g = clamp((int) ((mx - x) * 255 / 200));
        if (draggingB) b = clamp((int) ((mx - x) * 255 / 200));
        if (draggingA) a = clamp((int) ((mx - x) * 255 / 200));
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    private boolean within(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
