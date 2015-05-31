package com.mobarenas.enjinpointsync.commands;

import com.mobarenas.enjinpointsync.EnjinPointSync;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AddPoints implements CommandExecutor {

    private EnjinPointSync instance;

    public AddPoints() {
        instance = EnjinPointSync.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
        if (!(sender instanceof ConsoleCommandSender))
            return false;

        if (args.length != 2)
            return false;

        if (Bukkit.getServer().getPlayerExact(args[0]) == null) {
            EnjinPointSync.getInstance().setChanged(true);
            UUID playerID = Bukkit.getServer().getOfflinePlayer(args[0]).getUniqueId();
            if (instance.getQueue().containsKey(playerID)) {
                instance.getQueue().put(playerID, Integer.parseInt(args[1]) + instance.getQueue().get(playerID));
                instance.log("Could not add points to " + args[0] + ". Player was not online, updating queue value");
                return true;
            } else {
                instance.getQueue().put(playerID, Integer.parseInt(args[1]));
                instance.log("Could not add points to " + args[0] + ". Player was not online, adding to queue.");
                return true;
            }

        } else {
            Player player = Bukkit.getServer().getPlayerExact(args[0]);
            instance.getPointManager().addPoints(player, Integer.parseInt(args[1]));
            instance.getLobby().getScoreboard(player).update(instance.getLobby());
            instance.log("Added " + args[1] + " points to " + args[0]);
            return true;
        }
    }

}