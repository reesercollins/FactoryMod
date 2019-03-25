package reesercollins.FactoryMod.interaction;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.interaction.clickable.Clickable;
import reesercollins.FactoryMod.recipes.InputRecipe;

public class ProductionInteractionManager implements IInteractionManager {

	private ProductionFactory pf;
	private HashMap<Clickable, InputRecipe> recipes = new HashMap<Clickable, InputRecipe>();
	private DecimalFormat decimalFormatting;

	public ProductionInteractionManager(ProductionFactory pf) {
		this();
		this.pf = pf;
	}

	public ProductionInteractionManager() {
		this.decimalFormatting = new DecimalFormat("#.#####");
	}

	public void setFactory(ProductionFactory pf) {
		this.pf = pf;
	}

	@Override
	public void rightClick(Player p, Block b, BlockFace bf) {
		// All of the blocks in this factory already have a rightClick inventory.
	}

	@Override
	public void leftClick(Player p, Block b, BlockFace bf) {
		if (p.getInventory().getItemInMainHand().getType() != FMPlugin.getManager()
				.getFactoryInteractionMaterial()) {
			    return;
		}
	}

	@Override
	public void blockBreak(Player p, Block b) {
		if (p != null && !pf.getRepairManager().inDisrepair()) {
			p.sendMessage(ChatColor.DARK_RED + "You broke the factory, it is in disrepair now");
		}
		if (pf.isActive()) {
			pf.deactivate();
		}
		pf.getRepairManager().breakIt();
	}

	@Override
	public void redstoneEvent(BlockRedstoneEvent e, Block factoryBlock) {
		// TODO Auto-generated method stub

	}

}
