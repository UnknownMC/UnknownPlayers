package net.unknownmc.players;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;

public class UnknownPlayers extends JavaPlugin {
	
	public static FileConfiguration config;
	public static Logger log;
	BukkitTask task;
	public static File folder;
	public static final HttpProfileRepository repository = new HttpProfileRepository("minecraft");
	public static HashMap<String, String> uuids;
	
	public void onEnable() {
		getCommand("player").setExecutor(new CommandsHandler());
		getCommand("demote").setExecutor(new CommandsHandler());
		uuids = new HashMap<String, String>();
				
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
		for (Player pl : Bukkit.getOnlinePlayers()) {
			Profile[] profile = UnknownPlayers.repository.findProfilesByCriteria(pl.getName());
			if (profile.length != 1) {
				pl.kickPlayer(ChatColor.RED + "Error connecting to server: couldn't get your UUID\nAre Mojang's login servers down?");
				continue;
			}
			String uuid = null;
			for (Profile pr : profile) { // Loop through it even though there's only one entry
				uuid = pr.getId();
			}
			if (uuid == null) {
				pl.kickPlayer(ChatColor.RED + "Error connecting to server: couldn't get your UUID\nAre Mojang's login servers down?");
				continue;
			}
			uuids.put(pl.getName(), uuid);
			Playtime play = new Playtime(UnknownPlayers.getUUID(pl)); // This generates file if not found
			play.resetLastJoinTime(); // This sets lastjoin
			play.addName(pl.getName());
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
				System.out.println(name);
				name = name.substring(0, name.length()-4); // trim the ".yml"
				System.out.println(name);
				String uuid = getUUID(name);
				if (uuid == null) {
					log.warning("Skipping " + name + ", UUID is null");
					continue;
				}
				File targ = new File(fl.getParentFile(), uuid + ".yml");
				log.info("Renaming " + fl.getName() + " to " + targ.getName() + " (" + targ.getParentFile().getAbsolutePath() + ").");
				fl.renameTo(targ);
				FileConfiguration stats = YamlConfiguration.loadConfiguration(targ);
				List<?> namesO = stats.getList("known-names");
				List<String> names = new ArrayList<String>();
				if (namesO != null) {
					if (namesO.size() > 0) {
						for (Object o : namesO) {
							names.add(o.toString());
						}
					}
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
			Playtime play = new Playtime(getUUID(pl));
			play.saveStatistics();
		}
		uuids.clear();
	}
	
	/**
	 * Get an offline player's UUID
	 * MUST BE RUN ASYNCHRONOUSLY!
	 * @param name The username
	 * @return The UUID
	 */
	public static String getUUID(String name) {
		 Profile[] profile = repository.findProfilesByCriteria(name);
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
		 return uuid;
	}
	
	/**
	 * Get an online player's UUID
	 * Thread-safe.
	 * @param player Player
	 * @return The UUID
	 */
	public static String getUUID(Player player) {
		return uuids.get(player.getName());
	}
}