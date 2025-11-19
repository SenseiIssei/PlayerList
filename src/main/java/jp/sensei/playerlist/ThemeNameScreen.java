package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ThemeNameScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget input;

    public ThemeNameScreen(Screen parent) {
        super(Text.literal("Theme benennen"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        input = new TextFieldWidget(textRenderer, x, 60, 200, 20, Text.literal("Theme-Name"));
        this.addSelectableChild(input);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Speichern"), btn -> {
            String name = input.getText().trim();
            if (!name.isEmpty()) {
                ThemeStorage.saveTheme(CustomTheme.fromConfig(PlayerListConfig.config, name));
                MinecraftClient.getInstance().setScreen(parent);
            }
        }).dimensions(x, 90, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Abbrechen"), btn ->
                MinecraftClient.getInstance().setScreen(parent)
        ).dimensions(x, 120, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mx, int my, float delta) {
        this.renderBackground(context, mx, my, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        input.render(context, mx, my, delta);
        super.render(context, mx, my, delta);
    }
}
