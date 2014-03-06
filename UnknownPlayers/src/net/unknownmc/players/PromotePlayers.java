package net.unknownmc.players;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PromotePlayers extends BukkitRunnable {
	
	//UnknownPlayers main;
	
	public PromotePlayers (UnknownPlayers pl) {
		
	}
	
    @Override
    public void run() {
    	for (Player pl : Bukkit.getOnlinePlayers()) {
    		if (!pl.hasPermission("unknownmc.player")) {
    			File folder = UnknownPlayers.folder;
    			String name = pl.getName().toLowerCase() + ".yml";
    			File confF = new File(folder, name);
    			if (!confF.exists()) {
    				UnknownPlayers.log.severe(confF.getAbsolutePath() + " (" + pl.getName() + ") doesn't exist!");
    			} else {
    				FileConfiguration stats = YamlConfiguration.loadConfiguration(confF);
    				long playtime = stats.getLong("playtime");
    				long current = System.currentTimeMillis() - stats.getLong("lastjoin");
    				playtime = playtime + current;
    				long reqtime = UnknownPlayers.config.getLong("rankup.time") * 60 * 1000; //Time is in minutes. Turn it into seconds, then millis
    				if (playtime >= reqtime) {
    					String cmd = String.format(UnknownPlayers.config.getString("rankup.command"), pl.getName());
    					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
    					UnknownPlayers.log.info("Promoting " + pl.getName() + " to Player.");
    				}
    			}
    		}
    	}
    }
}
