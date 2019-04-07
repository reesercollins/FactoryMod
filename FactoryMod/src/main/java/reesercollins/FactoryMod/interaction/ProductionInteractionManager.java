package reesercollins.FactoryMod.interaction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.interaction.clickable.Clickable;
import reesercollins.FactoryMod.interaction.clickable.ClickableInventory;
import reesercollins.FactoryMod.interaction.clickable.DecorationStack;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.recipes.IRecipe;
import reesercollins.FactoryMod.recipes.InputRecipe;
import reesercollins.FactoryMod.recipes.ProductionRecipe;
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
					DecorationStack c = new DecorationStack(is);
					ci.setSlot(c, index);
					index++;
				}
				List<ItemStack> output = ((InputRecipe) pf.getCurrentRecipe()).getOutputRepresentation(pf.getInventory(),
						pf);
				if (output.size() > 18) {
					output = new ItemMap(output).getLoredItemCountRepresentation();
				}
				index = 27;
				for (ItemStack is : output) {
					Clickable c = new DecorationStack(is);
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
		} else if (b.equals(pf.getCraftingTable())) {
			int rows = (pf.getRecipes().size() / 9) + 1;
			ClickableInventory ci = new ClickableInventory(rows * 9, "Select a recipe");
			for (IRecipe rec : pf.getRecipes()) {
				InputRecipe recipe = (InputRecipe) rec;
				ItemStack recStack = recipe.getRecipeRepresentation();
				int runcount = pf.getRunCount(recipe);

				ItemMeta recMeta = recStack.getItemMeta();
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.AQUA + "Ran " + runcount + " times");

				if (recipe instanceof ProductionRecipe) {
					ProductionRecipe prod = (ProductionRecipe) recipe;
					if (prod.getModifier() != null) {
						lore.add(ChatColor.BOLD + "" + ChatColor.GOLD + pf.getRecipeLevel(recipe) + " ★");
						lore.add(ChatColor.GREEN + decimalFormatting
								.format(prod.getModifier().getFactor(pf.getRecipeLevel(recipe), runcount)));
					}
				}
				recMeta.setLore(lore);
				recStack.setItemMeta(recMeta);

				Clickable c = new Clickable(recStack) {

					@Override
					public void clicked(Player p) {
						if (pf.isActive()) {
							p.sendMessage(ChatColor.RED + "You can't switch recipes while the factory is running!");
						} else {
							pf.setRecipe(recipes.get(this));
							p.sendMessage(ChatColor.GREEN + "Switched recipe to " + recipes.get(this).getName());
						}
					}

				};

				recipes.put(c, recipe);
				ci.addSlot(c);
			}

			ItemStack autoSelectStack = new ItemStack(
					pf.isAutoSelect() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
			ItemMeta autoSelectMeta = autoSelectStack.getItemMeta();
			autoSelectMeta.setDisplayName(ChatColor.RESET + "Toggle auto select");
			autoSelectMeta.setLore(new ArrayList<String>(
					Arrays.asList(ChatColor.GOLD + "Auto select will make the factory automatically select any",
							ChatColor.GOLD + "recipe it can run whenever you activate it.",
							ChatColor.AQUA + "Click to turn it " + (pf.isAutoSelect() ? "off" : "on"))));
			autoSelectStack.setItemMeta(autoSelectMeta);
			Clickable autoSelectClick = new Clickable(autoSelectStack) {

				@Override
				public void clicked(Player p) {
					p.sendMessage(ChatColor.GREEN + "Turned auto select " + (pf.isAutoSelect() ? "off" : "on") + " for "
							+ pf.getName());
					getItemStack().setType(pf.isAutoSelect() ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK);
					pf.setAutoSelect(!pf.isAutoSelect());
				}
			};
			ci.setSlot(autoSelectClick, (rows * 9) - 2);
			ItemStack menuStack = new ItemStack(Material.PAINTING);
			ItemMeta menuMeta = menuStack.getItemMeta();
			menuMeta.setDisplayName(ChatColor.RESET + "Open Menu");
			menuMeta.setLore(
					new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to open a detailed menu")));
			menuStack.setItemMeta(menuMeta);
			Clickable menuC = new Clickable(menuStack) {
				@Override
				public void clicked(Player p) {
					FMPlugin.getMenuBuilder().openFactoryBrowser(p, pf.getName());
				}
			};
			ci.setSlot(menuC, (rows * 9) - 1);

			ci.showInventory(p);
			return;

		} else if (b.equals(pf.getFurnace())) {
			if (pf.isActive()) {
				pf.deactivate();
				p.sendMessage(ChatColor.RED + "Deactivated " + pf.getName());
			} else {
				pf.attemptToActivate(p, false);
			}
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
