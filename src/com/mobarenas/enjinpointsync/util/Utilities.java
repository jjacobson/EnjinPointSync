package com.mobarenas.enjinpointsync.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Utilities {

    public void Announce(String message) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

    }

}
