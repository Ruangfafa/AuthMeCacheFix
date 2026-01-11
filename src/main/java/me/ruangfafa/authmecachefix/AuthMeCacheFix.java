package me.ruangfafa.authmecachefix;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AuthMeCacheFix extends JavaPlugin implements Listener {

    private AuthMeApi authMeApi;
    private Method invalidateMethod;
    private Object cacheDataSource;

    @Override
    public void onEnable() {
        Plugin authMe = Bukkit.getPluginManager().getPlugin("AuthMe");
        if (authMe == null) {
            getLogger().severe("AuthMe not found! Disabling AuthMeCacheFix.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        authMeApi = AuthMeApi.getInstance();

        // === 反射准备 ===
        try {
            Field databaseField = authMe.getClass().getDeclaredField("database");
            databaseField.setAccessible(true);
            Object dataSource = databaseField.get(authMe);

            // CacheDataSource 在 AuthMe 内部包里，用反射拿方法
            invalidateMethod = dataSource.getClass()
                    .getMethod("invalidateCache", String.class);
            cacheDataSource = dataSource;

            getLogger().info("AuthMe cache reflection invalidate ENABLED.");
        } catch (Exception e) {
            getLogger().warning("Reflection invalidate unavailable, fallback to soft refresh only.");
            invalidateMethod = null;
            cacheDataSource = null;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AuthMeCacheFix enabled.");
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();

        //强制失效缓存
        if (invalidateMethod != null) {
            try {
                invalidateMethod.invoke(cacheDataSource, name.toLowerCase());
            } catch (Exception ignored) {
                // 反射失败不影响登录
            }
        }

        //软刷新兜底
        try {
            authMeApi.isRegistered(name);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AuthMeCacheFix disabled.");
    }
}
