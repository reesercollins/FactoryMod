package reesercollins.FactoryMod.listeners.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import reesercollins.FactoryMod.interaction.clickable.ClickableInventory;

public class InventoryCloseListener implements Listener {

	@EventHandler
	public void onEvent(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getPlayer();
		ClickableInventory.inventoryWasClosed(p);
	}
}
