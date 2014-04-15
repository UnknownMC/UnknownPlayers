package net.unknownmc.players;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class CommandsHandler implements CommandExecutor {

	//UnknownPlayers main;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (cmd.getName().equalsIgnoreCase("player")) {
			if (!sender.hasPermission("unknownmc.playtime")) {
				sender.sendMessage(ChatColor.RED + "No access");
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
				player = args[0];
			} else {
				if (sender.hasPermission("unknownmc.playtime.other")) {
					sender.sendMessage(ChatColor.RED + "Incorrect syntax! Use " + ChatColor.DARK_RED + "/player [name]" + ChatColor.RED + ".");
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect syntax! Use " + ChatColor.DARK_RED + "/player" + ChatColor.RED + ".");
				}
				return true;
			}
			if (player.equals("")) {
				sender.sendMessage(ChatColor.RED + "Oops, an error occurred!");
				return true;
			}
			// Prevent SQL-injection-like entries (access non-player file)
			if ((player.contains("/")) || (player.contains(".")) || (player.contains("\\"))) {
				sender.sendMessage(ChatColor.RED + "That username isn't valid...");
				return true;
			}
			
			final String targ = player;
			final CommandSender send = sender;
			
			Player target = Bukkit.getPlayerExact(targ);
			if (target != null) {
				String uuid = UnknownPlayers.getUUID(target);
				Playtime pl = new Playtime(uuid);
				sendPlaytime(pl, send);
			} else {
				new BukkitRunnable() {
					@Override
					public void run() {
						String uuid = UnknownPlayers.getUUID(targ);
						Playtime pl = new Playtime(uuid);
						sendPlaytime(pl, send);
					}
				};
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("demote")) {
			if (!sender.hasPermission("unknownmc.demote")) {
				sender.sendMessage(ChatColor.RED + "Why are you even trying to demote somebody?!");
				return true;
			}
			StaffHandler sh = new StaffHandler(args[0]);
			if (sh.demote()) {
				sender.sendMessage(ChatColor.GREEN + args[0] + " is no longer a staff member.");
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Either " + args[0] + " isn't staff, you don't have the permission to demote them or an error occurred with the plugin. They weren't demoted.");
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("names")) {
			if (!sender.hasPermission("unknownmc.playernames")) {
				sender.sendMessage(ChatColor.RED + "Nope");
				return true;
			}
			if (args.length != 1) {
				sender.sendMessage(ChatColor.RED + "Incorrect usage, try /names <player name>");
				return true;
			}
			Player pl = Bukkit.getServer().getPlayerExact(args[0]);
			if (pl == null) {
				sender.sendMessage(ChatColor.RED + "The player you're checking should be online.");
				return true;
			}
			String uuid = UnknownPlayers.getUUID(pl);
			Playtime pt = new Playtime(uuid);
			List<String> names = pt.getNames();
			String nameStr = "";
			if (names.size() == 1) {
				nameStr = ChatColor.RED + "none";
			} else {
				for (String name : names) {
					if (!name.equalsIgnoreCase(pl.getName())) {
						if (nameStr.length() != 0) {
							nameStr = nameStr + ", ";
						}
						nameStr = nameStr + name;
					}
				}
			}
			sender.sendMessage(ChatColor.GREEN + pl.getName() + "'s previous names: " + ChatColor.YELLOW + nameStr);
		}
		return false;
	}
	
	/**
	 * Send requested playtime information
	 * @param play The instance of Playtime
	 * @param sender The requester
	 */
	public void sendPlaytime(Playtime play, CommandSender sender) {
		long playtime = play.getPlayTime();
        UUID uuid = new UUID(new BigInteger(play.getUUID().substring(0, 16), 16).longValue(), new BigInteger(play.getUUID().substring(16), 16).longValue());
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			if (player.isOnline()) {
				playtime = (playtime) + (System.currentTimeMillis() - play.getLastJoinTime());
			}
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
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(play.getFirstJoin());
		String first = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'Z").format(cal.getTime());
		sender.sendMessage(ChatColor.GREEN + "First visit: " + ChatColor.YELLOW + first);
		String usernames = "";
		for (String name : play.getNames()) {
			if (usernames.length() != 0) {
				usernames = usernames + ", ";
			}
			usernames = usernames + name;
		}
		sender.sendMessage(ChatColor.GREEN + "Known usernames: " + ChatColor.YELLOW + usernames);
	}
}
