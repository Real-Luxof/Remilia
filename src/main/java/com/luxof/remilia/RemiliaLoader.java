package com.luxof.remilia;

import com.luxof.remilia.client.RemiliaClientAPI;

import static com.luxof.remilia.Remilia.LOGGER;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

// I saw what Patchouli did...
// I don't trust it.
public class RemiliaLoader {
    public static boolean loadedAlready = false;

    private static void err(String mod, String reason) {
        LOGGER.error("Remilia could not parse book.json belonging to " + mod + ": " + reason);
    }
    private static void err(String mod, Throwable t) {
        LOGGER.error("Remilia errored parsing book.json belonging to " + mod, t);
    }

    public static void reload(ResourceManager manager, boolean client) {
        if (loadedAlready) return;
        LOGGER.info("Remilia loading...");

        Map<Identifier, Resource> resources = manager.findResources(
            "remilia",
            id -> id.getPath().equals("remilia/book.json")
        );

        HashMap<String, String> macros = new HashMap<>();

        for (var entry : resources.entrySet()) {
            String mod = entry.getKey().getNamespace();

            if (mod == "remilia" && !FabricLoader.getInstance().isDevelopmentEnvironment())
                continue;

            Resource given = entry.getValue();

            JsonObject resource;
            try {

                JsonElement file = JsonParser.parseReader(given.getReader());
                if (!file.isJsonObject()) {
                    err(mod, "file was not an Object.");
                    continue;
                }
                resource = file.getAsJsonObject();

            } catch (IOException e) {
                err(mod, e);
                continue;
            }

            if (!(resource.get("macros") instanceof JsonObject macrosObj)) {
                err(mod, "\'macros\' is not an Object.");
                continue;
            }

            for (String macroKey : macrosObj.keySet()) {
                try {
                    macros.put(macroKey, macrosObj.get(macroKey).getAsString());
                } catch (Exception e) {
                    err(mod, "\'macros\' Object's values are not all strings.");
                }
            }

            Book book = BookRegistry.INSTANCE.books.get(
                new Identifier("hexcasting", "thehexbook")
            );
            book.macros.putAll(macros);
            macros = (HashMap<String, String>)book.macros;
            RemiliaAPI.INSTANCE = new RemiliaAPI(macros);
            loadedAlready = true;
        }
    }

    public static void registerClient() {
        ClientPlayConnectionEvents.INIT.register(
            (networkHandler, client) -> reload(client.getResourceManager(), true)
        );
    }

    public static void registerServer() {
        ServerPlayConnectionEvents.INIT.register(
            (networkHandler, server) -> reload(server.getResourceManager(), false)
        );
    }
}
