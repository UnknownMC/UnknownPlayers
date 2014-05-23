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

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mojang.api.profiles.Profile;

public class EvtListener implements Listener {
	
	@EventHandler
	public void join (PlayerJoinEvent e) {
		Playtime play = new Playtime(UnknownPlayers.getUUID(e.getPlayer())); // This generates file if not found
		play.resetLastJoinTime(); // This sets lastjoin
		play.addName(e.getPlayer().getName());
	}

	@EventHandler
	public void leave (PlayerQuitEvent e) {
		//if editing this, update UnknownPlayers.onDisable()
		Playtime play = new Playtime(UnknownPlayers.getUUID(e.getPlayer()));
		play.saveStatistics();
		UnknownPlayers.uuids.remove(e.getPlayer().getName()); // Prevent mem leaks
	}
	
	@EventHandler
	public void prelogin (AsyncPlayerPreLoginEvent e) {
		Profile[] profile = UnknownPlayers.repository.findProfilesByCriteria(e.getName());
		if (profile.length != 1) {
			e.disallow(Result.KICK_OTHER, ChatColor.RED + "Error connecting to server: couldn't get your UUID\nAre Mojang's login servers down?");
			e.setLoginResult(Result.KICK_OTHER);
			e.setKickMessage(ChatColor.RED + "Error connecting to server: couldn't get your UUID\nAre Mojang's login servers down?");
			return;
		}
		String uuid = null;
		for (Profile pr : profile) { // Loop through it even though there's only one entry
			uuid = pr.getId();
		}
		if (uuid == null) {
			e.disallow(Result.KICK_OTHER, ChatColor.RED + "Error connecting to server: couldn't get your UUID\nAre Mojang's login servers down?");
			e.setLoginResult(Result.KICK_OTHER);
			e.setKickMessage(ChatColor.RED + "Error connecting to server: couldn't get your UUID\nAre Mojang's login servers down?");
			return;
		}
		UnknownPlayers.uuids.put(e.getName(), uuid);
	}
}
