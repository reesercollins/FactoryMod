package reesercollins.FactoryMod.listeners.player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.FactoryManager;
import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.structures.MultiBlockStructure;

public class PlayerInteractListener implements Listener {

	FactoryManager manager;

	public PlayerInteractListener() {
		this.manager = FMPlugin.getManager();
	}

	@EventHandler
	public void onEvent(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		Player player = e.getPlayer();
		if (block != null && manager.isPossibleInteractionBlock(block.getType())) {
			BlockFace bf = e.getBlockFace();
			Factory c = manager.getFactoryAt(block);

			if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
				if (c == null) {
					if (manager.isPossibleCenterBlock(block.getType())) {
						if (player.getInventory().getItemInMainHand().getType() == manager
								.getFactoryInteractionMaterial()) {
							manager.attemptCreation(block, player);
						}
					} else {
						// check if chest is other half of double chest
						if (block.getType() == Material.CHEST) {
							for (Block b : MultiBlockStructure.searchForBlocksOnAllSides(block, Material.CHEST)) {
								Factory f = manager.getFactoryAt(b);
								if (f != null) {
									f.getInteractionManager().leftClick(player, b, bf);
								}
							}
						}
					}
				} else {
					c.getInteractionManager().leftClick(player, block, bf);
				}

			}
		}
	}

}
