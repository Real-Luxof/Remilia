package com.luxof.remilia;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Side-agnostic.
 * <p>Use after ClientPlayConnectionEvents.INIT or ServerPlayConnectionEvents.INIT. */
public abstract class RemiliaAPI {
    public final HashMap<String, String> macros;

    protected RemiliaAPI(HashMap<String, String> macros) { this.macros = macros; }

    protected static RemiliaAPI INSTANCE = null;

    // this hooliganery is to keep stuff neatly organized
    /** contains methods to deal with setting or getting macros in the hex book. */
    public static class Macros {
        /** directly puts the value of a macro in the book, on the client.
         * Returns what was there before, or null if there was nothing there before. */
        public static String put(String key, @NotNull String value) {
            return RemiliaAPI.INSTANCE.macros.put(key, value);
        }
        public static HashMap<String, String> all() {
            return RemiliaAPI.INSTANCE.macros;
        }
        public static boolean exists(String key) {
            return RemiliaAPI.INSTANCE.macros.contains(key);
        }
        public static String get(String key) {
            return RemiliaAPI.INSTANCE.macros.get(key);
        }
    }
}
