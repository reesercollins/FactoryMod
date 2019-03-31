package reesercollins.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.repair.PercentageHealthRepairManager;
import reesercollins.FactoryMod.utils.LoggingUtils;

public class RepairRecipe extends InputRecipe {

	private int healthPerRun;

	public RepairRecipe(String identifier, int productionTime, ItemMap input, int healthPerRun) {
		super(identifier, productionTime, input);
		this.healthPerRun = healthPerRun;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i, ProductionFactory pf) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		ItemStack apple = new ItemStack(Material.GOLDEN_APPLE);
		ItemMeta appleMeta = apple.getItemMeta();
		appleMeta.setLore(
				new ArrayList<String>(Arrays.asList(ChatColor.GREEN + "+" + String.valueOf(healthPerRun) + " health")));
		apple.setItemMeta(appleMeta);
		result.add(apple);
		return result;
	}

	public List<ItemStack> getInputRepresentation(Inventory i, ProductionFactory pf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	public void applyEffect(Inventory i, ProductionFactory pf) {
		logBeforeRecipeRun(i, pf);
		if (enoughMaterialAvailable(i)) {
			if (input.removeSafelyFrom(i)) {
				((PercentageHealthRepairManager) (pf.getRepairManager())).repair(healthPerRun);
				LoggingUtils.log(((PercentageHealthRepairManager) (pf.getRepairManager())).getHealth() + " for "
						+ pf.getLogData() + " after repairing");
			}
		}
		logAfterRecipeRun(i, pf);
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.GOLDEN_APPLE);
		ItemMeta resResult = res.getItemMeta();
		resResult.setDisplayName(getType().toString());
		res.setItemMeta(resResult);
		return res;
	}

	public int getHealth() {
		return healthPerRun;
	}

	@Override
	public RecipeType getType() {
		return RecipeType.REPAIR;
	}

}
