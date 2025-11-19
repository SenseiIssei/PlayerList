package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ThemeSelectScreen extends Screen {
    private final Screen parent;
    private List<CustomTheme> themes;
    private int scroll = 0;
    private final int visibleCount = 8;
    private int spacing = 24;

    public ThemeSelectScreen(Screen parent) {
        super(Text.literal("Select Theme Preset"));
        this.parent = parent;
        this.themes = ThemeStorage.getThemes();
    }

    @Override
    protected void init() {
        this.clearChildren();

        int y = 40;
        int centerX = width / 2;

        // Scroll Up
        if (scroll > 0) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("↑ Scroll Up"), btn -> {
                scroll = Math.max(0, scroll - visibleCount);
                init();
            }).dimensions(centerX - 100, 10, 200, 20).build());
        }

        // Theme Buttons
        int end = Math.min(scroll + visibleCount, themes.size());
        for (int i = scroll; i < end; i++) {
            CustomTheme theme = themes.get(i);
            int finalY = y + (i - scroll) * spacing;

            this.addDrawableChild(ButtonWidget.builder(Text.literal(theme.name), btn -> {
                        theme.applyTo(PlayerListConfig.config);
                        PlayerListConfig.save();
                        MinecraftClient.getInstance().setScreen(parent);
                    })
                    .dimensions(centerX - 150, finalY, 120, 20)
                    .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal("Click to apply theme")))
                    .build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), btn -> {
                        ThemeStorage.removeTheme(theme.name);
                        themes = ThemeStorage.getThemes();
                        if (scroll >= themes.size() && scroll > 0) scroll -= visibleCount;
                        init();
                    })
                    .dimensions(centerX - 20, finalY, 80, 20)
                    .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal("Delete this preset")))
                    .build());
        }

        // Done button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn ->
                        MinecraftClient.getInstance().setScreen(parent))
                .dimensions(centerX - 100, height - 40, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, this.title, width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
