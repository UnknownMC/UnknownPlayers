package net.unknownmc.players;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CommandsHandler implements CommandExecutor {

	//UnknownPlayers main;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (cmd.getName().equalsIgnoreCase("player")) {
			if (!sender.hasPermission("unknownmc.playtime")) {
				sender.sendMessage(ChatColor.RED + "Such access. Many denied. Very HTTP 403 (even though this wasn't an HTTP request but whatever). Wow.");
				return true;
			}
			String player = "";
			if (args.length == 0) {
				player = sender.getName();
			} else if (args.length == 1) {
				if (!sender.hasPermission("unknownmc.playtime.other")) {
					sender.sendMessage(ChatColor.RED + "You can only check your playtime (omit all arguments).");
					return true;
				}
				//sender.sendMessage(ChatColor.RED + "Incorrect syntax! Please use " + ChatColor.DARK_RED + "/player <player name>");
				//return true;
				player = args[0];
			} else {
				if (sender.hasPermission("unknownmc.playtime.other")) {
					sender.sendMessage(ChatColor.RED + "Incorrect syntax! Use " + ChatColor.DARK_RED + "/player [name]" + ChatColor.RED + ".");
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect syntax! Use " + ChatColor.DARK_RED + "/player" + ChatColor.RED + ".");
				}
				return true;
			}
			if (player == "") {
				sender.sendMessage(ChatColor.RED + "Oops, an error occurred!");
				return true;
			}
			// Prevent SQL-injection-like entries (access non-player file)
			if ((player.contains("/")) || (player.contains(".")) || (player.contains("\\"))) {
				sender.sendMessage(ChatColor.RED + "That username isn't valid...");
				return true;
			}
			
			File folder = UnknownPlayers.folder;
			String name = player.toLowerCase() + ".yml";
			File confF = new File(folder, name);
			if (!confF.exists()) {
				sender.sendMessage(ChatColor.RED + player + " has never connected to the server yet.");
				return true;
			}
			
			FileConfiguration stats = YamlConfiguration.loadConfiguration(confF);
			
			sender.sendMessage(ChatColor.GOLD + "=======-------> " + ChatColor.YELLOW + player + ChatColor.GOLD + " <-------=======");
			Player onlineplayer = Bukkit.getPlayerExact(player);
			boolean online = false;
			if ((onlineplayer != null) && (onlineplayer.isOnline())) {
				//sender.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.RED + "TIP" + ChatColor.DARK_RED + "] " + ChatColor.GREEN + player + " is currently online, use " + ChatColor.DARK_GREEN + "/whois " + player + ChatColor.GREEN + " to see more information about them.");
				online = true;
			}
			long playtime = stats.getLong("playtime");
			if (online) {
				long current = System.currentTimeMillis() - stats.getLong("lastjoin");
				playtime = playtime + current;
			}
			long second = (playtime / 1000) % 60;
			long minute = (playtime / (1000 * 60)) % 60;
			long hour = (playtime / (1000 * 60 * 60)) % 24;
			long days = (playtime / (1000 * 60 * 60 * 24));
	
			String time = String.format(ChatColor.GOLD + "%d" + ChatColor.YELLOW + " days " + ChatColor.GOLD + "%d" + ChatColor.YELLOW + " hours " + ChatColor.GOLD + "%d" + ChatColor.YELLOW + " minutes " + ChatColor.GOLD + "%d" + ChatColor.YELLOW + " seconds", days, hour, minute, second);
	
			sender.sendMessage(ChatColor.GREEN + "Play time: " + ChatColor.DARK_GREEN + time);
			long reqtime = UnknownPlayers.config.getLong("rankup.time") * 60 * 1000;
			if (playtime < reqtime) {
				long timeuntil = reqtime - playtime;
				long plS = (timeuntil / 1000) % 60;
				long plM = (timeuntil / (1000 * 60)) % 60;
				long plH = (timeuntil / (1000 * 60 * 60));
				String timeplayer = String.format(ChatColor.GOLD + "%d" + ChatColor.YELLOW + " hours " + ChatColor.GOLD + "%d" + ChatColor.YELLOW + " minutes " + ChatColor.GOLD + "%d" + ChatColor.YELLOW + " seconds", plH, plM, plS);
				sender.sendMessage(ChatColor.GREEN + "Time until Player: " + timeplayer);
			}
	//		sender.sendMessage(ChatColor.GREEN + "Play time: " + ChatColor.DARK_GREEN + time);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(stats.getLong("firstjoin"));
			String first = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'Z").format(cal.getTime());
			sender.sendMessage(ChatColor.GREEN + "First visit: " + ChatColor.YELLOW + first);
			if (sender.hasPermission("unknownmc.playtime.detailed")) {
				OfflinePlayer op = Bukkit.getServer().getOfflinePlayer("");
				boolean ban = op.isBanned();
				sender.sendMessage(ChatColor.GREEN + "Is banned: " + bool2Str(ban));
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("demote")) {
			if (!sender.hasPermission("unknownmc.demote")) {
				sender.sendMessage(ChatColor.RED + "Why are you even trying to demote somebody?!");
				return true;
			}
			if ((args.length > 2) || (args.length < 1)) {
				sender.sendMessage(ChatColor.RED + "/demote <staff member's name> [fire]" + ChatColor.GREEN + " - only include 'fire' if you want them to be [Player] and not [VIP].");
				return true;
			}
			boolean fire = false;
			if (args.length == 2) {
				if (!args[1].equalsIgnoreCase("fire")) {
					sender.sendMessage(ChatColor.RED + "Argument 2 can only be " + ChatColor.DARK_RED + "fire" + ChatColor.RED + " (moves the former mod to Player instead of VIP)");
					return true;
				} else {
					fire = true;
				}
			}
			StaffHandler sh = new StaffHandler(args[0]);
			if (sh.demote(fire)) {
				sender.sendMessage(ChatColor.GREEN + args[0] + " is no longer a staff member.");
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Either " + args[0] + " isn't staff, you don't have the permission to demote them or an error occurred with the plugin. They weren't demoted.");
				return true;
			}
		}
		return false;
	}
	
	public String bool2Str(boolean bool) {
		String str = "";
		if (bool) {
			str = ChatColor.DARK_GREEN + "Yes";
		}
		if (!bool) {
			str = ChatColor.DARK_RED + "No";
		}
		return str;
	}

}
