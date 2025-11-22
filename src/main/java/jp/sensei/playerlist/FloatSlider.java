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
        int cardH = getHeight();
        int cardW = width;
        int innerPad = 10;
        boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= yOffset && mouseY <= yOffset + cardH;
        int bg = hovered ? 0xFF131826 : 0xFF0F1720;
        context.fill(x, yOffset, x + cardW, yOffset + cardH, bg);
        int valW = client.textRenderer.getWidth(String.format("%.2f", getter.get()));
        int labelMax = Math.max(24, cardW - innerPad * 2 - valW - 28);
        String drawLabel = label;
        if (client.textRenderer.getWidth(drawLabel) > labelMax) {
            while (drawLabel.length() > 0 && client.textRenderer.getWidth(drawLabel + "...") > labelMax) {
                drawLabel = drawLabel.substring(0, drawLabel.length() - 1);
            }
            drawLabel = drawLabel + "...";
        }
        context.drawText(client.textRenderer, Text.literal(drawLabel), x + innerPad, yOffset + 6, 0xFFCBD5E1, false);
        float value = getter.get();
        String valStr = String.format("%.2f", value);
        int valWActual = client.textRenderer.getWidth(valStr);
        int valX = x + cardW - innerPad - valWActual - 4;
        context.drawText(client.textRenderer, Text.literal(valStr), valX, yOffset + 6, 0xFFCBD5E1, false);

        int trackX = x + innerPad;
        int trackY = yOffset + cardH - 8;
        int trackW = Math.max(40, cardW - innerPad * 2 - valWActual - 12);
        context.fill(trackX, trackY, trackX + trackW, trackY + 2, 0xFF232E3A);
        
        float targetPos = (value - min) / (max - min);
        animPos += (targetPos - animPos) * 0.22f;
        int knobW = 10;
        int knobX = trackX + (int) (animPos * Math.max(0, trackW - knobW));
        int knobY = trackY - 3;
        context.fill(knobX, knobY, knobX + knobW, knobY + knobW, hovered ? 0xFFFFFFFF : 0xFFE6EEF6);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int yOffset, int width) {
        int x = 12;
        int innerPad = 10;
        int cardW = width;
        if (mouseX >= x && mouseX <= x + cardW && mouseY >= yOffset && mouseY <= yOffset + getHeight()) {
            int valW = client.textRenderer.getWidth(String.format("%.2f", getter.get()));
            int trackX = x + innerPad;
            int trackW = Math.max(40, cardW - innerPad * 2 - valW - 8);
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
        int innerPad = 10;
        int x = 12 + innerPad;
        int cardW = width;
        int valW = client.textRenderer.getWidth(String.format("%.2f", getter.get()));
        int trackW = Math.max(40, cardW - innerPad * 2 - valW - 8);
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
    public int getHeight() { return PlayerListConfig.config.menuRowHeight; }
}
