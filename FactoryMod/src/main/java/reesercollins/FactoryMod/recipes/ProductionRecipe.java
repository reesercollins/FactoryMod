package reesercollins.FactoryMod.recipes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.recipes.scaling.ProductionRecipeModifier;

public class ProductionRecipe extends InputRecipe {

	private ItemMap output;
	private ProductionRecipeModifier modifier;
	private Random rng;
	private DecimalFormat decimalFormatting;

	public ProductionRecipe(String identifier, String name, int productionTime, ItemMap input, ItemMap output,
			ProductionRecipeModifier modifier) {
		super(identifier, name, productionTime, input);
		this.output = output;
		this.modifier = modifier;
		this.rng = new Random();
		this.decimalFormatting = new DecimalFormat("#.####");
	}

	public ItemMap getOutput() {
		return output;
	}

	public ProductionRecipeModifier getModifier() {
		return modifier;
	}

	public ItemMap getAdjustedOutput(int rank, int runs) {
		ItemMap im = output.clone();
		if (modifier != null) {
			im.multiplyContent(modifier.getFactor(rank, runs));
			return im;
		}
		return im;
	}

	public ItemMap getGuaranteedOutput(int rank, int runs) {
		if (modifier == null) {
			return output.clone();
		}
		ItemMap adjusted = new ItemMap();
		double factor = modifier.getFactor(rank, runs);
		for (Entry<ItemStack, Integer> entry : output.getEntrySet()) {
			adjusted.addItemAmount(entry.getKey(), (int) (Math.floor(entry.getValue() * factor)));
		}
		return adjusted;
	}

	public int getCurrentMultiplier(Inventory i) {
		return input.getMultiplesContainedIn(i);
	}

	@Override
	public void applyEffect(Inventory i, ProductionFactory pf) {
		logBeforeRecipeRun(i, pf);
		ItemMap toRemove = input.clone();
		ItemMap toAdd;
		if (getModifier() == null) {
			toAdd = output.clone();
		} else {
			toAdd = getGuaranteedOutput(pf.getRecipeLevel(this), pf.getRunCount(this));
			double factor = modifier.getFactor(pf.getRecipeLevel(this), pf.getRunCount(this));
			for (Entry<ItemStack, Integer> entry : output.getEntrySet()) {
				double additionalChance = (((double) entry.getValue()) * factor) - toAdd.getAmount(entry.getKey());
				if (rng.nextDouble() <= additionalChance) {
					toAdd.addItemAmount(entry.getKey(), 1);
				}
			}
		}

		if (toRemove.isContainedIn(i)) {
			if (toRemove.removeSafelyFrom(i)) {
				for (ItemStack is : toAdd.getItemStackRepresentation()) {
					i.addItem(is);
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
		if (i == null || pf == null || modifier == null) {
			return output.getItemStackRepresentation();
		}
		ItemMap currentOut = getGuaranteedOutput(pf.getRecipeLevel(this), pf.getRunCount(this));
		List<ItemStack> stacks = currentOut.getItemStackRepresentation();
		double factor = modifier.getFactor(pf.getRecipeLevel(this), pf.getRunCount(this));
		for (Entry<ItemStack, Integer> entry : output.getEntrySet()) {
			double additionalChance = (((double) entry.getValue()) * factor) - currentOut.getAmount(entry.getKey());
			if (Math.abs(additionalChance) > 0.00000001) {
				ItemStack is = entry.getKey().clone();
				ItemMeta im = is.getItemMeta();
				im.setLore(new ArrayList<String>(Arrays.asList(
						ChatColor.GOLD + decimalFormatting.format(additionalChance) + " chance for additional item")));
				is.setItemMeta(im);
				stacks.add(is);
			}
		}
		int possibleRuns = input.getMultiplesContainedIn(i);
		for (ItemStack is : stacks) {
			ItemMeta im = is.getItemMeta();
			im.setLore(new ArrayList<String>(Arrays.asList(ChatColor.GREEN + "Enough materials for " + String.valueOf(possibleRuns) + " runs")));
			is.setItemMeta(im);
		}
		return stacks;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		List<ItemStack> out = getOutput().getItemStackRepresentation();
		ItemStack res;
		if (out.size() == 0) {
			res = new ItemStack(Material.STONE);
		} else {
			res = out.get(0);
		}
		ItemMeta im = res.getItemMeta();
		im.setDisplayName(ChatColor.RESET + name);
		res.setItemMeta(im);
		return res;
	}

	@Override
	public RecipeType getTypeIdentifier() {
		return RecipeType.PRODUCTION;
	}

}
