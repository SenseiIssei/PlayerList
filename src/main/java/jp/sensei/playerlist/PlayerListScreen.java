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
    private double targetScrollOffset = 0.0;
    private double scrollOffsetSmooth = 0.0;
    private final double scrollSmoothing = 0.18;
    
    private int contentTop;
    private boolean scrollbarDragging = false;
    private int scrollbarDragStartY = 0;
    private int scrollbarStartOffset = 0;

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
            case STYLE -> contentPanel = SubmenuFactory.createStyleMenu(this);
        }
        scrollOffset = 0;
        targetScrollOffset = 0.0;
        scrollOffsetSmooth = 0.0;
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xC0000000);

        int panelW = Math.min(920, width - 60);
        int panelX = (width - panelW) / 2;
        int panelY = 40;

        int titleY = panelY + 4;
        int tabBarY = titleY + 6;
        contentTop = tabBarY + 8;

        int contentHeight = contentPanel.getContentHeight(panelW);
        int padding = PlayerListConfig.config.menuInnerPadding;
        int bottomPadding = PlayerListConfig.config.menuBottomPadding;
        if (bottomPadding <= 0) bottomPadding = Math.max(8, padding / 2);

        int maxPanelH = Math.max(220, height - panelY - 40);
        int desiredPanelH = contentHeight + contentTop + bottomPadding - panelY;
        int panelH = Math.max(220, Math.min(maxPanelH, desiredPanelH));

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, PlayerListConfig.config.menuBackgroundColor);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("PlayerList Settings"), width / 2, titleY, PlayerListConfig.config.menuTextColor);
        tabBar.render(context, mouseX, mouseY);

        int contentAreaHeight = panelY + panelH - contentTop - bottomPadding;

        int visible = contentAreaHeight;
        int maxOffset = Math.max(0, contentHeight - visible);
        if (targetScrollOffset < 0) targetScrollOffset = 0;
        if (targetScrollOffset > maxOffset) targetScrollOffset = maxOffset;

        scrollOffsetSmooth += (targetScrollOffset - scrollOffsetSmooth) * scrollSmoothing;
        if (Math.abs(scrollOffsetSmooth - targetScrollOffset) < 0.5) scrollOffsetSmooth = targetScrollOffset;
        scrollOffset = Math.max(0, Math.min(maxOffset, (int) Math.round(scrollOffsetSmooth)));

        context.getMatrices().push();
        context.getMatrices().translate(panelX, contentTop - scrollOffset, 0);
        context.enableScissor(panelX, contentTop, panelW, contentAreaHeight);
        contentPanel.render(context, mouseX - panelX, mouseY - contentTop + scrollOffset, panelW);
        context.disableScissor();
        context.getMatrices().pop();

        if (contentHeight > visible) {
            int trackW = 8;
            int trackX = panelX + panelW - trackW - 6;
            int trackY = contentTop;
            int trackH = visible;
            context.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0x44000000);

            float ratio = (float) visible / (float) contentHeight;
            int thumbH = Math.max(12, (int) (ratio * trackH));
            int maxThumbTravel = Math.max(1, trackH - thumbH);
            int thumbY = trackY + (int) ((float) scrollOffset / Math.max(1, contentHeight - visible) * maxThumbTravel);
            context.fill(trackX, thumbY, trackX + trackW, thumbY + thumbH, PlayerListConfig.config.menuAccentColor);
        }

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
        int panelY = 40;
        int panelH = Math.max(220, height - panelY - 40);
        
        int adjustedMouseX = (int) (mouseX - panelX);
        int adjustedMouseY = (int) (mouseY - contentTop + scrollOffset);

        if (hudResizeHandle.mouseClicked(mouseX, mouseY)) return true;
        if (hudDragArea.mouseClicked(mouseX, mouseY)) return true;

        if (radarResizeHandle.mouseClicked(mouseX, mouseY)) return true;
        if (radarDragArea.mouseClicked(mouseX, mouseY)) return true;

        if (contentPanel.mouseClicked(adjustedMouseX, adjustedMouseY, panelW)) return true;

        int contentHeight = contentPanel.getContentHeight(panelW);
        int padding = PlayerListConfig.config.menuInnerPadding;
        int bottomPadding = PlayerListConfig.config.menuBottomPadding;
        if (bottomPadding <= 0) bottomPadding = Math.max(8, padding / 2);
        int available = panelY + panelH - contentTop - bottomPadding;
        if (contentHeight > available) {
            int trackW = 8;
            int trackX = panelX + panelW - trackW - 6;
            int trackY = contentTop;
            int trackH = available;
            float ratio = (float) available / (float) contentHeight;
            int thumbH = Math.max(12, (int) (ratio * trackH));
            int maxThumbTravel = Math.max(1, trackH - thumbH);
            int thumbY = trackY + (int) ((float) scrollOffset / Math.max(1, contentHeight - available) * maxThumbTravel);

            int tx = (int) mouseX;
            int ty = (int) mouseY;
            if (tx >= trackX && tx <= trackX + trackW && ty >= contentTop && ty <= contentTop + available) {
                if (ty < thumbY) {
                    targetScrollOffset = Math.max(0, (int) (targetScrollOffset - available));
                    scrollOffsetSmooth = targetScrollOffset;
                    return true;
                } else if (ty > thumbY + thumbH) {
                    targetScrollOffset = Math.min(Math.max(0, contentHeight - available), (int) (targetScrollOffset + available));
                    scrollOffsetSmooth = targetScrollOffset;
                    return true;
                } else {
                    scrollbarDragging = true;
                    scrollbarDragStartY = ty;
                    scrollbarStartOffset = (int) Math.round(targetScrollOffset);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        hudResizeHandle.mouseReleased();
        hudDragArea.mouseReleased();

        radarResizeHandle.mouseReleased();
        radarDragArea.mouseReleased();

        contentPanel.mouseReleased();

        if (scrollbarDragging) {
            scrollbarDragging = false;
            return true;
        }

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

        if (scrollbarDragging) {
                int panelY = 40;
                int panelH = Math.max(220, height - panelY - 40);
                int padding = PlayerListConfig.config.menuInnerPadding;
                int bottomPadding = PlayerListConfig.config.menuBottomPadding;
                if (bottomPadding <= 0) bottomPadding = Math.max(8, padding / 2);
                int available = panelY + panelH - contentTop - bottomPadding;
            int contentHeight = contentPanel.getContentHeight(panelW);
            int visible = available;
            if (contentHeight > visible) {
                int trackH = visible;
                float ratio = (float) visible / (float) contentHeight;
                int thumbH = Math.max(12, (int) (ratio * trackH));
                int maxThumbTravel = Math.max(1, trackH - thumbH);

                int delta = (int) mouseY - scrollbarDragStartY;
                float travelRatio = (float) delta / (float) Math.max(1, maxThumbTravel);
                int newOffset = scrollbarStartOffset + (int) (travelRatio * (contentHeight - visible));
                newOffset = Math.max(0, Math.min(newOffset, Math.max(0, contentHeight - visible)));
                targetScrollOffset = newOffset;
                scrollOffsetSmooth = newOffset;
                scrollOffset = newOffset;
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int panelW = Math.min(920, width - 60);
        int panelY = 40;
        int panelH = Math.max(220, height - panelY - 40);
        int padding = PlayerListConfig.config.menuInnerPadding;
        int bottomPadding = PlayerListConfig.config.menuBottomPadding;
        if (bottomPadding <= 0) bottomPadding = Math.max(8, padding / 2);
        int available = panelY + panelH - contentTop - bottomPadding;
        int contentHeight = contentPanel.getContentHeight(panelW);
        int maxOffset = Math.max(0, contentHeight - available);
        targetScrollOffset = Math.max(0, Math.min(maxOffset, (int) Math.round(targetScrollOffset - vertical * 20)));
        return true;
    }
}
