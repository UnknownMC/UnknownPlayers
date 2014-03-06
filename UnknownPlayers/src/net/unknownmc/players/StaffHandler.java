package net.unknownmc.players;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
		File gmDir = new File(Bukkit.getServer().getPluginManager().getPlugin("GroupManager").getDataFolder(), File.separator + "worlds" + File.separator + "unknownmc");
		users = new File(gmDir, "users.yml");
		if (!users.isFile()) {
			UnknownPlayers.log.severe(users.getAbsolutePath() + " doesn't exist!");
			error = true;
		} else {
			usrs = YamlConfiguration.loadConfiguration(users);
		}
		
		staffR = new ArrayList<String>();
		staffR.add("Moderator");
		
		pl = player;
	}
	
	public boolean demote(boolean fire) {
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
		
		String n3w = "vip";
		if (fire) {
			n3w = "Default";
		}
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
