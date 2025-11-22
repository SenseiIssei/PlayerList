package jp.sensei.playerlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerListConfig {
    public static final File CONFIG_FILE = new File("config/playerlist/playerlist.json");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Config config = new Config();

    public static class Config {
        // === General Settings ===
        public boolean enabled = true;                  // Enable PlayerList mod
        public boolean showDistance = true;             // Show player distances
        public boolean bigNotifications = true;         // Show big notifications
        public int maxVisiblePlayers = 10;               // Max players visible in list
        public boolean hideSelf = false;                  // Hide self from player list
        public List<String> excludedNames = new ArrayList<>(); // Names to exclude (e.g. "slot_11")
        public SortMode sortMode = SortMode.NAME;         // Sorting mode

        public enum SortMode { NAME, DISTANCE }

        // === HUD Position & Scaling ===
        public int hudX = 10;
        public int hudY = 10;
        public int windowWidth = 180;
        public int windowHeight = 150;
        public float baseWidth = 180f;                     // Base width for scaling
        public float baseHeight = 140f;                    // Base height for scaling
        public float minScale = 0.4f;                       // Minimum HUD scale
        public float fontScaleMultiplier = 1.0f;           // Font scale multiplier
        public float paddingScale = 1.0f;                   // Padding scale
        public float iconScale = 1.0f;                      // Icon scale
        public float lineHeightScale = 1.0f;                // Line height scale
        public float sectionSpacingScale = 1.0f;            // Section spacing scale

        // === Radar & Minimap Display ===
        // Default minimap/radar is OFF to avoid unnecessary CPU/memory usage on low-end systems.
        public boolean minimapEnabled = false;

        public boolean radarModeCircular = true;    // true = radar (circular), false = minimap (rectangular)

        public boolean radarShowNames = true;
        public boolean radarRoundBorder = true;

        // === Radar/Minimap Position & Size ===
        public int minimapPixelStep = 2;
        // Maximum total 'pixels' to generate for minimap cache before increasing sampling.
        // This caps memory usage for the generated color grid (pixelsWide * pixelsHigh).
        public int minimapMaxPixels = 2000;
        // Max entries in the color cache before it gets cleared to avoid unbounded growth.
        public int minimapCacheMaxEntries = 2000;
        public float minimapFontScale = 0.5f;                // default smaller font for minimap
        public float minimapZoom = 2f;                       // blocks per pixel zoom for minimap (lower = bigger zoom)
        public int radarX = 10;
        public int radarY = 170;
        public int radarWidth = 120;
        public int radarHeight = 120;
        public float radarZoom = 60f;                // zoom distance in meters (for radar)

        // === Player Dot Sizes ===
        public int selfDotSize = 6;       // Size of your own player dot on radar/minimap
        public int playerDotSize = 3;     // Size of other players' dots on radar/minimap

        // === Colors ===
        public int hudBackgroundColor = 0x60000000;         // HUD background ARGB
        public int hudTextColor = 0xFFFFFFFF;                // HUD text color ARGB
        public int radarDotColor = 0xFFFF4444;               // Radar player dot color ARGB
        public int radarBackgroundColor = 0x60000000;        // Radar/minimap background ARGB
        public int radarArrowColor = 0xFFDD44;               // Direction arrow color ARGB
        public int focusHighlightColor = 0xFF00FFFF;         // Focus player highlight color ARGB
        public int stickyArrowColor = 0xFF00FF00;            // Sticky tracker arrow color ARGB

        // === Menu Styling ===
        public int menuBackgroundColor = 0xE0101A1F;        // Settings menu background ARGB
        public int menuTextColor = 0xFFFFFFFF;              // Settings menu text color ARGB
        public int menuAccentColor = 0xFF3B82F6;            // Accent color for active tabs/buttons
        public int menuRowHeight = 22;                      // Height of each row in settings
        public int menuInnerPadding = 20;                   // Inner padding for components
        public int menuGap = 2;                             // Vertical gap between rows
        public int menuSafeEdge = 5;                        // Extra safe-edge padding from right side
        public int menuBottomPadding = 0;                   // Additional bottom padding for menus (configurable)



        // === Focus/Sticky Player Tracking ===
        public UUID focusTarget = null;                       // UUID of focus player
        public UUID stickyTarget = null;                      // UUID of sticky player
        public boolean stickyTrackerEnabled = false;         // Enable sticky tracker
    }

    public static void load() {
        try {
            if (CONFIG_FILE.exists()) {
                try (FileReader reader = new FileReader(CONFIG_FILE)) {
                    config = GSON.fromJson(reader, Config.class);
                }
            } else {
                //(slot_11 .. slot_90)
                for (int i = 11; i <= 90; i++) {
                    config.excludedNames.add("slot_" + i);
                }
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
