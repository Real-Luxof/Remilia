package com.luxof.remilia.client;

import com.luxof.remilia.RemiliaAPI;
import com.luxof.remilia.RemiliaLoader;

import static com.luxof.remilia.Remilia.id;
import static com.luxof.remilia.Remilia.readAndExecuteThisOnVars;

import net.fabricmc.api.ClientModInitializer;

import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;

import java.util.HashMap;

public class RemiliaClient implements ClientModInitializer {

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
    }

    public void registerSharingPacketReceivers() {
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
