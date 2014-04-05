package net.unknownmc.players;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Playtime {
	
	UUID player;
	long time;
	File data;
	FileConfiguration stats;
	
	/**
	 * The method to be used for getting/setting a player's play time configuration.
	 * @param pl The player name
	 */
	public Playtime(UUID uuid) {
		player = uuid;
		File folder = UnknownPlayers.folder;
		String name = player + ".yml";
		data = new File(folder, name);
		if (!data.exists()) {
			UnknownPlayers.log.severe(data.getAbsolutePath() + " (" + player + ") doesn't exist!");
		} else {
			stats = YamlConfiguration.loadConfiguration(data);
		}
	}
	
	/**
	 * Gets the player's play time.
	 * @return The play time in milliseconds
	 */
	public long getPlayTime() {
		if (stats == null) {
			return -1L;
		}
		return stats.getLong("playtime");
	}
	
	/**
	 * Get the player's last join time.
	 * @return The last join time in milliseconds (based on System.currentTimeMillis())
	 */
	public long getLastJoinTime() {
		if (stats == null) {
			return -1L;
		}
		return stats.getLong("lastjoin");
	}
	
	/**
	 * Sets the player's play time, overwriting the current value.
	 * @param time The player's new play time
	 */
	public void setPlayTime(long time) {
		stats.set("playtime", time);
		try {
			stats.save(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Resets the player's last join time, setting it to the current time. Call this in the onjoin or after saving the play time
	 */
	public void resetLastJoinTime() {
		stats.set("lastjoin", System.currentTimeMillis());
		try {
			stats.save(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the statistics to file
	 */
	public boolean saveStatistics() {
		if (!data.exists()) {
			UnknownPlayers.log.severe("What happened to playerdata/" + data.getName() + "?!");
			UnknownPlayers.log.severe("Discarding unsaved data!");
			return false;
		}
		long oldPlaytime = stats.getLong("playtime");
		long join = stats.getLong("lastjoin");
		long currentSession = System.currentTimeMillis() - join;
		long newPlaytime = oldPlaytime + currentSession;
		stats.set("playtime", newPlaytime);
		try {
			stats.save(data);
			return true;
		} catch (IOException ioe) {
			UnknownPlayers.log.severe("Couldn't save " + player + "'s playtime!");
			ioe.printStackTrace();
			return false;
		}
	}
}