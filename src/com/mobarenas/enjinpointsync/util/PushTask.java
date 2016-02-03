package com.mobarenas.enjinpointsync.util;

import com.mobarenas.enjinpointsync.EnjinPointSync;
import com.puzlinc.lolmewn.mobarena.api.MobArenaApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public class PushTask {

    private final String privateKey;
    private final URL domain;
    private Map<UUID, Integer> points = new HashMap<>();
    private MobArenaApi API;

    public PushTask() throws MalformedURLException {
        API = EnjinPointSync.getInstance().getMAPI();
        privateKey = EnjinPointSync.getInstance().getConfig().getString("private-key");
        domain = new URL(EnjinPointSync.getInstance().getConfig().getString("domain"));
        createTask();
    }

    public void createTask() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(EnjinPointSync.getInstance(), () -> {
            // Periodically remove non online players from the list. Considering the large amount of relogs,
            // this will be more efficient than listing on quit/kick (although it is negligible)
            List<UUID> keysToRemove = new ArrayList<>();
            for (UUID id : points.keySet()) {
                if (Bukkit.getServer().getPlayer(id) == null)
                    keysToRemove.add(id);
            }
            // Avoid CME
            for (UUID key : keysToRemove) {
                points.remove(key);
            }

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                asyncEnjinPointsUpdate(player.getName(), player.getUniqueId());
            }
        }, 20L, EnjinPointSync.getInstance().getConfig().getLong("polling-time") * 20L);
    }

    public void asyncEnjinPointsUpdate(final String name, final UUID uuid) {
        // return if player is not online (we cannot get their points if so)
        if (Bukkit.getServer().getPlayerExact(name) == null)
            return;

        // fetch points from the API
        final int playerPoints = API.getPoints(Bukkit.getServer().getPlayerExact(name));

        // If they have been previously synced we will check if any changes have been made. If they have, we will sync normally.
        // If not, we will add them to the list once we are done syncing them
        if (points.containsKey(uuid)) {
            // Player has been previously synced.
            if (points.get(uuid) == playerPoints) {
                return;
            }
        }
        Bukkit.getServer().getScheduler().runTaskAsynchronously(EnjinPointSync.getInstance(), () -> {
            JSONObject object = new JSONObject();
            JSONObject params = new JSONObject();
            params.put("api_key", privateKey);
            params.put("points", playerPoints);
            params.put("user_id", false);
            params.put("player", name);
            object.put("jsonrpc", "2.0");
            object.put("id", "100");
            object.put("params", params);
            object.put("method", "Points.set");
            try {
                HttpURLConnection conn = (HttpURLConnection) domain.openConnection();
                conn.setReadTimeout(6000);
                conn.setConnectTimeout(6000);
                conn.setDoOutput(true);
                conn.getOutputStream().write(object.toString().getBytes());
                StringBuilder sb = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine + "\n");
                }
                String response = sb.toString();
                if (response.contains("error")) {
                    if (response.contains("not linked")) {
                        EnjinPointSync.getInstance().getBugger().bug(name);
                    } else {
                        EnjinPointSync.getInstance().log(Level.SEVERE, "Error updating enjin points for " + name + ": " + response);
                    }
                }
                in.close();
                conn.disconnect();
            } catch (IOException e) {
                EnjinPointSync.getInstance().log(Level.INFO, "Exception while syncing " + name);
                e.printStackTrace();
            }
        });
        // Update the map to hold the new points number to prevent it from using the first number it was given
        points.put(uuid, playerPoints);
    }
}
