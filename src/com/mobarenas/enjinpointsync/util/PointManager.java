package com.mobarenas.enjinpointsync.util;

import com.mobarenas.enjinpointsync.EnjinPointSync;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PointManager {

    public void addPoints(Player player, int points) {
        EnjinPointSync.getInstance().getMAPI().addPoints(player, points, false);
    }

    public void setWebsitePoints(String name, UUID uuid) {
        EnjinPointSync.getInstance().getTask().asyncEnjinPointsUpdate(name, uuid);
    }
}
