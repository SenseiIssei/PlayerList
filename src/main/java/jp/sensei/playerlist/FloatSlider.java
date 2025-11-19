package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FloatSlider implements UIComponent {
    private final MinecraftClient client = MinecraftClient.getInstance();

    private final String label;
    private final float min;
    private final float max;
    private final Supplier<Float> getter;
    private final Consumer<Float> setter;

    private final int height = 20;
    private boolean dragging = false;
    private float animPos = 0f;

    public FloatSlider(String label, float min, float max, Supplier<Float> getter, Consumer<Float> setter) {
        this.label = label;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, int yOffset, int width) {
        int x = 12;
        int cardH = height;
        int cardW = width;
        boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= yOffset && mouseY <= yOffset + cardH;
        int bg = hovered ? 0xFF131826 : 0xFF0F1720;
        context.fill(x, yOffset, x + cardW, yOffset + cardH, bg);
        context.drawText(client.textRenderer, Text.literal(label), x + 8, yOffset + 2, 0xFFCBD5E1, false);
        float value = getter.get();
        String valStr = String.format("%.2f", value);
        int valW = client.textRenderer.getWidth(valStr);
        context.drawText(client.textRenderer, Text.literal(valStr), x + cardW - valW - 12, yOffset + 2, 0xFFCBD5E1, false);
        
        int trackX = x + 8;
        int trackY = yOffset + cardH - 10;
        int trackW = cardW - 32 - valW;
        context.fill(trackX, trackY, trackX + trackW, trackY + 2, 0xFF232E3A);
        
        float targetPos = (value - min) / (max - min);
        animPos += (targetPos - animPos) * 0.22f;
        int knobW = 10;
        int knobX = trackX + (int) (animPos * (trackW - knobW));
        int knobY = trackY - 5;
        context.fill(knobX, knobY, knobX + knobW, knobY + knobW, hovered ? 0xFFFFFFFF : 0xFFE6EEF6);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int yOffset, int width) {
        int x = 12;
        int cardW = width;
        if (mouseX >= x && mouseX <= x + cardW && mouseY >= yOffset && mouseY <= yOffset + height) {
            int valW = client.textRenderer.getWidth(String.format("%.2f", getter.get()));
            int trackX = x + 8;
            int trackW = cardW - 32 - valW;
            float rel = (float) ((mouseX - trackX) / (double) (trackW));
            rel = Math.max(0, Math.min(1, rel));
            float newVal = min + rel * (max - min);
            setter.accept(newVal);
            PlayerListConfig.save();
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY, int width) {
        if (!dragging) return false;
        int x = 12 + 8;
        int cardW = width;
        int valW = client.textRenderer.getWidth(String.format("%.2f", getter.get()));
        int trackW = cardW - 32 - valW;
        double rel = (mouseX - x) / (double) trackW;
        rel = Math.max(0, Math.min(1, rel));
        float newVal = min + (float) rel * (max - min);
        setter.accept(newVal);
        PlayerListConfig.save();
        return true;
    }

    @Override
    public void mouseReleased() { dragging = false; }

    @Override
    public int getHeight() { return height; }
}
