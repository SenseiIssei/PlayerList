package jp.sensei.playerlist;

public enum ThemePreset {
    DARK("Dunkel", 0xA0000000, 0xFFFFFFFF, 0xFFDD4444, 0x60000000, 0xFFAA00, 0xFF00FFFF, 0xFF00FF00),
    LIGHT("Hell",  0x99FFFFFF, 0xFF000000, 0xFFFF4444, 0x80FFFFFF, 0xFF4444AA, 0xFF00FFFF, 0xFF00FF00),
    NEON("Neon",   0x90000000, 0xFFFFFFFF, 0xFFFF00FF, 0x80000000, 0xFFFFFF00, 0xFFFF4444, 0xFF00FF00),
    MINIMAL("Minimal", 0x00000000, 0xFFFFFFFF, 0xFFFFFFFF, 0x20000000, 0xFF999999, 0xFFFFFFFF, 0xFFFFFFFF);

    public final String displayName;
    public final int hudBackground, textColor, dotColor, radarBg, arrowColor, focusColor, stickyColor;

    ThemePreset(String name, int hudBg, int text, int dot, int radarBg, int arrow, int focus, int sticky) {
        this.displayName = name;
        this.hudBackground = hudBg;
        this.textColor = text;
        this.dotColor = dot;
        this.radarBg = radarBg;
        this.arrowColor = arrow;
        this.focusColor = focus;
        this.stickyColor = sticky;
    }

    public void apply(PlayerListConfig.Config cfg) {
        cfg.hudBackgroundColor = hudBackground;
        cfg.hudTextColor = textColor;
        cfg.radarDotColor = dotColor;
        cfg.radarBackgroundColor = radarBg;
        cfg.radarArrowColor = arrowColor;
        cfg.focusHighlightColor = focusColor;
        cfg.stickyArrowColor = stickyColor;
    }
}
