package jp.sensei.playerlist;

import java.util.List;

public class SubmenuFactory {
    public static ScrollablePanel createGeneralMenu(PlayerListScreen screen) {
        ScrollablePanel panel = new ScrollablePanel();

        panel.addComponent(new BoolToggleButton("PlayerList Enabled", () -> PlayerListConfig.config.enabled, v -> { PlayerListConfig.config.enabled = v; PlayerListConfig.save(); }));
        panel.addComponent(new BoolToggleButton("Show Distance (m)", () -> PlayerListConfig.config.showDistance, v -> { PlayerListConfig.config.showDistance = v; PlayerListConfig.save(); }));
        panel.addComponent(new BoolToggleButton("Large Notification Popups", () -> PlayerListConfig.config.bigNotifications, v -> { PlayerListConfig.config.bigNotifications = v; PlayerListConfig.save(); }));
        panel.addComponent(new BoolToggleButton("Hide Your Entry", () -> PlayerListConfig.config.hideSelf, v -> { PlayerListConfig.config.hideSelf = v; PlayerListConfig.save(); }));
        panel.addComponent(new ActionButton("Excluded Players…", () -> screen.client.setScreen(new ExcludedNamesScreen(screen))));
        panel.addComponent(new ActionButton("Sort: " + PlayerListConfig.config.sortMode.name(), () -> {
            PlayerListConfig.config.sortMode = PlayerListConfig.config.sortMode == PlayerListConfig.Config.SortMode.NAME
                    ? PlayerListConfig.Config.SortMode.DISTANCE
                    : PlayerListConfig.Config.SortMode.NAME;
            PlayerListConfig.save();
            screen.init();
        }));

        return panel;
    }

    public static ScrollablePanel createHudMenu(PlayerListScreen screen) {
        ScrollablePanel panel = new ScrollablePanel();

        panel.addComponent(new FloatSlider("Base Width", 100f, 400f, () -> PlayerListConfig.config.baseWidth, v -> { PlayerListConfig.config.baseWidth = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Base Height", 80f, 300f, () -> PlayerListConfig.config.baseHeight, v -> { PlayerListConfig.config.baseHeight = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Min Scale", 0.4f, 1.0f, () -> PlayerListConfig.config.minScale, v -> { PlayerListConfig.config.minScale = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Max Visible Players", 2f, 30f, () -> (float) PlayerListConfig.config.maxVisiblePlayers, v -> { PlayerListConfig.config.maxVisiblePlayers = Math.round(v); PlayerListConfig.save(); }));

        panel.addComponent(new FloatSlider("Font Scale", 0.5f, 2.0f, () -> PlayerListConfig.config.fontScaleMultiplier, v -> { PlayerListConfig.config.fontScaleMultiplier = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Padding Scale", 0.5f, 2.0f, () -> PlayerListConfig.config.paddingScale, v -> { PlayerListConfig.config.paddingScale = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Line Height Scale", 0.5f, 2.0f, () -> PlayerListConfig.config.lineHeightScale, v -> { PlayerListConfig.config.lineHeightScale = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Icon Scale", 0.5f, 2.0f, () -> PlayerListConfig.config.iconScale, v -> { PlayerListConfig.config.iconScale = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Section Spacing", 0.5f, 2.0f, () -> PlayerListConfig.config.sectionSpacingScale, v -> { PlayerListConfig.config.sectionSpacingScale = v; PlayerListConfig.save(); }));

        return panel;
    }

    public static ScrollablePanel createRadarMenu(PlayerListScreen screen) {
        ScrollablePanel panel = new ScrollablePanel();

        panel.addComponent(new BoolToggleButton("Show Minimap on HUD", () -> PlayerListConfig.config.minimapEnabled, v -> { PlayerListConfig.config.minimapEnabled = v; PlayerListConfig.save(); }));
        panel.addComponent(new BoolToggleButton("Use Circular Radar (instead of Minimap)", () -> PlayerListConfig.config.radarModeCircular, v -> { PlayerListConfig.config.radarModeCircular = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Minimap Font Scale", 0.3f, 1.5f, () -> PlayerListConfig.config.minimapFontScale, v -> { PlayerListConfig.config.minimapFontScale = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Minimap Zoom (blocks per pixel)", 0.5f, 5f, () -> PlayerListConfig.config.minimapZoom, v -> { PlayerListConfig.config.minimapZoom = v; PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Minimap Sampling (pixel step)", 1f, 8f, () -> (float) PlayerListConfig.config.minimapPixelStep, v -> { PlayerListConfig.config.minimapPixelStep = Math.round(v); PlayerListConfig.save(); }));

        panel.addComponent(new BoolToggleButton("Show Names on Radar", () -> PlayerListConfig.config.radarShowNames, v -> { PlayerListConfig.config.radarShowNames = v; PlayerListConfig.save(); }));

        panel.addComponent(new FloatSlider("Radar Width", 40f, 300f, () -> (float) PlayerListConfig.config.radarWidth, v -> { PlayerListConfig.config.radarWidth = Math.round(v); PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Radar Height", 40f, 300f, () -> (float) PlayerListConfig.config.radarHeight, v -> { PlayerListConfig.config.radarHeight = Math.round(v); PlayerListConfig.save(); }));

        panel.addComponent(new FloatSlider("Your Dot Size", 1f, 20f, () -> (float) PlayerListConfig.config.selfDotSize, v -> { PlayerListConfig.config.selfDotSize = Math.round(v); PlayerListConfig.save(); }));
        panel.addComponent(new FloatSlider("Other Players Dot Size", 1f, 20f, () -> (float) PlayerListConfig.config.playerDotSize, v -> { PlayerListConfig.config.playerDotSize = Math.round(v); PlayerListConfig.save(); }));

        panel.addComponent(new ActionButton("Radar Background Color", () -> screen.client.setScreen(new ColorPickerScreen(screen, "Radar Background", PlayerListConfig.config.radarBackgroundColor, color -> PlayerListConfig.config.radarBackgroundColor = color))));
        panel.addComponent(new ActionButton("Dot Color", () -> screen.client.setScreen(new ColorPickerScreen(screen, "Player Dot", PlayerListConfig.config.radarDotColor, color -> PlayerListConfig.config.radarDotColor = color))));
        panel.addComponent(new ActionButton("Arrow Color", () -> screen.client.setScreen(new ColorPickerScreen(screen, "Direction Arrow", PlayerListConfig.config.radarArrowColor, color -> PlayerListConfig.config.radarArrowColor = color))));
        panel.addComponent(new ActionButton("Focus Highlight Color", () -> screen.client.setScreen(new ColorPickerScreen(screen, "Focus Highlight", PlayerListConfig.config.focusHighlightColor, color -> PlayerListConfig.config.focusHighlightColor = color))));

        return panel;
    }

    public static ScrollablePanel createTrackedMenu(PlayerListScreen screen) {
        ScrollablePanel panel = new ScrollablePanel();

        panel.addComponent(new ActionButton("Select Focus Player", () -> screen.client.setScreen(new SelectTrackedPlayerScreen(screen, false))));
        panel.addComponent(new ActionButton("Select Sticky Player", () -> screen.client.setScreen(new SelectTrackedPlayerScreen(screen, true))));
        panel.addComponent(new ActionButton("Clear Focus Player", () -> {
            PlayerListConfig.config.focusTarget = null;
            PlayerListConfig.save();
        }));
        panel.addComponent(new ActionButton("Clear Sticky Tracker", () -> {
            PlayerListConfig.config.stickyTarget = null;
            PlayerListConfig.config.stickyTrackerEnabled = false;
            PlayerListConfig.save();
        }));

        return panel;
    }
}
