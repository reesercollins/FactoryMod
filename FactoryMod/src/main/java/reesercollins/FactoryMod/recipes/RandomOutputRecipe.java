package reesercollins.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;

public class RandomOutputRecipe extends InputRecipe {

	private Map<ItemMap, Double> outputs;
	private ItemMap lowestChanceMap;
	private static Random rng;

	public RandomOutputRecipe(String identifier, String name, int productionTime, ItemMap input,
			Map<ItemMap, Double> outputs, ItemMap displayOutput) {
		super(identifier, name, productionTime, input);
		this.outputs = outputs;
		if (rng == null) {
			rng = new Random();
		}
		if (displayOutput == null) {
			for (Entry<ItemMap, Double> entry : outputs.entrySet()) {
				if (lowestChanceMap == null) {
					lowestChanceMap = entry.getKey();
					continue;
				}
				if (entry.getValue() < outputs.get(lowestChanceMap)) {
					lowestChanceMap = entry.getKey();
				}
			}
			if (lowestChanceMap == null) {
				lowestChanceMap = new ItemMap(new ItemStack(Material.STONE));
			}
		} else {
			lowestChanceMap = displayOutput;
		}
	}

	public ItemMap getRandomOutput() {
		double random = rng.nextDouble();
		double count = 0.0;
		for (Entry<ItemMap, Double> entry : outputs.entrySet()) {
			count += entry.getValue();
			if (count >= random) {
				return entry.getKey();
			}
		}
		return null;
	}

	public ItemMap getDisplayMap() {
		return lowestChanceMap;
	}

	public Map<ItemMap, Double> getOutputs() {
		return outputs;
	}

	@Override
	public void applyEffect(Inventory i, ProductionFactory pf) {
		logBeforeRecipeRun(i, pf);
		ItemMap toRemove = input.clone();
		ItemMap toAdd = null;
		int counter = 0;
		while (counter < 20) {
			toAdd = getRandomOutput();
			if (toAdd != null) {
				toAdd = toAdd.clone();
				break;
			} else {
				counter++;
			}
		}
		if (toAdd == null) {
			FMPlugin.getInstance().warning(
					"Unable to find a random item to output. Recipe execution was cancelled," + pf.getLogData());
			return;
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
		List<ItemStack> items = lowestChanceMap.getItemStackRepresentation();
		for (ItemStack is : items) {
			ItemMeta isMeta = is.getItemMeta();
			isMeta.setLore(new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Randomized output")));
		}
		return items;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		ItemStack is = lowestChanceMap.getItemStackRepresentation().get(0);
		ItemMeta isMeta = is.getItemMeta();
		isMeta.setDisplayName(ChatColor.RESET + name);
		is.setItemMeta(isMeta);
		return is;
	}

	@Override
	public RecipeType getTypeIdentifier() {
		return RecipeType.RANDOM;
	}

}
