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
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StaffHandler {

	private static File users;
	private static FileConfiguration usrs;
	private static boolean error = false;
	private static String pl;
	private static List<String> staffR;
	
	public StaffHandler(String player) {
		File gmDir = new File(Bukkit.getServer().getPluginManager().getPlugin("GroupManager").getDataFolder(), File.separator + "worlds" + File.separator + UnknownPlayers.config.getString("default-world"));
		users = new File(gmDir, "users.yml");
		if (!users.isFile()) {
			UnknownPlayers.log.severe(users.getAbsolutePath() + " doesn't exist!");
			error = true;
		} else {
			usrs = YamlConfiguration.loadConfiguration(users);
		}
		
		staffR = UnknownPlayers.config.getStringList("demote.demote-groups");
		
		pl = player;
	}
	
	public boolean demote() {
		if (error) {
			return false;
		}
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mansave");
		String path = "users." + pl + ".group";
		if (!usrs.isString(path)) {
			return false;
		}
		String current = usrs.getString("users." + pl + ".group");
		if (!staffR.contains(current)) {
			// Not staff, no demote.
			return false;
		}
		
		String n3w = UnknownPlayers.config.getString("demote.demote-to-group");
		usrs.set(path, n3w);
		try {
			usrs.save(users);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "manload");
		return true;
	}
}
