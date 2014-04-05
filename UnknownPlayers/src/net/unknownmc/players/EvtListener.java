package net.unknownmc.players;

import java.util.UUID;

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
