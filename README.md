AuthMeCacheFix
==============

AuthMeCacheFix is a lightweight helper plugin for offline-mode Minecraft servers
using AuthMe Reloaded.

It fixes a known cache inconsistency issue where players registered by an
external system (website, IAM service, etc.) are not immediately recognized
by AuthMe unless the server is restarted.


Problem
-------

AuthMe Reloaded uses an internal cache layer (CacheDataSource) to store player
authentication data.

When a player joins the server while unregistered, AuthMe may cache an
Optional.empty() result for that username. If the player is later registered
externally (direct database insert), AuthMe can continue to treat the player
as unregistered until the cache expires or the server restarts.

This results in players being incorrectly prompted to use /register even
though their data already exists in the database.


Solution
--------

AuthMeCacheFix ensures that AuthMe re-queries the database at login time by
combining two mechanisms:

1. Reflection-based cache invalidation (when available)
   - Directly invalidates the internal AuthMe cache entry for the player.
   - Forces the next lookup to read from the database.
   - Provides immediate consistency.

2. API-based soft refresh (fallback)
   - Calls AuthMeApi.isRegistered(playerName) during pre-login.
   - Triggers AuthMe's normal cache reload behavior.
   - Ensures eventual consistency even if reflection is unavailable.

The reflection logic is optional and safely disabled if AuthMe internals
change in future versions.


How It Works
------------

On AsyncPlayerPreLoginEvent:

- Attempt to invalidate AuthMe's cache for the joining player via reflection.
- Regardless of reflection success, call AuthMeApi.isRegistered(name) as a
  safe fallback.

This guarantees that:
- External registrations are recognized immediately when possible.
- The plugin never blocks login or crashes the server.
- Compatibility is preserved across AuthMe updates.


Compatibility
-------------

- Minecraft: 1.20+
- AuthMe Reloaded: 5.6.x / 5.7.x
- Server mode: Offline-mode (online-mode=false)

The plugin depends on AuthMe and will automatically disable itself if AuthMe
is not present.


Installation
------------

1. Place AuthMeCacheFix.jar into the server's /plugins directory.
2. Ensure AuthMe Reloaded is installed and enabled.
3. Restart the server.

No configuration is required.


Commands
--------

(Optional, if enabled in plugin.yml)

  /authmecache <player>
    Manually invalidates the AuthMe cache for the specified player.


Safety and Design Notes
-----------------------

- No AuthMe source code is modified.
- Reflection is performed once during plugin startup.
- If reflection fails, the plugin automatically falls back to API-only behavior.
- No player data is altered by this plugin.
- Designed to be safe for long-term use.


License
-------

This plugin is provided as-is.
You are free to modify and adapt it for your own server.
