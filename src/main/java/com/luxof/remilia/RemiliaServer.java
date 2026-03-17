package com.luxof.remilia;

import static com.luxof.remilia.Remilia.id;
import static com.luxof.remilia.Remilia.readAndExecuteThisOnVars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver;

public class RemiliaServer {

    public static void initializeServerSideStuff() {
        RemiliaLoader.registerServer();
        initPacketReceivers();
    }

    public static HashMap<UUID, ArrayList<String>> notOurGlobals = new HashMap<>();

    public static void initPacketReceivers() {
        registerGlobalReceiver(
            id("you_just_got_deleted_buddy"),
            (server, player, networkHandler, packet, sender) -> {

                // well i gotta track global variables sent by players *somehow*
                UUID uuid = player.getUuid();
                if (!notOurGlobals.containsKey(uuid)) return;

                ArrayList<String> selectedGlobals = notOurGlobals.get(uuid);
                var globals = RemiliaAPI.Sharing.shared.get(null);

                for (String varName : selectedGlobals) {
                    globals.remove(varName);
                }

                notOurGlobals.remove(player.getUuid());

            }
        );

        registerGlobalReceiver(
            id("your_global_vars_just_got_deleted_buddy"),
            (server, player, networkHandler, packet, sender) -> {

                ArrayList<String> globalsFromPlayer = notOurGlobals.get(player.getUuid());
                var globals = RemiliaAPI.Sharing.shared.get(null);

                packet.readList(buf -> buf.readString()).forEach(varName -> {
                    globalsFromPlayer.remove(varName);
                    globals.remove(varName);
                });

            }
        );

        // id("your_vars_just_got_deleted_buddy") can never happen to the server

        registerGlobalReceiver(
            id("changed_global_vars"),
            (server, player, networkHandler, packet, sender) -> {

                UUID uuid = player.getUuid();
                if (!notOurGlobals.containsKey(uuid))
                    notOurGlobals.put(uuid, new ArrayList<>());
                ArrayList<String> globalsFromPlayer = notOurGlobals.get(uuid);

                var shared = RemiliaAPI.Sharing.shared;
                if (!shared.containsKey(null)) {
                    shared.put(null, new HashMap<>());
                }
                var globals = shared.get(null);

                readAndExecuteThisOnVars(
                    packet,
                    (varName, value) -> {
                        globalsFromPlayer.add(varName);
                        globals.put(varName, () -> value);
                    }
                );

            }
        );

        registerGlobalReceiver(
            id("changed_vars"),
            (server, player, networkHandler, packet, sender) -> {

                UUID uuid = player.getUuid();
                var shared = RemiliaAPI.Sharing.shared;
                if (!shared.containsKey(uuid))
                    shared.put(uuid, new HashMap<>());

                var variables = shared.get(uuid);

                readAndExecuteThisOnVars(
                    packet,
                    (varName, value) -> variables.put(varName, () -> value)
                );

            }
        );
    }
}
