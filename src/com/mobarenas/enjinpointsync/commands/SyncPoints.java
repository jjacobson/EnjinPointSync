package com.mobarenas.enjinpointsync.commands;

import com.mobarenas.enjinpointsync.EnjinPointSync;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SyncPoints implements CommandExecutor {

    private EnjinPointSync instance;

    public SyncPoints() {
        instance = EnjinPointSync.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
        if (!(sender instanceof ConsoleCommandSender))
            return false;

        if (args.length != 2) {
            return false;
        }

        if (Bukkit.getServer().getPlayerExact(args[0]) == null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
            if (!op.hasPlayedBefore()) {
                sender.sendMessage("Error: player has never played before");
                return true;
            }
            instance.getMAPI().addOfflinePoints(op, Integer.parseInt(args[1]));
            instance.getPointManager().setWebsitePoints(op.getName(), op.getUniqueId());
        } else {
            Player player = Bukkit.getServer().getPlayerExact(args[0]);
            instance.getPointManager().addPoints(player, Integer.parseInt(args[1]));
            instance.getLobby().getBoardHelper().updateLobbyBoard(player);
            instance.getPointManager().setWebsitePoints(player.getName(), player.getUniqueId());
            instance.log("Added " + args[1] + " points to " + args[0]);
        }
        return true;
    }
}