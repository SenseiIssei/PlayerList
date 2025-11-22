package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoolToggleButton implements UIComponent {
    private final MinecraftClient client = MinecraftClient.getInstance();

    private final String label;
    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;

    private final int height = 22;
    private float anim = 0f;

    public BoolToggleButton(String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, int yOffset, int width) {
        int x = 8;
        int cardH = getHeight();
        int cardW = width;
        int innerPad = PlayerListConfig.config.menuInnerPadding;
        boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= yOffset && mouseY <= yOffset + cardH;
        int bg = PlayerListConfig.config.menuBackgroundColor;
        context.fill(x, yOffset, x + cardW, yOffset + cardH, bg);
        context.drawText(client.textRenderer, Text.literal(label), x + innerPad, yOffset + (cardH - 8) / 2, PlayerListConfig.config.menuTextColor, false);
        
        
        
        
        int pillW = Math.max(36, innerPad * 4);
        int pillX = x + cardW - pillW - innerPad - PlayerListConfig.config.menuSafeEdge;
        int pillY = yOffset + (cardH - (cardH - 6)) / 2;
        int pillH = cardH - 6;
        boolean value = getter.get();
        
        float target = value ? 1f : 0f;
        anim += (target - anim) * 0.25f;
        int bgBlend = value ? 0xFF10B981 : 0xFF374151;
        context.fill(pillX, pillY, pillX + pillW, pillY + pillH, bgBlend);
        int circleSize = Math.max(6, pillH - 6);
        int minX = pillX + 4;
        int maxX = pillX + pillW - 4 - circleSize;
        int circleX = minX + Math.round((maxX - minX) * anim);
        int circleY = pillY + 3;
        context.fill(circleX, circleY, circleX + circleSize, circleY + circleSize, 0xFFFFFFFF);
        
        String status = value ? "ON" : "OFF";
        context.drawText(client.textRenderer, Text.literal(status), pillX + 6, pillY + (pillH - 8) / 2, 0xFF04111A, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int yOffset, int width) {
        int x = 12;
        int cardW = width;
        if (mouseX >= x && mouseX <= x + cardW && mouseY >= yOffset && mouseY <= yOffset + getHeight()) {
            boolean newVal = !getter.get();
            setter.accept(newVal);
            PlayerListConfig.save();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY, int width) { return false; }

    @Override
    public void mouseReleased() {}

    @Override
    public int getHeight() { return height; }
}
