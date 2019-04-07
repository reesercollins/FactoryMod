package reesercollins.FactoryMod.listeners.block;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.FactoryManager;
import reesercollins.FactoryMod.factories.Factory;

public class BlockBurnListener implements Listener {

	FactoryManager manager;

	public BlockBurnListener() {
		this.manager = FMPlugin.getManager();
	}

	@EventHandler
	public void onEvent(BlockBurnEvent e) {
		Block block = e.getBlock();
		if (manager.isPossibleInteractionBlock(block.getType())) {
			Factory c = manager.getFactoryAt(block);
			if (c != null) {
				c.getInteractionManager().blockBreak(null, block);
			}
		}
	}

}
