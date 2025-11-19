package jp.sensei.playerlist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PlayerList implements ClientModInitializer {
    public static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        PlayerListConfig.load();
        ThemeStorage.loadThemes();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.playerlist.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category.playerlist"
        ));

        PlayerTracker.init();
        RadarRenderer.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.wasPressed() && client.currentScreen == null) {
                client.setScreen(new PlayerListScreen());
            }
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (PlayerListConfig.config.minimapEnabled) {
                RadarRenderer.render(context, true);
            }
        });
    }
}
