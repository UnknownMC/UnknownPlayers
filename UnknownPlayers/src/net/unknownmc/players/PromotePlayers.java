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

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PromotePlayers extends BukkitRunnable {

	public PromotePlayers (UnknownPlayers pl) {
		
	}
	
    @Override
    public void run() {
    	for (Player pl : Bukkit.getOnlinePlayers()) {
    		if (!pl.hasPermission("unknownmc.player")) {
    			Playtime play = new Playtime(UnknownPlayers.getUUID(pl));
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
