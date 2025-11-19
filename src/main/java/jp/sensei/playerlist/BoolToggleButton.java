package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class BoolToggleButton implements UIComponent {
    private final MinecraftClient client = MinecraftClient.getInstance();

    private final String label;
    private boolean value;
    private final Consumer<Boolean> setter;

    private final int width = 200;
    private final int height = 20;

    public BoolToggleButton(String label, boolean value, Consumer<Boolean> setter) {
        this.label = label;
        this.value = value;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, int yOffset) {
        int x = client.getWindow().getScaledWidth() / 2 - width / 2;
        context.fill(x, yOffset, x + width, yOffset + height, 0xAA333333);
        String text = label + ": " + (value ? "ON" : "OFF");
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(text), x + width / 2, yOffset + 6, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int yOffset) {
        int x = client.getWindow().getScaledWidth() / 2 - width / 2;
        if (mouseX >= x && mouseX <= x + width && mouseY >= yOffset && mouseY <= yOffset + height) {
            value = !value;
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
