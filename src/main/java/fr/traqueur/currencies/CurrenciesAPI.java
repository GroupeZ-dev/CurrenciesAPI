package fr.traqueur.currencies;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class CurrenciesAPI {

    private static volatile Plugin plugin;

    private CurrenciesAPI() {
    }

    /**
     * Register the plugin instance used to schedule work on the main server thread.
     *
     * @param owningPlugin The plugin instance, must not be null.
     */
    public static void init(@NotNull Plugin owningPlugin) {
        if (owningPlugin == null) {
            throw new IllegalArgumentException("The plugin instance cannot be null.");
        }
        if (plugin != null) {
            return;
        }
        plugin = owningPlugin;
    }

    /**
     * @return The registered plugin instance, or null when {@link #init(Plugin)} was never called.
     */
    @Nullable
    public static Plugin getPlugin() {
        return plugin;
    }

    /**
     * Whether the caller is on the main server thread.
     *
     * @return True when the caller is running on the main server thread.
     */
    static boolean isMainThread() {
        try {
            return Bukkit.isPrimaryThread();
        } catch (Throwable throwable) {
            return false;
        }
    }
}
