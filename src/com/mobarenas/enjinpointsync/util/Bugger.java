package com.mobarenas.enjinpointsync.util;

import com.mobarenas.enjinpointsync.EnjinPointSync;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bugger implements Listener {

    private List<UUID> players = new ArrayList<>();

    public Bugger() {
        EnjinPointSync plugin = EnjinPointSync.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void bug(String name) {
        final Player player = Bukkit.getServer().getPlayer(name);

        if (!player.isOnline() || player == null)
            return;

        if (players.contains(player.getUniqueId()))
            return;

        players.add(player.getUniqueId());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSign up and add your Minecraft profile to our site for &e500 &apoints instantly! &ewww.mobarenas.com"));
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        players.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void playerKick(PlayerKickEvent event) {
        players.remove(event.getPlayer().getUniqueId());
    }

}
