package reesercollins.FactoryMod.recipes;

import org.bukkit.inventory.Inventory;

import reesercollins.FactoryMod.factories.ProductionFactory;

public interface IRecipe {

	/**
	 * @return The identifier for this recipe, which is used both internally and to
	 *         display the recipe to a player
	 */
	public String getName();

	/**
	 * @return A unique identifier for this recipe. This should not be used for
	 *         display purposes
	 */
	public String getIdentifier();

	/**
	 * @return How long this recipe takes for one run in ticks
	 */
	public int getProductionTime();

	/**
	 * Checks whether enough material is available in the given inventory to run
	 * this recipe at least once
	 * 
	 * @param i Inventory to check
	 * @return True if the recipe could be run at least once, false if not
	 */
	public boolean enoughMaterialAvailable(Inventory i);

	/**
	 * Applies whatever the recipe actually does, it's main functionality
	 * 
	 * @param i Inventory which contains the materials to work with
	 * @param f Factory which is run
	 */
	public void applyEffect(Inventory i, ProductionFactory f);

	/**
	 * Each implementation of this interface has to specify a unique identifier,
	 * which is used to identify instances of this recipe in the configuration
	 * 
	 * @return Unique identifier for the implementation
	 */
	public String getTypeIdentifier();
}
