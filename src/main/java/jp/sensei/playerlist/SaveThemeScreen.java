package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SaveThemeScreen extends Screen {
    private final Screen parent;
    private String input = "";
    private boolean saving = false;

    public SaveThemeScreen(Screen parent) {
        super(Text.literal("Save Custom Theme"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xCC000000);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Save Current Theme Preset"), width / 2, 40, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Preset Name:"), width / 2, 70, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("> " + input + (saving ? "..." : "")), width / 2, 90, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Press ENTER to save, ESC to cancel"), width / 2, 120, 0x888888);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER && !input.trim().isEmpty()) {
            saving = true;
            CustomTheme theme = CustomTheme.from(PlayerListConfig.config);
            theme.name = input.trim();
            ThemeStorage.saveTheme(theme);
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr == '\b') {
            if (!input.isEmpty()) input = input.substring(0, input.length() - 1);
        } else if (chr >= 32 && chr != 127) {
            input += chr;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
