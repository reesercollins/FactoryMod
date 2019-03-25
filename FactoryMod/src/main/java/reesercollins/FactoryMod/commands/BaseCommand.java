package reesercollins.FactoryMod.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public abstract class BaseCommand implements TabExecutor {
	
	private final Map<String, BaseCommand> subCommands = new HashMap<>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			TabExecutor child = subCommands.get(args[0]);
			if (child != null) {
				label = args[0];
				String[] newArgs = new String[args.length - 1];
				System.arraycopy(args, 1, newArgs, 0, newArgs.length);
				return child.onCommand(sender, cmd, label, newArgs);
			} else if (args.length == 2){
				TabExecutor wildcardChild = subCommands.get("* " + args[1]);
				if (wildcardChild != null) {
					label = args[1];
					String[] newArgs = new String[args.length - 1];
					System.arraycopy(args, 1, newArgs, 0, newArgs.length);
					return wildcardChild.onCommand(sender, cmd, label, newArgs);
				}
			}
		}
		return onNoRemainingArgs(sender, cmd, label, args);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			BaseCommand child = subCommands.get(args[0]);
			if (child != null) {
				return child.getTabCompletions();
			}
		}
		return this.getTabCompletions();
	}
	
	/**
	 * Gets the values that the TabCompleter should return when the sender presses tab.
	 * @return A List containing all values to be displayed.
	 */
	public List<String> getTabCompletions() {
		return new ArrayList<String>(subCommands.keySet());
	}
	
	/**
	 * Executed only when the sub-command has no proceeding arguments.
	 * 
	 * @param sender Sender of the command.
	 * @param cmd Root command from which the sub-command originates.
	 * @param label The label of the sub-command.
	 * @param args Arguments not including argument before and including the sub-command label.
	 * @return Outcome of the command.
	 */
	protected abstract boolean onNoRemainingArgs(CommandSender sender, Command rootCommand, String label, String[] args);

}
