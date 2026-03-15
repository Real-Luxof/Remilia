package com.luxof.remilia;

import static com.luxof.remilia.Remilia.LOGGER;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

public class RemiliaAPI {
    public final HashMap<String, String> macros;
    public final HashMap<String, Function<String, String>> methodCallingMacros;

    protected RemiliaAPI(
        HashMap<String, String> macros,
        HashMap<String, Function<String, String>> methodCallingMacros
    ) { this.macros = macros; this.methodCallingMacros = methodCallingMacros; }

    protected static RemiliaAPI INSTANCE = null;

    // this hooliganery is to keep stuff (as) neatly organized (as possible)
    /** contains methods to deal with setting or getting macros in the hex book.
     * <p>any changed macros on the server sync to the client every server tick.
     * <p>Use after assets have been loaded (so works in [SIDE]LifeCycleEvents.INIT). */
    public static class Macros {
        public static String put(String key, @NotNull String value) {
            return RemiliaAPI.INSTANCE.macros.put(key, value);
        }
        public static HashMap<String, String> all() {
            return RemiliaAPI.INSTANCE.macros;
        }
        public static boolean exists(String key) {
            return RemiliaAPI.INSTANCE.macros.containsKey(key);
        }
        public static String get(String key) {
            return RemiliaAPI.INSTANCE.macros.get(key);
        }
    }

    /** for automatically updating values between client and server.
     * <p>variables are client-specific.
     * <p>when UUID for putX is null then the variable is global when on
     * the server, but directed to the server when on the client.
     * <p>persistent state is used to keep variables across logins.
     * <p>conflicts are undefined behaviour. Don't Do That™
     * <p>the side that shares the variable is the one that updates it.
     * <p>every tick the values given by the suppliers of the current side's vars are checked.
     * if they have changed then an update packet is sent to the other side.
     * <p>any <code>Supplier</code>s received from here update automatically on that update packet.
     * Note that they return null if their variable doesn't exist
     * or it's value is a different type than it originally was.
     * <p>use cases: config, great spell discovered, etcetera. */
    public static class Sharing {

        // :devious:
        public static HashMap<UUID, HashMap<String, Supplier<Object>>> shared = new HashMap<>();
        public static HashMap<UUID, List<String>> ours = new HashMap<>();

        public static List<List<String>> getAllVarNames() {
            return shared.keySet()
                .stream()
                .map(Sharing::getAllVarNamesFor)
                .toList();
        }
        public static List<String> getAllVarNamesFor(UUID player) {
            return shared.get(player).keySet().stream().toList();
        }
        /** stops sharing a variable there.
         * <p>if "there" does not exist, log an error.
         * <p>if the variable does not exist, log an error.
         * <p>if the variable is not being shared there, log an error. */
        public static void discard(UUID player, String name) {
            String uuid = player == null ? "null UUID" : player.toString();
            var playerShared = shared.get(player);

            if (playerShared == null) {
                LOGGER.error("Not sharing any variables on " + uuid);
                return;
            }
            if (!playerShared.containsKey(name)) {
                LOGGER.error(uuid + " doesn't contain variable " + name);
                return;
            }

            playerShared.remove(name);
        }
        /** inputs null for the player. */
        public static void discard(String name) {
            discard(null, name);
        }

        private static void put(UUID p, String n, Supplier<? extends Object> v) {
            if (!shared.containsKey(p))
                shared.put(p, new HashMap<>());
            shared.get(p).put(n, () -> v.get());
        }
        private static <T extends Object> Supplier<T> get(
            UUID p,
            String n,
            Class<T> type
        ) {
            return () -> {
                var playerShared = shared.get(p);

                return playerShared == null
                    || !playerShared.containsKey(n)
                    || !type.isInstance(playerShared.get(n).get())
                    ? null
                    : type.cast(playerShared.get(n).get());
            };
        }

        public static void shareInt(UUID player, String name, Supplier<Integer> var) { put(player, name, var); }
        public static Supplier<Integer> getInt(UUID player, String name) { return get(player, name, Integer.TYPE); }
        /** shorthand for null UUID. */ public static void shareInt(String name, Supplier<Integer> var) { put(null, name, var); }
        /** shorthand for null UUID. */ public static Supplier<Integer> getInt(String name) { return getInt(null, name); }

        public static void shareDouble(UUID player, String name, Supplier<Double> var) { put(player, name, var); }
        public static Supplier<Double> getDouble(UUID player, String name) { return get(player, name, Double.TYPE); }
        /** shorthand for null UUID. */ public static void shareDouble(String name, Supplier<Double> var) { put(null, name, var); }
        /** shorthand for null UUID. */ public static Supplier<Double> getDouble(String name) { return getDouble(null, name); }

        public static void shareBool(UUID player, String name, Supplier<Boolean> var) { put(player, name, var); }
        public static Supplier<Boolean> getBool(UUID player, String name) { return get(player, name, Boolean.TYPE); }
        /** shorthand for null UUID. */ public static void shareBool(String name, Supplier<Boolean> var) { put(null, name, var); }
        /** shorthand for null UUID. */ public static Supplier<Boolean> getBool(String name) { return getBool(null, name); }

        public static void shareString(UUID player, String name, Supplier<String> var) { put(player, name, var); }
        public static Supplier<String> getString(UUID player, String name) { return get(player, name, String.class); }
        /** shorthand for null UUID. */ public static void shareString(String name, Supplier<Boolean> var) { put(null, name, var); }
        /** shorthand for null UUID. */ public static Supplier<String> getString(String name) { return getString(null, name); }
    }

    public static class MethodCallingMacros {
        public static Function<String, String> put(
            String key,
            @NotNull Function<String, String> value
        ) {
            return RemiliaAPI.INSTANCE.methodCallingMacros.put(key, value);
        }
        public static HashMap<String, Function<String, String>> all() {
            return RemiliaAPI.INSTANCE.methodCallingMacros;
        }
        public static boolean exists(String key) {
            return RemiliaAPI.INSTANCE.methodCallingMacros.containsKey(key);
        }
        public static Function<String, String> get(String key) {
            return RemiliaAPI.INSTANCE.methodCallingMacros.get(key);
        }
    }
}
