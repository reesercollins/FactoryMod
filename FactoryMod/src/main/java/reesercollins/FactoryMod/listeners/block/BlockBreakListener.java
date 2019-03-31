package reesercollins.FactoryMod.listeners.block;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.FactoryManager;
import reesercollins.FactoryMod.factories.Factory;

public class BlockBreakListener implements Listener {

	FactoryManager manager;

	public BlockBreakListener() {
		this.manager = FMPlugin.getManager();
	}

	@EventHandler
	public void onEvent(BlockBreakEvent e) {
		Block block = e.getBlock();
		if (manager.isPossibleInteractionBlock(block.getType())) {
			Factory c = manager.getFactoryAt(block);
			if (c != null) {
				// let creative player interact without breaking it
				if (e.getPlayer().getGameMode() == GameMode.CREATIVE
						&& e.getPlayer().getInventory().getItemInMainHand() != null && e.getPlayer().getInventory()
								.getItemInMainHand().getType() == manager.getFactoryInteractionMaterial()) {
					e.setCancelled(true);
					return;
				}
				c.getInteractionManager().blockBreak(e.getPlayer(), block);
			}
		}
	}

}
