package reesercollins.FactoryMod.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import reesercollins.FactoryMod.ConfigParser;
import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.interaction.clickable.MenuBuilder;

public class FactoryMenu extends BaseCommand {

	@Override
	protected boolean onNoRemainingArgs(CommandSender sender, Command rootCommand, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can run this command");
			return false;
		}
		MenuBuilder mb = FMPlugin.getMenuBuilder();
		Player p = (Player) sender;
		if (args.length == 0) {
			mb.openFactoryBrowser(p, null);
		} else {
			mb.openFactoryBrowser(p, ConfigParser.getFactoryName(args));
		}
		return false;
	}

	@Override
	public List<String> getTabCompletions(CommandSender sender, Command cmd, String label, String[] args) {
		return super.tabCompleteFactory(sender, args);
	}

}
