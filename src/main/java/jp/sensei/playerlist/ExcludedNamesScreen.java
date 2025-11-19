package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ExcludedNamesScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget inputField;
    private int scrollIndex = 0;
    private static final int VISIBLE_COUNT = 5;

    public ExcludedNamesScreen(Screen parent) {
        super(Text.literal("Manage Excluded Names"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren(); // Clear previous children when reopening

        int centerX = width / 2;
        inputField = new TextFieldWidget(textRenderer, centerX - 100, 30, 200, 20, Text.literal("Add Name"));
        this.addSelectableChild(inputField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
            String name = inputField.getText().trim();
            if (!name.isEmpty() && !PlayerListConfig.config.excludedNames.contains(name)) {
                PlayerListConfig.config.excludedNames.add(name);
                PlayerListConfig.save();
                inputField.setText("");
                this.rebuild();
            }
        }).dimensions(centerX - 100, 60, 200, 20).build());

        this.rebuild();
    }

    private void rebuild() {
        // Remove and re-add exclusion entries
        List<String> list = PlayerListConfig.config.excludedNames;
        int startY = 90;
        int centerX = width / 2;

        int endIndex = Math.min(scrollIndex + VISIBLE_COUNT, list.size());
        for (int i = scrollIndex; i < endIndex; i++) {
            String name = list.get(i);
            int y = startY + (i - scrollIndex) * 24;
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Remove " + name), btn -> {
                PlayerListConfig.config.excludedNames.remove(name);
                PlayerListConfig.save();
                if (scrollIndex > 0 && scrollIndex >= list.size()) scrollIndex--;
                this.rebuild();
            }).dimensions(centerX - 100, y, 200, 20).build());
        }

        // Navigation buttons
        if (scrollIndex > 0) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("↑ Prev"), btn -> {
                scrollIndex = Math.max(0, scrollIndex - VISIBLE_COUNT);
                this.rebuild();
            }).dimensions(centerX - 100, height - 70, 95, 20).build());
        }

        if (list.size() > scrollIndex + VISIBLE_COUNT) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Next ↓"), btn -> {
                scrollIndex = Math.min(list.size() - VISIBLE_COUNT, scrollIndex + VISIBLE_COUNT);
                this.rebuild();
            }).dimensions(centerX + 5, height - 70, 95, 20).build());
        }

        // Done button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> {
            PlayerListConfig.save();
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX - 100, height - 40, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
        inputField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
