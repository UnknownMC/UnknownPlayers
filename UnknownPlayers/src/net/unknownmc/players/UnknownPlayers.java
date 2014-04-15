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
			log.info("Converting all old player files to use the UUIDs, this one time process will delay the start up time this one time!");
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
		List<String> names = new ArrayList<String>();
		int processed = 0;
		for (File fl : folder.listFiles()) {
			if (!fl.isFile()) {
				continue;
			}
			if (fl.getName().startsWith("uuid-")) {
				continue;
			}
			String name = fl.getName();
			if (name.length() < 5) {
				continue;
			}
			name = name.substring(0, name.length()-4); // trim the ".yml"
			names.add(name);
			processed++;
			if (processed >= 100) {
				break;
			}
		}
		if (processed == 0) {
			log.info("Conversion done, no more files to convert!");
			return;
		}
		System.out.println("DEBUG: " + processed + " about to process");
		log.info("Asking Mojang for 100 players' UUIDS, might take a while?");
		Profile[] profiles = repository.findProfilesByCriteria(names.toArray(new String[names.size()]));
		log.info("Got a response from Mojang, starting renaming of the 100 batch");
		System.out.println("DEBUG: " + profiles.length + " profiles from Mojang");
		if (profiles.length != processed) {
			log.info("Couldn't get all UUIDs, asked for " + processed + " but only got " + profiles.length);
			log.info("You can easily repeat the conversion process by setting version to 2 in the config");
		}
		for (Profile pr : profiles) {
			if (pr.getId() == null) {
				log.warning("Skipping " + pr.getName() + ", UUID is null");
				continue;
			}
			File in = new File(folder, pr.getName().toLowerCase() + ".yml");
			File out = new File(folder, "uuid-" + pr.getId() + ".yml");
			log.info("About to rename " + in.getName() + " to " + out.getName());
			if (!in.renameTo(out)) {
				log.warning("Failed to rename " + in.getAbsolutePath() + " to " + out.getAbsolutePath());
				continue;
			}
			// Now add the known names
			FileConfiguration stats = YamlConfiguration.loadConfiguration(out);
			List<?> namesO = stats.getList("known-names");
			List<String> knames = new ArrayList<String>();
			if (namesO != null) {
				if (namesO.size() > 0) {
					for (Object o : namesO) {
						knames.add(o.toString());
					}
				}
			}
			if (!names.contains(pr.getName())) {
				knames.add(pr.getName());
				stats.set("known-names", knames);
				try {
					stats.save(out);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		convertToUUID(); // No stackoverflow pl0x, we have to do this for every 100 players... so with 5k players, that's 50 times :(
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