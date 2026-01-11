package me.ruangfafa.authmecachefix;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AuthMeCacheFix
 *
 * Purpose:
 * Trigger a "soft refresh" of AuthMe cache on player pre-login,
 * so AuthMe will re-query database instead of using cached Optional.empty().
 *
 * Compatible with:
 * - AuthMe Reloaded 5.7.x
 * - Offline servers
 *
 * Design:
 * - Uses AuthMeApi only (no internal classes, no reflection)
 * - Relies on AuthMe's own cache reload behavior
 */
public class AuthMeCacheFix extends JavaPlugin implements Listener {

    private AuthMeApi authMeApi;

    @Override
    public void onEnable() {
        // Ensure AuthMe is loaded
        if (!Bukkit.getPluginManager().isPluginEnabled("AuthMe")) {
            getLogger().severe("AuthMe not found! Disabling AuthMeCacheFix.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Get AuthMe API singleton
        authMeApi = AuthMeApi.getInstance();

        // Register PreLogin listener
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("AuthMeCacheFix enabled (PreLogin soft refresh active).");
    }

    /**
     * PreLogin soft refresh:
     * This triggers AuthMe to re-check database instead of using cached Optional.empty().
     */
    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            // Trigger AuthMe cache reload logic
            authMeApi.isRegistered(event.getName());
        } catch (Exception e) {
            // Never block login because of this plugin
            getLogger().warning("Failed to soft-refresh AuthMe cache for "
                    + event.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AuthMeCacheFix disabled.");
    }
}
