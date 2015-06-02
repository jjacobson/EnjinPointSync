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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(EnjinPointSync.getInstance(), new Runnable() {
            public void run() {

                if (EnjinPointSync.getInstance().hasChanged()) {
                    EnjinPointSync.getInstance().writeData();
                    EnjinPointSync.getInstance().setChanged(false);
                }

                // Periodically remove non online players from the list. Considering the large amount of relogs,
                // this will be more efficient than listing on quit/kick (although it is negligible)
                for (UUID id : points.keySet()) {
                    if (!Bukkit.getServer().getPlayer(id).isOnline())
                        points.remove(id);
                }

                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    asyncEnjinPointsUpdate(player);
                }
            }

        }, 20L, EnjinPointSync.getInstance().getConfig().getLong("polling-time") * 20L);

    }

    public void asyncEnjinPointsUpdate(Player p) {

        final String player = p.getName();
        // return if player is not online (we cannot get their points if so)
        if (Bukkit.getServer().getPlayerExact(player) == null)
            return;

        // fetch points from the API
        final int playerPoints = API.getPoints(Bukkit.getServer().getPlayerExact(player));

        // If they have been previously synced we will check if any changes have been made. If they have, we will sync normally.
        // If not, we will add them to the list once we are done syncing them
        if (points.containsKey(p.getUniqueId())) {
            // Player has been previously synced.
            if (points.get(p.getUniqueId()) == playerPoints) {
                // Points have not been changed since last sync
                EnjinPointSync.getInstance().log(Level.INFO, "No changes detected for " + player + ". Continuing to the next player...");
                return;
            }
        }

        Bukkit.getServer().getScheduler().runTaskAsynchronously(EnjinPointSync.getInstance(), new Runnable() {

            @SuppressWarnings("unchecked")
            public void run() {
                JSONObject object = new JSONObject();
                JSONObject params = new JSONObject();
                params.put("api_key", privateKey);
                params.put("points", playerPoints);
                params.put("user_id", false);
                params.put("player", player);
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
                            EnjinPointSync.getInstance().log(Level.INFO, player + " has not linked their enjin account.");
                            EnjinPointSync.getInstance().getBugger().bug(player);
                        } else {
                            EnjinPointSync.getInstance().log(Level.SEVERE, "Error updating enjin points for " + player + ": " + response);
                        }
                    } else {
                        EnjinPointSync.getInstance().log("Successfully updated " + player + "'s points to: " + playerPoints);
                    }
                    in.close();
                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        // Update the map to hold the new points number to prevent it from using the first number it was given
        points.put(p.getUniqueId(), playerPoints);

    }

}
