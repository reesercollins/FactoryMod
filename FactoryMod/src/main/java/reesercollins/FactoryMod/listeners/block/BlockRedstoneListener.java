package reesercollins.FactoryMod.listeners.block;

import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.FactoryManager;
import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.structures.MultiBlockStructure;

public class BlockRedstoneListener implements Listener {

	FactoryManager manager;

	public BlockRedstoneListener() {
		this.manager = FMPlugin.getManager();
	}

	@EventHandler
	public void onEvent(BlockRedstoneEvent e) {
		if (e.getOldCurrent() == e.getNewCurrent()) {
			return;
		}
		for (BlockFace face : MultiBlockStructure.allBlockSides) {
			Factory f = manager.getFactoryAt(e.getBlock().getRelative(face));
			if (f != null) {
				f.getInteractionManager().redstoneEvent(e, e.getBlock().getRelative(face));
			}
		}
	}

}
