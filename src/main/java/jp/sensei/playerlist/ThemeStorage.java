package jp.sensei.playerlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;

public class ThemeStorage {
    private static final File THEME_DIR = new File("config/playerlist/themes");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<CustomTheme> themes = new ArrayList<>();

    static {
        loadThemes();
    }

    public static List<CustomTheme> getThemes() {
        return new ArrayList<>(themes);
    }

    public static void saveTheme(CustomTheme theme) {
        try {
            if (!THEME_DIR.exists()) THEME_DIR.mkdirs();
            File file = new File(THEME_DIR, theme.name + ".json");
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(theme, writer);
            }
            loadThemes(); // refresh list after saving
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeTheme(String name) {
        File file = new File(THEME_DIR, name + ".json");
        if (file.exists()) {
            if (!file.delete()) {
                System.err.println("Failed to delete theme file: " + file.getAbsolutePath());
            }
        }
        loadThemes();
    }

    public static void loadThemes() {
        themes.clear();
        if (!THEME_DIR.exists()) return;

        File[] files = THEME_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                CustomTheme theme = GSON.fromJson(reader, CustomTheme.class);
                if (theme != null && theme.name != null) {
                    themes.add(theme);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
