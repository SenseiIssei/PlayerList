package jp.sensei.playerlist;

import java.util.Objects;

public class CustomTheme {
    public String name = "Unnamed";

    public int radarColor = 0x60000000;
    public int minimapColor = 0x60000000;
    public int playerColor = 0xFFFF0000;

    public boolean showNames = false;
    public boolean roundRadar = true;

    public static CustomTheme from(PlayerListConfig.Config config) {
        CustomTheme theme = new CustomTheme();
        theme.radarColor = config.radarBackgroundColor;
        theme.playerColor = config.radarDotColor;
        theme.showNames = config.radarShowNames;
        theme.roundRadar = config.radarRoundBorder;
        return theme;
    }

    public static CustomTheme fromConfig(PlayerListConfig.Config config, String name) {
        CustomTheme theme = from(config);
        theme.name = name;
        return theme;
    }

    public void applyTo(PlayerListConfig.Config config) {
        config.radarBackgroundColor = this.radarColor;
        config.radarDotColor = this.playerColor;
        config.radarShowNames = this.showNames;
        config.radarRoundBorder = this.roundRadar;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CustomTheme other)) return false;
        return Objects.equals(this.name, other.name)
                && radarColor == other.radarColor
                && minimapColor == other.minimapColor
                && playerColor == other.playerColor
                && showNames == other.showNames
                && roundRadar == other.roundRadar;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, radarColor, minimapColor, playerColor, showNames, roundRadar);
    }
}