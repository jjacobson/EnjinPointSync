package com.mobarenas.enjinpointsync.util;

import com.mobarenas.enjinpointsync.EnjinPointSync;
import org.bukkit.entity.Player;

public class PointManager {

    public void addPoints(Player player, int points) {

        EnjinPointSync.getInstance().getMAPI().addRawPoints(player, points);

        setWebsitePoints(player.getName());
    }

    public void setWebsitePoints(String player) {
        EnjinPointSync.getInstance().getTask().asyncEnjinPointsUpdate(player);
    }

}
