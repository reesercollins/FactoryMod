package reesercollins.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;

public class RecipeScalingUpgradeRecipe extends InputRecipe {

	private ProductionRecipe toUpgrade;
	private int newRank;
	private RecipeScalingUpgradeRecipe followUpRecipe;

	public RecipeScalingUpgradeRecipe(String identifier, String name, int productionTime, ItemMap input,
			ProductionRecipe toUpgrade, int newRank, RecipeScalingUpgradeRecipe followUpRecipe) {
		super(identifier, name, productionTime, input);
		this.toUpgrade = toUpgrade;
		this.newRank = newRank;
		this.followUpRecipe = followUpRecipe;
	}

	public IRecipe getToUpgrade() {
		return toUpgrade;
	}

	public int getNewRank() {
		return newRank;
	}

	public IRecipe getFollowUpRecipe() {
		return followUpRecipe;
	}

	public void setUpgradedRecipe(ProductionRecipe rec) {
		this.toUpgrade = rec;
	}

	public void setFollowUpRecipe(RecipeScalingUpgradeRecipe rec) {
		this.followUpRecipe = rec;
	}

	@Override
	public void applyEffect(Inventory i, ProductionFactory pf) {
		logBeforeRecipeRun(i, pf);
		if (toUpgrade == null || !pf.getRecipes().contains(toUpgrade)) {
			return;
		}
		ItemMap toRemove = input.clone();
		if (toRemove.isContainedIn(i)) {
			if (toRemove.removeSafelyFrom(i)) {
				if (newRank == 1) {
					pf.addRecipe(toUpgrade);
				} else {
					pf.setRecipeLevel(toUpgrade, newRank);
				}
				// no longer needed
				pf.removeRecipe(this);
				if (followUpRecipe != null) {
					pf.addRecipe(followUpRecipe);
					pf.setRecipe(toUpgrade);
				}
			}
		}
		logAfterRecipeRun(i, pf);
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, ProductionFactory pf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, ProductionFactory pf) {
		ItemStack is = getRecipeRepresentation();
		List<ItemStack> result = new LinkedList<ItemStack>();
		result.add(is);
		return result;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		ItemStack is = new ItemStack(Material.PAPER);
		List<String> lore = new ArrayList<String>();
		if (toUpgrade == null) {
			lore.add(ChatColor.RED + "ERROR ERROR ERROR ERROR");
			return is;
		}
		if (newRank == 1) {
			lore.add(ChatColor.GOLD + "Unlock " + toUpgrade.getName());
		} else {
			lore.add(ChatColor.GOLD + "Upgrade " + toUpgrade.getName() + " to rank " + newRank);
		}
		lore.add(ChatColor.GOLD + "Up to " + toUpgrade.getModifier().getMaximumMultiplierForRank(newRank)
				+ " output multiplier");
		ItemMeta im = is.getItemMeta();
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}

}
