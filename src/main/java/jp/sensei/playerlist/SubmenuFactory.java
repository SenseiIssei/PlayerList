package jp.sensei.playerlist;

import java.util.List;

public class SubmenuFactory {
    public static ScrollablePanel createGeneralMenu(PlayerListScreen screen) {
        ScrollablePanel panel = new ScrollablePanel();

        panel.addComponent(new BoolToggleButton("Enabled", PlayerListConfig.config.enabled, v -> PlayerListConfig.config.enabled = v));
        panel.addComponent(new BoolToggleButton("Show Distance", PlayerListConfig.config.showDistance, v -> PlayerListConfig.config.showDistance = v));
        panel.addComponent(new BoolToggleButton("Big Notifications", PlayerListConfig.config.bigNotifications, v -> PlayerListConfig.config.bigNotifications = v));
        panel.addComponent(new BoolToggleButton("Hide Self", PlayerListConfig.config.hideSelf, v -> PlayerListConfig.config.hideSelf = v));
        panel.addComponent(new ActionButton("Edit Excluded Players", () -> screen.client.setScreen(new ExcludedNamesScreen(screen))));
        panel.addComponent(new ActionButton("Sort by: " + PlayerListConfig.config.sortMode.name(), () -> {
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

        panel.addComponent(new FloatSlider("Base Width", 100f, 400f, PlayerListConfig.config.baseWidth, v -> PlayerListConfig.config.baseWidth = v));
        panel.addComponent(new FloatSlider("Base Height", 80f, 300f, PlayerListConfig.config.baseHeight, v -> PlayerListConfig.config.baseHeight = v));
        panel.addComponent(new FloatSlider("Min Scale", 0.4f, 1.0f, PlayerListConfig.config.minScale, v -> PlayerListConfig.config.minScale = v));
        panel.addComponent(new FloatSlider("Max Visible Players", 2f, 30f, PlayerListConfig.config.maxVisiblePlayers, v -> PlayerListConfig.config.maxVisiblePlayers = Math.round(v)));

        panel.addComponent(new FloatSlider("Font Scale", 0.5f, 2.0f, PlayerListConfig.config.fontScaleMultiplier, v -> PlayerListConfig.config.fontScaleMultiplier = v));
        panel.addComponent(new FloatSlider("Padding Scale", 0.5f, 2.0f, PlayerListConfig.config.paddingScale, v -> PlayerListConfig.config.paddingScale = v));
        panel.addComponent(new FloatSlider("Line Height Scale", 0.5f, 2.0f, PlayerListConfig.config.lineHeightScale, v -> PlayerListConfig.config.lineHeightScale = v));
        panel.addComponent(new FloatSlider("Icon Scale", 0.5f, 2.0f, PlayerListConfig.config.iconScale, v -> PlayerListConfig.config.iconScale = v));
        panel.addComponent(new FloatSlider("Section Spacing", 0.5f, 2.0f, PlayerListConfig.config.sectionSpacingScale, v -> PlayerListConfig.config.sectionSpacingScale = v));

        return panel;
    }

    public static ScrollablePanel createRadarMenu(PlayerListScreen screen) {
        ScrollablePanel panel = new ScrollablePanel();

        panel.addComponent(new BoolToggleButton("Minimap & Radar Enabled", PlayerListConfig.config.minimapEnabled, v -> PlayerListConfig.config.minimapEnabled = v));
        panel.addComponent(new BoolToggleButton("Radar Mode (true=Radar, false=Minimap)", PlayerListConfig.config.radarModeCircular, v -> PlayerListConfig.config.radarModeCircular = v));
        panel.addComponent(new FloatSlider("Minimap Font Scale", 0.3f, 1.5f, PlayerListConfig.config.minimapFontScale, v -> PlayerListConfig.config.minimapFontScale = v));
        panel.addComponent(new FloatSlider("Minimap Zoom (blocks per pixel)", 0.5f, 5f, PlayerListConfig.config.minimapZoom, v -> PlayerListConfig.config.minimapZoom = v));
        panel.addComponent(new FloatSlider("Minimap Pixel Step", 1f, 5f, PlayerListConfig.config.minimapPixelStep, v -> PlayerListConfig.config.minimapPixelStep = Math.round(v)));

        panel.addComponent(new BoolToggleButton("Show Names on Radar", PlayerListConfig.config.radarShowNames, v -> PlayerListConfig.config.radarShowNames = v));

        panel.addComponent(new FloatSlider("Radar Width", 40f, 300f, PlayerListConfig.config.radarWidth, v -> PlayerListConfig.config.radarWidth = Math.round(v)));
        panel.addComponent(new FloatSlider("Radar Height", 40f, 300f, PlayerListConfig.config.radarHeight, v -> PlayerListConfig.config.radarHeight = Math.round(v)));

        // New sliders for player dot sizes
        panel.addComponent(new FloatSlider("Your Dot Size", 1f, 20f, PlayerListConfig.config.selfDotSize, v -> PlayerListConfig.config.selfDotSize = Math.round(v)));
        panel.addComponent(new FloatSlider("Other Players Dot Size", 1f, 20f, PlayerListConfig.config.playerDotSize, v -> PlayerListConfig.config.playerDotSize = Math.round(v)));

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

    public static ScrollablePanel createThemesMenu(PlayerListScreen screen) {
        ScrollablePanel panel = new ScrollablePanel();

        panel.addComponent(new ActionButton("Save current as preset", () -> screen.client.setScreen(new SaveThemeScreen(screen))));
        List<CustomTheme> themes = ThemeStorage.getThemes();
        for (CustomTheme theme : themes) {
            panel.addComponent(new ActionButton("Load: " + theme.name, () -> {
                theme.applyTo(PlayerListConfig.config);
                PlayerListConfig.save();
                screen.init();
            }));
            panel.addComponent(new ActionButton("✖ Delete: " + theme.name, () -> {
                ThemeStorage.removeTheme(theme.name);
                screen.init();
            }));
        }
        panel.addComponent(new ActionButton("Open Theme Folder", screen::openThemeFolder));

        return panel;
    }
}
