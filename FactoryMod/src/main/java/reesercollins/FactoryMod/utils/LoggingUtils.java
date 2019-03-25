package reesercollins.FactoryMod.utils;

import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.itemHandling.ItemMap;

public class LoggingUtils {

	public static void log(String msg) {
		FMPlugin.getInstance().getLogger().log(Level.INFO, msg);
	}

	private static String serializeInventory(Inventory i) {
		return new ItemMap(i).toString();
	}

	public static void logInventory(Block b) {
		if (FMPlugin.getManager().logInventories() && b.getState() instanceof InventoryHolder) {
			log("Contents of " + b.getType().toString() + " at " + b.getLocation().toString() + " contains: "
					+ serializeInventory(((InventoryHolder) b.getState()).getInventory()));
		}
	}

	public static void logInventory(Inventory i, String msg) {
		if (FMPlugin.getManager().logInventories()) {
			log(msg + serializeInventory(i));
		}
	}

}
