package reesercollins.FactoryMod.listeners.entity;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.FactoryManager;
import reesercollins.FactoryMod.factories.Factory;

public class EntityExplodeListener implements Listener {

	FactoryManager manager;

	public EntityExplodeListener() {
		this.manager = FMPlugin.getManager();
	}

	@EventHandler
	public void onEvent(EntityExplodeEvent e) {
		List<Block> blocks = e.blockList();
		for (Block block : blocks) {
			if (manager.isPossibleInteractionBlock(block.getType())) {
				Factory c = manager.getFactoryAt(block);
				if (c != null) {
					c.getInteractionManager().blockBreak(null, block);
				}
			}
		}
	}

}
