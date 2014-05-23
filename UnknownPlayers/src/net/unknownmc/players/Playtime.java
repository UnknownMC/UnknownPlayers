/*
 * UnknownPlayers
 * Copyright (C) 2014  UnknownMC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.unknownmc.players;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Playtime {
	
	String player;
	long time;
	File data;
	FileConfiguration stats;
	
	/**
	 * The method to be used for getting/setting a player's play time configuration.
	 * @param uuid The player's UUID
	 */
	public Playtime(String uuid) {
		player = uuid;
		File folder = UnknownPlayers.folder;
		String name = "uuid-" + player + ".yml";
		data = new File(folder, name);
		boolean isNew = false;
		if (!data.exists()) {
			data.getParentFile().mkdirs();
			try {
				data.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			isNew = true;
		}
		stats = YamlConfiguration.loadConfiguration(data);
		if (isNew) {
			createEmptyConfig();
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
	public boolean setPlayTime(long time) {
		if (stats == null) {
			return false;
		}
		stats.set("playtime", time);
		try {
			stats.save(data);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Resets the player's last join time, setting it to the current time. Call this in the onjoin or after saving the play time
	 * @return True on success, false on fail
	 */
	public boolean resetLastJoinTime() {
		if (stats == null) {
			return false;
		}
		stats.set("lastjoin", System.currentTimeMillis());
		try {
			stats.save(data);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Saves the statistics to file
	 * @return True on success, false on fail
	 */
	public boolean saveStatistics() {
		if (stats == null) {
			return false;
		}
		if (!data.exists()) {
			UnknownPlayers.log.severe("What happened to playerdata/" + data.getName() + "?!");
			UnknownPlayers.log.severe("Discarding unsaved data!");
			return false;
		}
		long oldPlaytime = stats.getLong("playtime");
		long join = stats.getLong("lastjoin");
		long currentSession = System.currentTimeMillis() - join;
		return setPlayTime(currentSession + oldPlaytime);
	}
	
	/**
	 * Fills the configuration with default values.
	 * @return True on success, false on fail
	 */
	public boolean createEmptyConfig() {
		try {
			stats.set("firstjoin", System.currentTimeMillis());
			stats.set("playtime", 0);
			stats.save(data);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Adds an username to list of known names.
	 * @param name The name
	 * @return False on fail, true on success. If name already exists, will fail silently (reports true)
	 */
	public boolean addName(String name) {
		List<String> names = getNames();
		if (!names.contains(name)) {
			names.add(name);
			stats.set("known-names", names);
			try {
				stats.save(data);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	public List<String> getNames() {
		List<?> objects = stats.getList("known-names");
		List<String> names = new ArrayList<String>();
		if (objects != null) {
			if (objects.size() > 0) {
				for (Object o : objects) {
					names.add(o.toString());
				}
			}
		}
		return names;
	}
	
	/**
	 * Get the UUID used.
	 * @return The UUID
	 */
	public String getUUID() {
		return player;
	}
	
	public long getFirstJoin() {
		return stats.getLong("firstjoin");
	}
}