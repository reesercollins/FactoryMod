package reesercollins.FactoryMod.listeners.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import reesercollins.FactoryMod.interaction.clickable.ClickableInventory;

public class InventoryClickListener implements Listener {
	
	@EventHandler
	public void onEvent(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getWhoClicked();
		ClickableInventory ci = ClickableInventory.getOpenInventory(p);
		if (ci != null) {
			e.setCancelled(true);
			ci.itemClick(p, e.getRawSlot());
		}
	}

}
