package reesercollins.FactoryMod.interaction;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.interaction.clickable.Clickable;
import reesercollins.FactoryMod.interaction.clickable.ClickableInventory;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.recipes.InputRecipe;
import reesercollins.FactoryMod.repair.PercentageHealthRepairManager;

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
		if (p.getInventory().getItemInMainHand().getType() != FMPlugin.getManager().getFactoryInteractionMaterial()) {
			return;
		}
//		ProtectedBlock block = FileManager.getBlockProtection(b.getLocation());
//		if (!block.playerHasPermission(p, FactoryPermission.USE_FACTORY)) {
//			p.sendMessage(ChatColor.RED + "You do not have permission to interact with this factory.");
//			return;
//		}
		if (b.equals(pf.getChest())) {
			if (p.isSneaking()) {
				ClickableInventory ci = new ClickableInventory(54, pf.getCurrentRecipe().getName());
				List<ItemStack> input = ((InputRecipe) pf.getCurrentRecipe()).getInputRepresentation(pf.getInventory(),
						pf);
				if (input.size() > 18) {
					input = new ItemMap(input).getLoredItemCountRepresentation();
				}
				int index = 0;
				for (ItemStack is : input) {
					Clickable c = new Clickable(is) {
						@Override
						public void clicked(Player p) {
						}
					};
					ci.setSlot(c, index);
					index++;
				}
				List<ItemStack> output = ((InputRecipe) pf.getCurrentRecipe()).getInputRepresentation(pf.getInventory(),
						pf);
				if (output.size() > 18) {
					output = new ItemMap(output).getLoredItemCountRepresentation();
				}
				index = 44;
				for (ItemStack is : output) {
					Clickable c = new Clickable(is) {
						@Override
						public void clicked(Player p) {
						}
					};
					ci.setSlot(c, index);
					index++;
				}
				ci.showInventory(p);
			} else {
				p.sendMessage(ChatColor.GOLD + pf.getName() + " currently turned " + (pf.isActive() ? "on" : "off"));
				if (pf.isActive()) {
					p.sendMessage(ChatColor.GOLD
							+ String.valueOf((pf.getCurrentRecipe().getProductionTime() - pf.getRunningTime()) / 20)
							+ " seconds remaining until current run is complete");
				}
				p.sendMessage(ChatColor.GOLD + "Currently selected recipe: " + pf.getCurrentRecipe().getName());
				p.sendMessage(ChatColor.GOLD + "Currently at " + pf.getRepairManager().getHealth() + " health");

				if (pf.getRepairManager().inDisrepair()) {
					PercentageHealthRepairManager rm = ((PercentageHealthRepairManager) pf.getRepairManager());
					long leftTime = rm.getGracePeriod() - (System.currentTimeMillis() - rm.getBreakTime());
					long months = leftTime / (60L * 60L * 24L * 30L * 1000L);
					long days = (leftTime - (months * 60L * 60L * 24L * 30L * 1000L)) / (60L * 60L * 24L * 1000L);
					long hours = (leftTime - (months * 60L * 60L * 24L * 30L * 1000L)
							- (days * 60L * 60L * 24L * 1000L)) / (60L * 60L * 1000L);
					String time = (months != 0 ? months + " months, " : "") + (days != 0 ? days + " days, " : "")
							+ (hours != 0 ? hours + " hours" : "");
					if (time.equals("")) {
						time = " less than an hour";
					}
					p.sendMessage(ChatColor.GOLD + "It will break permanently in " + time);
				}
			}
		}
		if (b.equals(pf.getCraftingTable())) {
			
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
