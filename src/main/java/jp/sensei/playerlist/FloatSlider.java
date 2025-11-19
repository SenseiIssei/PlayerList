package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class FloatSlider implements UIComponent {
    private final MinecraftClient client = MinecraftClient.getInstance();

    private final String label;
    private final float min;
    private final float max;
    private float value;
    private final Consumer<Float> setter;

    private final int width = 200;
    private final int height = 20;

    public FloatSlider(String label, float min, float max, float value, Consumer<Float> setter) {
        this.label = label;
        this.min = min;
        this.max = max;
        this.value = value;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, int yOffset) {
        int x = client.getWindow().getScaledWidth() / 2 - width / 2;

        float norm = (value - min) / (max - min);
        int barX = x;
        int sliderX = (int) (barX + norm * width);

        context.fill(x, yOffset, x + width, yOffset + height, 0xAA333333);
        context.drawText(client.textRenderer, Text.literal(label + ": " + String.format("%.2f", value)), x + 4, yOffset + 6, 0xFFFFFF, false);
        context.fill(sliderX - 2, yOffset + 12, sliderX + 2, yOffset + 18, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int yOffset) {
        int x = client.getWindow().getScaledWidth() / 2 - width / 2;
        if (mouseX >= x && mouseX <= x + width && mouseY >= yOffset && mouseY <= yOffset + height) {
            float norm = (float) ((mouseX - x) / width);
            value = min + norm * (max - min);
            setter.accept(value);
            PlayerListConfig.save();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY) { return false; }

    @Override
    public void mouseReleased() {}

    @Override
    public int getHeight() { return height; }
}
