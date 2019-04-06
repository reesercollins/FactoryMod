package reesercollins.FactoryMod.builders;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.interaction.ProductionInteractionManager;
import reesercollins.FactoryMod.power.FurnacePowerManager;
import reesercollins.FactoryMod.recipes.IRecipe;
import reesercollins.FactoryMod.repair.PercentageHealthRepairManager;
import reesercollins.FactoryMod.structures.MultiBlockStructure;
import reesercollins.FactoryMod.structures.ProductionStructure;

public class ProductionBuilder implements IFactoryBuilder {

	private String name;
	private int updateTime;
	private List<IRecipe> recipes;
	private ItemStack fuel;
	private int fuelConsumptionInterval;
	private int maximumHealth;
	private long breakGracePeriod;
	private int healthPerDamagePeriod;
	private double returnRateOnDestruction;
	private double breakReduction;

	public ProductionBuilder(String name, int updateTime, List<IRecipe> recipes, ItemStack fuel,
			int fuelConsumptionInterval, double returnRateOnDestruction, int maximumHealth, long breakGracePeriod,
			int healthPerDamagePeriod, double breakReduction) {
		this.name = name;	
		this.updateTime = updateTime;
		this.recipes = recipes;
		this.fuel = fuel;
		this.breakGracePeriod = breakGracePeriod;
		this.healthPerDamagePeriod = healthPerDamagePeriod;
		this.fuelConsumptionInterval = fuelConsumptionInterval;
		this.returnRateOnDestruction = returnRateOnDestruction;
		this.maximumHealth = maximumHealth;
		this.breakReduction = breakReduction;
	}

	public int getUpdateTime() {
		return updateTime;
	}

	public ItemStack getFuel() {
		return fuel;
	}

	public List<IRecipe> getRecipes() {
		return recipes;
	}

	public void setRecipes(List<IRecipe> recipes) {
		this.recipes = recipes;
	}

	public int getFuelConsumptionInterval() {
		return fuelConsumptionInterval;
	}

	public int getMaximumHealth() {
		return maximumHealth;
	}

	public int getDamagePerDamagingPeriod() {
		return healthPerDamagePeriod;
	}

	public long getBreakGracePeriod() {
		return breakGracePeriod;
	}

	public double getBreakReduction() {
		return breakReduction;
	}

	public Factory revive(List<Location> blocks, int health, String selectedRecipe, int productionTimer, long breakTime,
			List<String> recipeStrings) {
		ProductionStructure ps = new ProductionStructure(blocks);
		FurnacePowerManager fpm = new FurnacePowerManager(ps.getFurnace(), fuel, fuelConsumptionInterval);
		ProductionInteractionManager pim = new ProductionInteractionManager();
		PercentageHealthRepairManager phrm = new PercentageHealthRepairManager(health, maximumHealth, breakTime,
				healthPerDamagePeriod, breakGracePeriod);
		List<IRecipe> currRecipes = new ArrayList<IRecipe>();
		for (String recName : recipeStrings) {
			boolean found = false;
			for (IRecipe exRec : currRecipes) {
				if (exRec.getIdentifier().equals(recName)) {
					found = true;
					break;
				}
			}
			if (!found) {
				IRecipe rec = FMPlugin.getManager().getRecipe(recName);
				if (rec == null) {
					FMPlugin.getInstance().warning("Factory at " + blocks.get(0).toString() + " had recipe " + recName
							+ " saved, but it could not be loaded from the config");
				} else {
					currRecipes.add(rec);
				}
			}
		}
		ProductionFactory pf = new ProductionFactory(pim, phrm, fpm, ps, updateTime, name, currRecipes,
				breakReduction);
		pim.setFactory(pf);
		phrm.setFactory(pf);
		for (IRecipe recipe : currRecipes) {
			if (recipe.getName().equals(selectedRecipe)) {
				pf.setRecipe(recipe);
			}
		}
		if (pf.getCurrentRecipe() == null && currRecipes.size() != 0) {
			pf.setRecipe(currRecipes.get(0));
		}
		if (productionTimer != 0) {
			pf.attemptToActivate(null, true);
			if (pf.isActive()) {
				pf.setProductionTimer(productionTimer);
			}
		}
		return pf;
	}

	@Override
	public Factory build(MultiBlockStructure mbs, Player p) {
		ProductionStructure ps = (ProductionStructure) mbs;
		FurnacePowerManager fpm = new FurnacePowerManager(ps.getFurnace(), fuel, fuelConsumptionInterval);
		ProductionInteractionManager pim = new ProductionInteractionManager();
		PercentageHealthRepairManager phrm = new PercentageHealthRepairManager(maximumHealth, maximumHealth, 0,
				healthPerDamagePeriod, breakGracePeriod);
		ProductionFactory pf = new ProductionFactory(pim, phrm, fpm, ps, updateTime, name, recipes, breakReduction);
		pim.setFactory(pf);
		phrm.setFactory(pf);
		if (recipes.size() != 0) {
			pf.setRecipe(recipes.get(0));
		}
		return pf;
	}

	@Override
	public void attemptCreation(Block b, Player p) {
		if (!FMPlugin.getManager().factoryExistsAt(b.getLocation())) {
			if (b.getType() == Material.CRAFTING_TABLE) {

			}
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getReturnRate() {
		return returnRateOnDestruction;
	}

	@Override
	public Class<ProductionStructure> getMultiBlockStructure() {
		return ProductionStructure.class;
	}

}
