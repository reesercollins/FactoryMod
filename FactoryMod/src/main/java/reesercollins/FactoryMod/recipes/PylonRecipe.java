package reesercollins.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;

public class PylonRecipe extends InputRecipe {

	private ItemMap output;
	private static int currentGlobalWeight;
	private static int globalLimit;
	private int weight;

	public PylonRecipe(String identifier, String name, int productionTime, ItemMap input, ItemMap output, int weight) {
		super(identifier, name, productionTime, input);
		this.output = output;
		this.weight = weight;
	}

	private ItemMap getCurrentOutput() {
		int weight = 0;
		Set<ProductionFactory> pylons = ProductionFactory.getPylonFactories();
		if (pylons != null) {
			// if not a single factory (not limited to pylon) is in the map,
			// this will be null
			for (ProductionFactory f : pylons) {
				if (f.isActive() && f.getCurrentRecipe() instanceof PylonRecipe) {
					weight += ((PylonRecipe) f.getCurrentRecipe()).getWeight();
				}
			}
		}
		currentGlobalWeight = weight;
		double overload = Math.max(1.0, (float) currentGlobalWeight / (float) globalLimit);
		double multiplier = 1.0 / overload;
		ItemMap actualOutput = new ItemMap();
		for (Entry<ItemStack, Integer> entry : output.getEntrySet()) {
			actualOutput.addItemAmount(entry.getKey(), (int) (entry.getValue() * multiplier));
		}
		return actualOutput;
	}

	public int getWeight() {
		return weight;
	}

	public ItemMap getOutput() {
		return output;
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
		ItemMap currOut = getCurrentOutput();
		List<ItemStack> res = new LinkedList<ItemStack>();
		for (ItemStack is : currOut.getItemStackRepresentation()) {
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GOLD + "Currently there are " + ProductionFactory.getPylonFactories() == null ? "0"
					: ProductionFactory.getPylonFactories().size() + " pylons on the map");
			lore.add(ChatColor.RED + "Current global weight is " + currentGlobalWeight);
			ItemMeta im = is.getItemMeta();
			im.setLore(lore);
			is.setItemMeta(im);
			res.add(is);
		}
		return res;
	}

	@Override
	public void applyEffect(Inventory i, ProductionFactory f) {
		if (!input.isContainedIn(i)) {
			return;
		}
		ItemMap actualOutput = getCurrentOutput();
		if (!actualOutput.fitsIn(i)) {
			return;
		}
		if (input.removeSafelyFrom(i)) {
			for (ItemStack is : actualOutput.getItemStackRepresentation()) {
				i.addItem(is);
			}
		}
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		List<ItemStack> out = output.getItemStackRepresentation();
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
		return RecipeType.PYLON;
	}

	public static void addWeight(int weight) {
		currentGlobalWeight += weight;
	}

	public static void removeWeight(int weight) {
		currentGlobalWeight -= weight;
	}

	public static void setGlobalLimit(int limit) {
		globalLimit = limit;
	}

	public static int getGlobalLimit() {
		return globalLimit;
	}
}
