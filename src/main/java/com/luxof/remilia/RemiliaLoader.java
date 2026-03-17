package com.luxof.remilia;

import static com.luxof.remilia.Remilia.LOGGER;
import static com.luxof.remilia.Remilia.id;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

// I saw what Patchouli did...
// I don't trust it.
public class RemiliaLoader implements SimpleSynchronousResourceReloadListener {
    public static boolean loadedAlready = false;

    private static void err(String mod, String reason) {
        LOGGER.error("Remilia could not parse book.json belonging to " + mod + ": " + reason);
    }
    private static void err(String mod, Throwable t) {
        LOGGER.error("Remilia errored parsing book.json belonging to " + mod, t);
    }

    @Override
    public Identifier getFabricId() {
        return id("remilia_loader");
    }

    @Nullable
    private HashMap<String, String> loadMacros(
        String mod,
        JsonObject resource
    ) {
        if (!resource.has("macros")) return new HashMap<>();
        if (!(resource.get("macros") instanceof JsonObject macrosObj)) {
            err(mod, "'macros' is not an Object.");
            return null;
        }

        HashMap<String, String> macros = new HashMap<>();

        for (String macroKey : macrosObj.keySet()) {
            // i'm aware about the array thing
            try { macros.put(macroKey, macrosObj.get(macroKey).getAsString()); }
            catch (UnsupportedOperationException | IllegalStateException e) {
                err(mod, "\'macros\' Object's values are not all strings.");
                return null;
            }
        }

        return macros;
    }

    private HashMap<String, Function<String, String>> loadMethodCallingMacros(
        String mod,
        JsonObject resource
    ) {
        if (!resource.has("method_calling_macros")) return new HashMap<>();
        if (!(resource.get("method_calling_macros") instanceof JsonObject mcmObj)) {
            err(mod, "'method_calling_macros' is not an Object.");
            return null;
        }

        HashMap<String, Function<String, String>> mcms = new HashMap<>();

        for (String key : mcmObj.keySet()) {
            String methodPath;
            try { methodPath = mcmObj.get(key).getAsString(); }
            catch (UnsupportedOperationException | IllegalStateException e) {
                err(mod, "'method_calling_macros' Object's values are not all strings.");
                return null;
            }

            ArrayList<String> pack = new ArrayList<>(
                Arrays.asList(methodPath.split(Pattern.quote(".")))
            );

            String className = "";
            for (String p : pack.subList(0, pack.size() - 1)) {
                className += p + ".";
            }
            className = className.substring(0, className.length() - 1);

            Class<?> clazz;
            try { clazz = Class.forName(className); }
            catch (ClassNotFoundException e) {
                err(mod, className + " is a non-existent class.");
                return null;
            }

            String methodName = pack.get(pack.size() - 1);
            Method method;
            try { method = clazz.getMethod(methodName, String.class); }
            catch (NoSuchMethodException e) {
                err(mod, methodName + " is a non-existent method in class " + className + ".");
                return null;
            }

            if (method.getReturnType() != String.class) {
                err(mod, methodName + " of class " + className + "'s return type is not String.");
                return null;
            }

            try {
                if (!method.canAccess(null)) {
                    err(mod, methodName + " of class " + className + " is inaccessible by Remilia.");
                    return null;
                }
            } catch (IllegalArgumentException e) {
                err(mod, methodName + " of class " + className + " is not a static method.");
            }

            mcms.put(
                key,
                (String str) -> {
                    try { return (String)method.invoke(null, str); }
                    catch (InvocationTargetException e) {
                        LOGGER.error(
                            "Method-calling macro "
                            + key
                            + " pointing to "
                            + methodPath
                            + " of mod "
                            + mod
                            + " threw an exception. Returning empty string.",
                            e
                        );
                        return "";
                    } catch (IllegalAccessException e) {
                        LOGGER.error(
                            "Method-calling macro "
                            + key
                            + " point to "
                            + methodPath
                            + " of mod "
                            + mod
                            + " was inaccessible. Returning empty string.",
                            e
                        );
                        return "";
                    }
                }
            );
        }

        return mcms;
    }

    @Override
    public void reload(ResourceManager manager) {
        if (loadedAlready) return;
        LOGGER.info("Remilia loading...");

        Map<Identifier, Resource> resources = manager.findResources(
            "remilia",
            id -> id.getPath().equals("remilia/book.json")
        );

        HashMap<String, String> macros = new HashMap<>();
        HashMap<String, Function<String, String>> methodCallingMacros = new HashMap<>();

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

            try { macros.putAll(loadMacros(mod, resource)); }
            catch (NullPointerException npe) { continue; }

            try { methodCallingMacros.putAll(loadMethodCallingMacros(mod, resource)); }
            catch (NullPointerException npe) { continue; }
        }

        Book book = BookRegistry.INSTANCE.books.get(
            new Identifier("hexcasting", "thehexbook")
        );
        book.macros.putAll(macros);
        macros = (HashMap<String, String>)book.macros;
        RemiliaAPI.INSTANCE = new RemiliaAPI(macros, methodCallingMacros);
        loadedAlready = true;
    }

    public static void registerClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new RemiliaLoader()
        );
    }

    public static void registerServer() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
            new RemiliaLoader()
        );
    }
}
