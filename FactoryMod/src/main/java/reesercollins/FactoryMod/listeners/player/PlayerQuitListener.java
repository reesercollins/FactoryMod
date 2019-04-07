package reesercollins.FactoryMod.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import reesercollins.FactoryMod.interaction.clickable.ClickableInventory;

public class PlayerQuitListener implements Listener {
	
	@EventHandler
	public void onEvent(PlayerQuitEvent e) {
		ClickableInventory.inventoryWasClosed(e.getPlayer());
	}

}
