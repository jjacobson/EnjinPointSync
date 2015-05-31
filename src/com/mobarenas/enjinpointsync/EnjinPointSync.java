package com.mobarenas.enjinpointsync;


import com.mobarenas.enjinpointsync.commands.SyncPoints;
import com.mobarenas.enjinpointsync.commands.AwardAnnounce;
import com.mobarenas.enjinpointsync.listeners.PlayerListeners;
import com.mobarenas.enjinpointsync.util.Bugger;
import com.mobarenas.enjinpointsync.util.PointManager;
import com.mobarenas.enjinpointsync.util.PushTask;
import com.mobarenas.enjinpointsync.util.Utilities;
import com.puzlinc.lolmewn.mobarena.api.MobArenaApi;
import com.puzlinc.lolmewn.mobarena.lobby.MainLobby;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class EnjinPointSync extends JavaPlugin {

    public static EnjinPointSync instance;
    Map<UUID, Integer> queue;
    private PointManager points;
    private Utilities util;
    private PushTask task;
    private MobArenaApi API;
    private MainLobby lobby;
    private Bugger bugger;
    private boolean hasChanged = false;

    public static EnjinPointSync getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        points = new PointManager();
        util = new Utilities();
        bugger = new Bugger();
        getCommand("syncpoints").setExecutor(new SyncPoints());
        getCommand("awardannounce").setExecutor(new AwardAnnounce());
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        if (Bukkit.getPluginManager().getPlugin("MobArena-Lobby") == null) {
            log(Level.SEVERE, "Could not find MobArena-Lobby, disabling plugin!");
            this.setEnabled(false);
        }
        API = getServer().getServicesManager().getRegistration(MobArenaApi.class).getProvider();
        lobby = (MainLobby) Bukkit.getServer().getPluginManager().getPlugin("MobArena-Lobby");
        try {
            task = new PushTask();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // load queue
        queue = new HashMap<>();
        if (getConfig().getConfigurationSection("UUID") != null) {
            Set<String> set = getConfig().getConfigurationSection("UUID").getKeys(false);
            for (String uuid : set) {
                int value = getConfig().getConfigurationSection("UUID").getInt(uuid);
                queue.put(UUID.fromString(uuid), value);
            }
            saveConfig();
        }

    }

    @Override
    public void onDisable() {
        writeData();
    }

    // save player point queue
    public void writeData() {
        for (UUID player : queue.keySet()) {
            int value = queue.get(player);
            getConfig().set("UUID." + player, value);
        }
        saveConfig();
        log("Wrote users to file");
    }

    public PointManager getPointManager() {
        return points;
    }

    public MobArenaApi getMAPI() {
        return API;
    }

    public MainLobby getLobby() {
        return lobby;
    }

    public Utilities getUtil() {
        return util;
    }

    public PushTask getTask() {
        return task;
    }

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    public void log(String message) {
        log(Level.INFO, message);

    }

    public Map<UUID, Integer> getQueue() {
        return queue;
    }

    public void setChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public Bugger getBugger() {
        return bugger;
    }

}
