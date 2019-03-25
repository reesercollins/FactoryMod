package reesercollins.FactoryMod.events;

import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.recipes.InputRecipe;

public class RecipeExecuteEvent extends FactoryModEvent {

	private ProductionFactory pf;
	private InputRecipe input;

	public RecipeExecuteEvent(ProductionFactory pf, InputRecipe input) {
		this.pf = pf;
		this.input = input;
	}

	/**
	 * @return The factory executing the recipe
	 */
	public ProductionFactory getFactory() {
		return pf;
	}

	/**
	 * @return The recipe being executed
	 */
	public InputRecipe getRecipe() {
		return input;
	}

}
