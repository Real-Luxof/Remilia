package com.luxof.remilia.client;

import com.luxof.remilia.Remilia;
import com.luxof.remilia.RemiliaAPI;
import com.luxof.remilia.RemiliaLoader;

import static com.luxof.remilia.Remilia.LOGGER;
import static com.luxof.remilia.Remilia.id;
import static com.luxof.remilia.Remilia.readAndExecuteThisOnVars;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;

import java.util.HashMap;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

public class RemiliaClient implements ClientModInitializer {

    public static boolean dontRenderPatterns = false;
    public static KeyBinding toggleDontRenderPatterns = KeyBindingHelper.registerKeyBinding(
        new KeyBinding(
            "keys.remilia.togglepatternsrender",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_ALT,
            "key_category.remilia"
        )
    );
    private static HashMap<UUID, HashMap<String, Object>> prevShared = new HashMap<>();

    @Override
    public void onInitializeClient() {
        RemiliaLoader.registerClient();

        registerGlobalReceiver(
            id("macro_order_from_server"),
            (client, networkHandler, packet, sender) -> {
                int changes = packet.readInt();

                for (int i = 0; i < changes; i++) {
                    RemiliaAPI.Macros.put(packet.readString(), packet.readString());
                }
            }
        );

        registerSharingPacketReceivers();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            Remilia.shareVars(
                prevShared,
                (uuid, id, packet) -> LOGGER.error("CLIENT just tried to sync a variable to NON-NULL UUID " + uuid.toString() + ". What are you doing?"),
                ClientPlayNetworking::send
            );
        });

        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        ClientTickEvents.START_WORLD_TICK.register(world -> {
            RemiliaAPI.Macros.put(
                "$(rainbowClient)",
                "$(#" + pad(world.getRandom().nextInt(0xffffff)) + ")"
            );
        });
    }

    private static String pad(int code) {
        String str = String.valueOf(Integer.toHexString(code));
        while (str.length() < 6) {
            str = "0" + str;
        }
        return str;
    }

    private static void registerSharingPacketReceivers() {
        registerGlobalReceiver(
            id("your_global_just_got_deleted_buddy"),
            (client, networkHandler, packet, sender) -> {
                RemiliaAPI.Sharing.shared.remove(null);
            }
        );

        registerGlobalReceiver(
            id("you_just_got_deleted_buddy"),
            (client, networkHandler, packet, sender) -> {
                RemiliaAPI.Sharing.shared.remove(null);
            }
        );

        registerGlobalReceiver(
            id("your_global_vars_just_got_deleted_buddy"),
            (client, networkhandler, packet, sender) -> {
                packet.readList((buf) -> buf.readString())
                    .forEach(RemiliaAPI.Sharing.shared.get(null)::remove);
            }
        );

        registerGlobalReceiver(
            id("your_vars_just_got_deleted_buddy"),
            (client, networkhandler, packet, sender) -> {
                packet.readList((buf) -> buf.readString())
                    .forEach(RemiliaAPI.Sharing.shared.get(null)::remove);
            }
        );

        registerGlobalReceiver(
            id("changed_global_vars"),
            (client, networkHandler, packet, sender) -> {
                var shared = RemiliaAPI.Sharing.shared;
                if (!shared.containsKey(null))
                    shared.put(null, new HashMap<>());

                var globals = shared.get(null);

                readAndExecuteThisOnVars(
                    packet,
                    (varName, value) -> globals.put(varName, () -> value)
                );
            }
        );

        registerGlobalReceiver(
            id("changed_vars"),
            (client, networkHandler, packet, sender) -> {
                var shared = RemiliaAPI.Sharing.shared;
                if (!shared.containsKey(null))
                    shared.put(null, new HashMap<>());

                var globals = shared.get(null);

                readAndExecuteThisOnVars(
                    packet,
                    (varName, value) -> globals.put(varName, () -> value)
                );
            }
        );
    }
}
