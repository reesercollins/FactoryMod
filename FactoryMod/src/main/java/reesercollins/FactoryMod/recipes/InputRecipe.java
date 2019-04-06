package reesercollins.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.utils.LoggingUtils;

public abstract class InputRecipe implements IRecipe {

	protected int productionTime;
	protected ItemMap input;
	protected int fuelConsumptionInterval = -1;
	protected String identifier;
	protected String name;

	public InputRecipe(String identifier, String name, int productionTime, ItemMap input) {
		this.productionTime = productionTime;
		this.input = input;
		this.identifier = identifier;
	}

	/**
	 * Used to get a representation of a recipes input materials, which is displayed
	 * in an item GUI to illustrate the recipe and to give additional information.
	 * If null is given instead of an inventory or factory, just general information
	 * should be returned, which doesn't depend on a specific instance
	 * 
	 * @param i    Inventory for which the recipe would be run, this is used to add
	 *             lore to the items, which tells how often the recipe could be run
	 * @param fccf Factory for which the representation is meant. Needed for recipe
	 *             run scaling
	 * @return List of ItemStacks which represent the input required to run this
	 *         recipe
	 */
	public abstract List<ItemStack> getInputRepresentation(Inventory i, ProductionFactory pf);

	/**
	 * Used to get a representation of a recipes output materials, which is
	 * displayed in an item GUI to illustrate the recipe and to give additional
	 * information. If null is given instead of an inventory or factory, just
	 * general information should be returned, which doesn't depend on a specific
	 * instance
	 * 
	 * @param i    Inventory for which the recipe would be run, this is used to add
	 *             lore to the items, which tells how often the recipe could be run
	 * @param fccf Factory for which the representation is meant. Needed for recipe
	 *             run scaling
	 * @return List of ItemStacks which represent the output returned when running
	 *         this recipe
	 */
	public abstract List<ItemStack> getOutputRepresentation(Inventory i, ProductionFactory pf);

	public int getFuelConsumptionInterval() {
		return fuelConsumptionInterval;
	}

	public void setFuelConsumptionInterval(int interval) {
		this.fuelConsumptionInterval = interval;
	}

	public int getProductionTime() {
		return productionTime;
	}

	public ItemMap getInput() {
		return input;
	}

	public boolean enoughMaterialAvailable(Inventory i) {
		return input.isContainedIn(i);
	}
	
	public String getName() {
		return name;
	}

	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @return A single ItemStack which is used to represent this recipe as a whole
	 *         in an item GUI
	 */
	public abstract ItemStack getRecipeRepresentation();

	/**
	 * Creates a list of ItemStack for a GUI representation. This list contains all
	 * the ItemStacks contained in the ItemStack representation of the input map and
	 * adds to each of the stacks how many runs could be made with the material
	 * available in the chest
	 * 
	 * @param i Inventory to calculate the possible runs for
	 * @return ItemStacks containing the additional information, ready for the GUI
	 */
	protected List<ItemStack> createLoredStacksForInfo(Inventory i) {
		LinkedList<ItemStack> result = new LinkedList<ItemStack>();
		ItemMap inventoryMap = new ItemMap(i);
		ItemMap possibleRuns = new ItemMap();
		for (Entry<ItemStack, Integer> entry : input.getEntrySet()) {
			if (inventoryMap.getAmount(entry.getKey()) != 0) {
				possibleRuns.addItemAmount(entry.getKey(), inventoryMap.getAmount(entry.getKey()) / entry.getValue());
			} else {
				possibleRuns.addItemAmount(entry.getKey(), 0);

			}
		}

		for (ItemStack is : input.getItemStackRepresentation()) {
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GREEN + "Enough materials for " + String.valueOf(possibleRuns.getAmount(is)) + " runs");
			
			ItemMeta im = is.getItemMeta();
			im.setLore(lore);
			is.setItemMeta(im);
			result.add(is);
		}
		return result;
	}

	protected void logBeforeRecipeRun(Inventory i, Factory f) {
		LoggingUtils.logInventory(i, "Before executing recipe " + getName() + " for " + f.getLogData());
	}

	protected void logAfterRecipeRun(Inventory i, Factory f) {
		LoggingUtils.logInventory(i, "After executing recipe " + getName() + " for " + f.getLogData());
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

}
