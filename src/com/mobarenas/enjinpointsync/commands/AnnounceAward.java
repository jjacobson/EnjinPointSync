package com.mobarenas.enjinpointsync.commands;

import com.mobarenas.enjinpointsync.EnjinPointSync;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class AnnounceAward implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
        if (!(sender instanceof ConsoleCommandSender))
            return false;

        if (args.length < 2)
            return false;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i] + " ");
        }

        EnjinPointSync.getInstance().getUtil().Announce(sb.toString());
        return true;

    }

}
