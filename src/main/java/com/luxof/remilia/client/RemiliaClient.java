package com.luxof.remilia.client;

import com.luxof.remilia.RemiliaAPI;
import com.luxof.remilia.RemiliaLoader;

import static com.luxof.remilia.Remilia.id;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RemiliaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RemiliaLoader.registerClient();

        ClientPlayNetworking.registerGlobalReceiver(
            id("macro_order_from_server"),
            (client, networkHandler, packet, sender) -> {
                int changes = packet.readInt();

                for (int i = 0; i < changes; i++) {
                    RemiliaAPI.INSTANCE.putMacro(packet.readString(), packet.readString());
                }
            }
        );
    }
}
