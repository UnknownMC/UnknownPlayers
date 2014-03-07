package net.unknownmc.players;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
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
    			Playtime play = new Playtime(pl.getName());
				long playtime = play.getPlayTime();
				long current = System.currentTimeMillis() - play.getLastJoinTime();
				playtime = playtime + current;
				long reqtime = UnknownPlayers.config.getLong("rankup.time") * 60 * 1000; //Time is in minutes. Turn it into seconds, then millis
				if (playtime >= reqtime) {
					String group = UnknownPlayers.config.getString("rankup.group");
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mansave");
					String path = "users." + pl + ".group";
					File gmDir = new File(Bukkit.getServer().getPluginManager().getPlugin("GroupManager").getDataFolder(), File.separator + "worlds" + File.separator + UnknownPlayers.config.getString("default-world"));
					File users = new File(gmDir, "users.yml");
					YamlConfiguration usrs;
					if (!users.isFile()) {
						UnknownPlayers.log.severe(users.getAbsolutePath() + " doesn't exist!");
						break;
					} else {
						usrs = YamlConfiguration.loadConfiguration(users);
					}
					if (!usrs.isString(path)) {
						break;
					}
					
					usrs.set(path, group);
					try {
						usrs.save(users);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "manload");
					UnknownPlayers.log.info("Promoting " + pl.getName() + " to Player.");
				}
    		}
    	}
    }
}
