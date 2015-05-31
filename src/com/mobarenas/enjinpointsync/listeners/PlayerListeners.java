package com.mobarenas.enjinpointsync.listeners;

import com.mobarenas.enjinpointsync.EnjinPointSync;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListeners implements Listener {

    private EnjinPointSync instance;

    public PlayerListeners() {
        instance = EnjinPointSync.getInstance();
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (instance.getQueue().containsKey(player.getUniqueId())) {
            final int points = instance.getQueue().get(player.getUniqueId());
            instance.log("Player " + player.getName() + " was found in the queue. Adding " + points + " points to profile.");
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
                public void run() {
                    if (player.isOnline()) {
                        instance.getPointManager().addPoints(player, points);
                        instance.getLobby().getScoreboard(player).update(instance.getLobby());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThank you for participating on our website! You have been granted &6" + points + " &apoints."));
                        instance.getQueue().remove(player.getUniqueId());
                        instance.getConfig().getConfigurationSection("UUID").set(player.getUniqueId().toString(), null);
                        instance.saveConfig();
                    } else {
                        instance.log("Player " + player.getName() + " has logged out before points were added to their profile.");

                    }
                }

            }, 200L);
        }
    }

}
