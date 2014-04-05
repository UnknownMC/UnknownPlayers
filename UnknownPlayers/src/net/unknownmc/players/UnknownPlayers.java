package net.unknownmc.players;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;
import com.mojang.api.profiles.ProfileCriteria;

public class UnknownPlayers extends JavaPlugin {
	
	public static FileConfiguration config;
	public static Logger log;
	BukkitTask task;
	public static File folder;
	public static final HttpProfileRepository repository = new HttpProfileRepository();
	public static HashMap<String, UUID> uuids;
	
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
		if (config.getInt("config.version") < 3) {
			convertToUUID();
			config.set("config.version", 3);
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
	
	public void convertToUUID() {
		log.info("Converting all old player files to use the UUIDs, this one time process will delay the start up time this one time!");
		for (File fl : folder.listFiles()) {
			if (fl.isFile()) {
				String name = fl.getName();
				name = name.substring(0, name.length()-4); // trim the ".yml"
				UUID uuid = UUID.fromString(""); // TODO
				File targ = new File(fl.getParentFile(), uuid.toString());
				log.info("Renaming " + fl.getName() + " to " + targ.getName() + " (" + targ.getParentFile().getAbsolutePath() + ").");
				fl.renameTo(targ);
				FileConfiguration stats = YamlConfiguration.loadConfiguration(targ);
				List<?> namesO = stats.getList("known-names");
				List<String> names = new ArrayList<String>();
				for (Object o : namesO) {
					names.add(o.toString());
				}
				if (!names.contains(name)) {
					names.add(name);
				}
				stats.set("known-names", names);
				try {
					stats.save(targ);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
	
	/**
	 * Get an offline player's UUID
	 * MUST BE RUN ASYNCHRONOUSLY!
	 * @param name The username
	 * @return The UUID
	 */
	public static UUID getUUID(String name) {
		 Profile[] profile = repository.findProfilesByCriteria(new ProfileCriteria(name, "minecraft"));
		 if (profile.length != 1) {
			 String uuids = "";
			 for (Profile pr : profile) {
				 if (uuids.length() != 0) {
					 uuids = uuids + ", ";
				 }
				 uuids = uuids + pr.getId();
			 }
			 log.severe(name + " has " + profile.length + " UUIDs, expecting 1! " + uuids);
			 return null;
		 }
		 String uuid = "";
		 for (Profile pr : profile) {
			 uuid = pr.getId();
		 }
		 return UUID.fromString(uuid);
	}
	
	/**
	 * Get an online player's UUID
	 * Thread-safe.
	 * @param player Player
	 * @return The UUID
	 */
	public static UUID getUUID(Player player) {
		return uuids.get(player.getName());
	}
}