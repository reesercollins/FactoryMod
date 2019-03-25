package reesercollins.FactoryMod.recipes;

import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;

public class RecipeScalingUpgradeRecipe extends InputRecipe {

	public RecipeScalingUpgradeRecipe(String identifier, String name, int productionTime, ItemMap input) {
		super(identifier, name, productionTime, input);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void applyEffect(Inventory i, ProductionFactory f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTypeIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, ProductionFactory pf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, ProductionFactory pf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

}
