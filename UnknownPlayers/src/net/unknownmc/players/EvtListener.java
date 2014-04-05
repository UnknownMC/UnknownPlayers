package net.unknownmc.players;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mojang.api.profiles.Profile;
import com.mojang.api.profiles.ProfileCriteria;

public class EvtListener implements Listener {
	
	//UnknownPlayers main;
	
	@EventHandler
	public void join (PlayerJoinEvent e) {
		File folder = UnknownPlayers.folder;
		String name = UnknownPlayers.getUUID(e.getPlayer()) + ".yml";
		File confF = new File(folder, name);
		boolean newbie = false;
		if (!confF.exists()) {
			try {
				confF.getParentFile().mkdirs();
				confF.createNewFile();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return;
			}
			newbie = true;
		}
		FileConfiguration stats = YamlConfiguration.loadConfiguration(confF);
		
		if (newbie) {
			stats.set("firstjoin", System.currentTimeMillis());
			stats.set("playtime", 0);
		}
		stats.set("lastjoin", System.currentTimeMillis());
		try {
			stats.save(confF);
		} catch (IOException ioe) {
			UnknownPlayers.log.severe("Failed to save player statistics of " + e.getPlayer().getName());
			ioe.printStackTrace();
		}
	}
	
	@EventHandler
	public void leave (PlayerQuitEvent e) {
		//if editing this, update UnknownPlayers.onDisable()
		File folder = UnknownPlayers.folder;
		String name = UnknownPlayers.getUUID(e.getPlayer()) + ".yml";
		File confF = new File(folder, name);
		if (!confF.exists()) {
			UnknownPlayers.log.severe("What happened to playerdata/" + name + "?!");
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
			UnknownPlayers.log.severe("Couldn't save " + e.getPlayer().getName() + "'s playtime!");
			ioe.printStackTrace();
		}
		UnknownPlayers.uuids.remove(e.getPlayer().getName()); // Prevent mem leaks
	}
	
	@EventHandler
	public void prelogin (AsyncPlayerPreLoginEvent e) {
		Profile[] profile = UnknownPlayers.repository.findProfilesByCriteria(new ProfileCriteria(e.getName(), "minecraft"));
		if (profile.length != 1) {
			e.disallow(Result.KICK_OTHER, "Either your account has an invalid number of UUIDs,\nyou didn't buy Minecraft or\nMojang's servers are down.");
			e.setLoginResult(Result.KICK_OTHER);
			e.setKickMessage("Either your account has an invalid number of UUIDs,\nyou didn't buy Minecraft or\nMojang's servers are down.");
			return;
		}
		UUID uuid = null;
		for (Profile pr : profile) { // Loop through it even though there's only one entry
			uuid = UUID.fromString(pr.getId());
		}
		if (uuid == null) {
			e.disallow(Result.KICK_OTHER, "Either your account has an invalid number of UUIDs,\nyou didn't buy Minecraft or\nMojang's servers are down.");
			e.setLoginResult(Result.KICK_OTHER);
			e.setKickMessage("Either your account has an invalid number of UUIDs,\nyou didn't buy Minecraft or\nMojang's servers are down.");
			return;
		}
		UnknownPlayers.uuids.put(e.getName(), uuid);
	}
}
