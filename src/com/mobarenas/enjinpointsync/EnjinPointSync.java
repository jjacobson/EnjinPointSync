package com.mobarenas.enjinpointsync;


import com.mobarenas.enjinpointsync.commands.AwardAnnounce;
import com.mobarenas.enjinpointsync.commands.SyncPoints;
import com.mobarenas.enjinpointsync.util.Bugger;
import com.mobarenas.enjinpointsync.util.PointManager;
import com.mobarenas.enjinpointsync.util.PushTask;
import com.mobarenas.enjinpointsync.util.Utilities;
import com.puzlinc.lolmewn.mobarena.api.MobArenaApi;
import com.puzlinc.lolmewn.mobarena.lobby.MainLobby;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.MalformedURLException;
import java.util.logging.Level;

public class EnjinPointSync extends JavaPlugin {

    public static EnjinPointSync instance;
    private PointManager points;
    private Utilities util;
    private PushTask task;
    private MobArenaApi API;
    private MainLobby lobby;
    private Bugger bugger;

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

    public Bugger getBugger() {
        return bugger;
    }

}
