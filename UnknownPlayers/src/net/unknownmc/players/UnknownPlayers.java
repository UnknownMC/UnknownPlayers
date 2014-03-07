package net.unknownmc.players;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class UnknownPlayers extends JavaPlugin {
	
	public static FileConfiguration config;
	public static Logger log;
	BukkitTask task;
	public static File folder;
	
	public void onEnable() {
		getCommand("player").setExecutor(new CommandsHandler());
		getCommand("demote").setExecutor(new CommandsHandler());
		
		System.out.println(getCommand("player").getExecutor() + "");
				
		folder = new File(getDataFolder(), "players/");
		if (!folder.exists()) {
			folder.getParentFile().mkdirs();
			folder.mkdir();
		}
		
		log = getLogger();
				
		config = Bukkit.getServer().getPluginManager().getPlugin("UnknownPlayers").getConfig();
		this.saveDefaultConfig();
		config.options().copyDefaults(true);
		this.saveConfig();
		
		Bukkit.getServer().getPluginManager().registerEvents(new EvtListener(), this);
		
		task = new PromotePlayers(this).runTaskTimer(this, 12000, 12000);
		
		if (config.getInt("config.version") < 2) {
			convertToLowercase();
			config.set("config.version", 2);
			this.saveConfig();
		}
		
	}
	
	public void convertToLowercase() {
		log.info("Converting all old player files to lowercase file names, prepare for some console spam if you have lots of players.");
		for (File fl : folder.listFiles()) {
			if (fl.isFile()) {
				File targ = new File(fl.getParentFile().getAbsolutePath(), fl.getName().toLowerCase());
				log.info("Renaming " + fl.getName() + " to " + targ.getName() + " (" + targ.getParentFile().getAbsolutePath() + ").");
				fl.renameTo(targ);
			}
		}
		log.info("Finished converting old player files to lowercase!");
	}
	
	public void onDisable() {
		task.cancel();
		
		//if editing, update EvtListener.leave()
		for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
			File folder = UnknownPlayers.folder;
			String name = pl.getName().toLowerCase() + ".yml";
			File confF = new File(folder, name);
			if (!confF.exists()) {
				UnknownPlayers.log.severe("What happened to playerdata/" + pl.getName().toLowerCase() + "?!");
				UnknownPlayers.log.severe("Discarding unsaved data!");
				return;
			}
			FileConfiguration stats = YamlConfiguration.loadConfiguration(confF);
			long oldPlaytime = stats.getLong("playtime");
			long join = stats.getLong("lastjoin");
			long currentSession = System.currentTimeMillis() - join;
			long newPlaytime = oldPlaytime + currentSession;
			stats.set("playtime", newPlaytime);
			try {
				stats.save(confF);
			} catch (IOException ioe) {
				UnknownPlayers.log.severe("Couldn't save " + pl.getName() + "'s playtime!");
				ioe.printStackTrace();
			}

		}
	}
}