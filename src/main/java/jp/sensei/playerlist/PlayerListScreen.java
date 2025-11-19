package jp.sensei.playerlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class PlayerListScreen extends Screen {

    final MinecraftClient client = MinecraftClient.getInstance();

    private TabBar tabBar;
    private ScrollablePanel contentPanel;

    private ResizeHandle hudResizeHandle;
    private DragArea hudDragArea;

    private ResizeHandle radarResizeHandle;
    private DragArea radarDragArea;

    private int scrollOffset = 0;
    
    private int contentTop;

    public PlayerListScreen() {
        super(Text.literal("PlayerList Settings"));
    }

    @Override
    protected void init() {
        tabBar = new TabBar(this, width, 28);

        hudResizeHandle = new ResizeHandle(
                () -> PlayerListConfig.config.hudX,
                () -> PlayerListConfig.config.hudY,
                () -> PlayerListConfig.config.windowWidth,
                () -> PlayerListConfig.config.windowHeight
        );
        hudDragArea = new DragArea(
                () -> PlayerListConfig.config.hudX,
                () -> PlayerListConfig.config.hudY,
                () -> PlayerListConfig.config.windowWidth,
                () -> PlayerListConfig.config.windowHeight
        );

        radarResizeHandle = new ResizeHandle(
                () -> PlayerListConfig.config.radarX,
                () -> PlayerListConfig.config.radarY,
                () -> PlayerListConfig.config.radarWidth,
                () -> PlayerListConfig.config.radarHeight
        );
        radarDragArea = new DragArea(
                () -> PlayerListConfig.config.radarX,
                () -> PlayerListConfig.config.radarY,
                () -> PlayerListConfig.config.radarWidth,
                () -> PlayerListConfig.config.radarHeight
        );

        rebuildContent();
    }

    private void rebuildContent() {
        switch (tabBar.getSelectedTab()) {
            case GENERAL -> contentPanel = SubmenuFactory.createGeneralMenu(this);
            case HUD -> contentPanel = SubmenuFactory.createHudMenu(this);
            case RADAR -> contentPanel = SubmenuFactory.createRadarMenu(this);
            case TRACKED -> contentPanel = SubmenuFactory.createTrackedMenu(this);
        }
        scrollOffset = 0;
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xC0000000);

        
        
        int panelW = Math.min(920, width - 60);
        int panelH = Math.min(560, height - 80);
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xE0101A1F);

        int titleY = panelY + 6;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("PlayerList Settings"), width / 2, titleY, 0xFFFFFFFF);

        int tabBarY = titleY + 8;
        tabBar.render(context, mouseX, mouseY);

        contentTop = tabBarY + 12;

        int contentAreaHeight = panelY + panelH - contentTop - 20;

        context.getMatrices().push();
        context.getMatrices().translate(panelX, contentTop - scrollOffset, 0);
        context.enableScissor(panelX, contentTop, panelW, contentAreaHeight);
        contentPanel.render(context, mouseX - panelX, mouseY - contentTop + scrollOffset, panelW);
        context.disableScissor();
        context.getMatrices().pop();

        hudDragArea.render(context);
        hudResizeHandle.render(context, mouseX, mouseY);

        radarDragArea.render(context);
        radarResizeHandle.render(context, mouseX, mouseY);

        if (PlayerListConfig.config.minimapEnabled) {
            RadarRenderer.render(context, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tabBar.mouseClicked(mouseX, mouseY)) {
            rebuildContent();
            return true;
        }

        int panelW = Math.min(920, width - 60);
        int panelX = (width - panelW) / 2;
        
        int adjustedMouseX = (int) (mouseX - panelX);
        int adjustedMouseY = (int) (mouseY - contentTop + scrollOffset);

        if (hudResizeHandle.mouseClicked(mouseX, mouseY)) return true;
        if (hudDragArea.mouseClicked(mouseX, mouseY)) return true;

        if (radarResizeHandle.mouseClicked(mouseX, mouseY)) return true;
        if (radarDragArea.mouseClicked(mouseX, mouseY)) return true;

        if (contentPanel.mouseClicked(adjustedMouseX, adjustedMouseY, panelW)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        hudResizeHandle.mouseReleased();
        hudDragArea.mouseReleased();

        radarResizeHandle.mouseReleased();
        radarDragArea.mouseReleased();

        contentPanel.mouseReleased();

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (hudResizeHandle.isDragging()) {
            PlayerListConfig.config.windowWidth = Math.max(50, (int) mouseX - PlayerListConfig.config.hudX);
            PlayerListConfig.config.windowHeight = Math.max(30, (int) mouseY - PlayerListConfig.config.hudY);
            PlayerListConfig.save();
            return true;
        }

        if (hudDragArea.isDragging()) {
            PlayerListConfig.config.hudX = (int) mouseX - hudDragArea.getDragOffsetX();
            PlayerListConfig.config.hudY = (int) mouseY - hudDragArea.getDragOffsetY();
            PlayerListConfig.save();
            return true;
        }

        if (radarResizeHandle.isDragging()) {
            PlayerListConfig.config.radarWidth = Math.max(30, (int) mouseX - PlayerListConfig.config.radarX);
            PlayerListConfig.config.radarHeight = Math.max(30, (int) mouseY - PlayerListConfig.config.radarY);
            PlayerListConfig.save();
            return true;
        }

        if (radarDragArea.isDragging()) {
            PlayerListConfig.config.radarX = (int) mouseX - radarDragArea.getDragOffsetX();
            PlayerListConfig.config.radarY = (int) mouseY - radarDragArea.getDragOffsetY();
            PlayerListConfig.save();
            return true;
        }

        int panelW = Math.min(920, width - 60);
        int panelX = (width - panelW) / 2;
        int adjustedMouseX = (int) (mouseX - panelX);
        int adjustedMouseY = (int) (mouseY - contentTop + scrollOffset);
        if (contentPanel.mouseDragged(adjustedMouseX, adjustedMouseY, deltaX, deltaY, panelW)) return true;

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scrollOffset -= vertical * 20;
        int panelW = Math.min(920, width - 60);
        int panelH = Math.min(560, height - 80);
        int available = panelH - 40;
        scrollOffset = Math.max(0,
            Math.min(scrollOffset,
                Math.max(0, contentPanel.getContentHeight(panelW) - available)
            ));
        return true;
    }
}
