package reesercollins.FactoryMod.factories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import reesercollins.FactoryMod.events.FactoryActivateEvent;
import reesercollins.FactoryMod.events.RecipeExecuteEvent;
import reesercollins.FactoryMod.interaction.IInteractionManager;
import reesercollins.FactoryMod.power.FurnacePowerManager;
import reesercollins.FactoryMod.power.IPowerManager;
import reesercollins.FactoryMod.recipes.IRecipe;
import reesercollins.FactoryMod.recipes.InputRecipe;
import reesercollins.FactoryMod.recipes.PylonRecipe;
import reesercollins.FactoryMod.recipes.RecipeScalingUpgradeRecipe;
import reesercollins.FactoryMod.recipes.RepairRecipe;
import reesercollins.FactoryMod.recipes.UpgradeRecipe;
import reesercollins.FactoryMod.repair.IRepairManager;
import reesercollins.FactoryMod.repair.PercentageHealthRepairManager;
import reesercollins.FactoryMod.structures.ProductionStructure;
import reesercollins.FactoryMod.utils.LoggingUtils;

public class ProductionFactory extends Factory {

	protected int currentProductionTimer = 0;
	protected List<IRecipe> recipes;
	protected IRecipe currentRecipe;
	protected Map<IRecipe, Integer> runCount;
	protected Map<IRecipe, Integer> recipeLevel;
	private UUID activator;
	private double citadelBreakReduction;
	private boolean autoSelect;

	private static HashSet<ProductionFactory> pylonFactories;

	public ProductionFactory(IInteractionManager im, IRepairManager rm, IPowerManager ipm, ProductionStructure mbs,
			int updateTime, String name, List<IRecipe> recipes, double citadelBreakReduction) {
		super(im, rm, ipm, mbs, updateTime, name);
		this.active = false;
		this.runCount = new HashMap<IRecipe, Integer>();
		this.recipeLevel = new HashMap<IRecipe, Integer>();
		this.recipes = new ArrayList<IRecipe>();
		this.citadelBreakReduction = citadelBreakReduction;
		this.autoSelect = false;
		for (IRecipe rec : recipes) {
			addRecipe(rec);
		}
		if (pylonFactories == null) {
			pylonFactories = new HashSet<ProductionFactory>();
		}
		for (IRecipe rec : recipes) {
			if (rec instanceof PylonRecipe) {
				pylonFactories.add(this);
				break;
			}
		}
	}

	/**
	 * @return Inventory of the chest or null if there is no chest where one should
	 *         be
	 */
	public Inventory getInventory() {
		if (!(getChest().getType() == Material.CHEST)) {
			return null;
		}
		Chest chestBlock = (Chest) (getChest().getState());
		return chestBlock.getInventory();
	}

	/**
	 * @return Inventory of the furnace or null if there is no furnace where one
	 *         should be
	 */
	public FurnaceInventory getFurnaceInventory() {
		if (getFurnace().getType() != Material.FURNACE) {
			return null;
		}
		Furnace furnaceBlock = (Furnace) (getFurnace().getState());
		return furnaceBlock.getInventory();
	}

	/**
	 * Sets autoselect mode for this factory
	 * 
	 * @param mode Whether autoselect should be set to true or false
	 */
	public void setAutoSelect(boolean mode) {
		this.autoSelect = mode;
	}

	/**
	 * @return Whether the factory is in auto select mode
	 */
	public boolean isAutoSelect() {
		return autoSelect;
	}

	/**
	 * Attempts to turn the factory on and does all the checks needed to ensure that
	 * the factory is allowed to turn on
	 */
	public void attemptToActivate(Player p, boolean onStartUp) {
		LoggingUtils.log((p != null ? p.getName() : "Redstone") + " is attempting to activate " + getLogData());
		mbs.checkIsValid();
		// don't activate twice
		if (active) {
			return;
		}
		// ensure factory is physically complete
		if (!mbs.isValid()) {
			rm.breakIt();
			return;
		}
		// ensure enough materials for the recipe are available
		if (!hasInputMaterials()) {
			if (!isAutoSelect()) {
				if (p != null) {
					p.sendMessage(ChatColor.RED + "Not enough materials available");
				}
				return;
			} else {
				// handle autoselect
				IRecipe autoSelected = getAutoSelectRecipe();
				if (autoSelected == null) {
					if (p != null) {
						p.sendMessage(ChatColor.RED + "Not enough materials available to run any recipe");
					}
					return;
				} else {
					if (p != null) {
						p.sendMessage(ChatColor.GOLD + "Automatically selected recipe " + autoSelected.getName());
					}
					setRecipe(autoSelected);
				}
			}
		}
		// ensure we have fuel
		if (!pm.powerAvailable()) {
			if (p != null) {
				p.sendMessage(ChatColor.RED + "Failed to activate factory, there is no fuel in the furnace");
			}
			return;
		}
		// ensure factory isnt in disrepair
		if (rm.inDisrepair() && !(currentRecipe instanceof RepairRecipe)) {
			if (p != null) {
				p.sendMessage(ChatColor.RED + "This factory is in disrepair, you have to repair it before using it");
			}
			return;
		}
		if (currentRecipe instanceof RepairRecipe && rm.atFullHealth()) {
			if (p != null) {
				p.sendMessage("This factory is already at full health!");
			}
			return;
		}
//		if (!onStartUp && currentRecipe instanceof UpgradeRecipe && FMPlugin.getManager().isCitadelEnabled()) {
//			// only allow permitted members to upgrade the factory
//			ReinforcementManager rm = Citadel.getReinforcementManager();
//			PlayerReinforcement rein = (PlayerReinforcement) rm.getReinforcement(mbs.getCenter());
//			if (rein != null) {
//				if (p == null) {
//					return;
//				}
//				if (!NameAPI.getGroupManager().hasAccess(rein.getGroup().getName(), p.getUniqueId(),
//						PermissionType.getPermission("UPGRADE_FACTORY"))) {
//					p.sendMessage(ChatColor.RED + "You dont have permission to upgrade this factory");
//					return;
//				}
//			}
//		}
		FactoryActivateEvent fae = new FactoryActivateEvent(this, p);
		Bukkit.getPluginManager().callEvent(fae);
		if (fae.isCancelled()) {
			return;
		}
		if (p != null) {
			int consumptionInterval = ((InputRecipe) currentRecipe).getFuelConsumptionInterval() != -1
					? ((InputRecipe) currentRecipe).getFuelConsumptionInterval()
					: pm.getPowerConsumptionInterval();
			if (((FurnacePowerManager) pm)
					.getFuelAmountAvailable() < (currentRecipe.getProductionTime() / consumptionInterval)) {
				p.sendMessage(
						ChatColor.RED + "You don't have enough fuel, the factory will run out of it before completing");
			}
			p.sendMessage(ChatColor.GREEN + "Activated " + name + " with recipe: " + currentRecipe.getName());
			activator = p.getUniqueId();
		}
		activate();
	}

	/**
	 * Actually turns the factory on, never use this directly unless you know what
	 * you are doing, use attemptToActivate() instead to ensure the factory is
	 * allowed to turn on
	 */
	public void activate() {
		LoggingUtils.log("Activating " + getLogData());
		active = true;
		pm.setPowerCounter(0);
		turnFurnaceOn(getFurnace());
		// reset the production timer
		currentProductionTimer = 0;
		run();
	}

	/**
	 * Turns the factory off.
	 */
	public void deactivate() {
		LoggingUtils.log("Deactivating " + getLogData());
		if (active) {
			Bukkit.getScheduler().cancelTask(threadId);
			turnFurnaceOff(getFurnace());
			active = false;
			// reset the production timer
			currentProductionTimer = 0;
			activator = null;
		}
	}

	/**
	 * @return The furnace of this factory
	 */
	public Block getFurnace() {
		return ((ProductionStructure) mbs).getFurnace();
	}

	/**
	 * @return The chest of this factory
	 */
	public Block getChest() {
		return ((ProductionStructure) mbs).getChest();
	}

	/**
	 * @return The crafting table of this factory
	 */
	public Block getCraftingTable() {
		return ((ProductionStructure) mbs).getCraftingTable();
	}

	/**
	 * @return How long the factory has been running in ticks
	 */
	public int getRunningTime() {
		return currentProductionTimer;
	}

	public void setRunCount(IRecipe r, Integer count) {
		if (recipes.contains(r)) {
			runCount.put(r, count);
		}
	}

	public void setRecipeLevel(IRecipe r, Integer level) {
		if (recipes.contains(r)) {
			recipeLevel.put(r, level);
		}
	}

	/**
	 * @return UUID of the person who activated the factory or null if the factory
	 *         is off or was triggered by redstone
	 */
	public UUID getActivator() {
		return activator;
	}

	public void setActivator(UUID uuid) {
		this.activator = uuid;
	}

	/**
	 * Called by the manager each update cycle
	 */
	public void run() {
		if (active && mbs.isValid()) {
			// if the materials required to produce the current recipe are in
			// the factory inventory
			if (hasInputMaterials()) {
				// if the factory has been working for less than the required
				// time for the recipe
				if (currentProductionTimer < currentRecipe.getProductionTime()) {
					// if the factory power source inventory has enough fuel for
					// at least 1 energyCycle
					if (pm.powerAvailable()) {
						// check whether the furnace is on, minecraft sometimes
						// turns it off
						Furnace furnace = (Furnace) getFurnace().getState();
						if (furnace.getBurnTime() == 0) {
							turnFurnaceOn(getFurnace());
						}
						// if the time since fuel was last consumed is equal to
						// how often fuel needs to be consumed
						int consumptionIntervall = ((InputRecipe) currentRecipe).getFuelConsumptionInterval() != -1
								? ((InputRecipe) currentRecipe).getFuelConsumptionInterval()
								: pm.getPowerConsumptionInterval();
						if (pm.getPowerCounter() >= consumptionIntervall - 1) {
							// remove one fuel.
							pm.consumePower();
							// 0 seconds since last fuel consumption
							pm.setPowerCounter(0);
						}
						// if we don't need to consume fuel, just increase the
						// energy timer
						else {
							pm.increasePowerCounter(updateTime);
						}
						// increase the production timer
						currentProductionTimer += updateTime;
						// schedule next update
						scheduleUpdate();
					}
					// if there is no fuel Available turn off the factory
					else {
						sendActivatorMessage(ChatColor.GOLD + name + " deactivated, because it ran out of fuel");
						deactivate();
					}
				}

				// if the production timer has reached the recipes production
				// time remove input from chest, and add output material
				else if (currentProductionTimer >= currentRecipe.getProductionTime()) {
					LoggingUtils.log("Executing recipe " + currentRecipe.getName() + " for " + getLogData());
					RecipeExecuteEvent ree = new RecipeExecuteEvent(this, (InputRecipe) currentRecipe);
					Bukkit.getPluginManager().callEvent(ree);
					if (ree.isCancelled()) {
						LoggingUtils.log("Executing recipe " + currentRecipe.getName() + " for " + getLogData()
								+ " was cancelled over the event");
						deactivate();
						return;
					}
					sendActivatorMessage(
							ChatColor.GOLD + currentRecipe.getName().toString() + " in " + name + " completed");
					if (currentRecipe instanceof UpgradeRecipe || currentRecipe instanceof RecipeScalingUpgradeRecipe) {
						// this if else might look a bit weird, but because
						// upgrading changes the current recipe and a lot of
						// other stuff, this is needed
						currentRecipe.applyEffect(getInventory(), this);
						deactivate();
						return;
					} else {
						currentRecipe.applyEffect(getInventory(), this);
						runCount.put(currentRecipe, runCount.get(currentRecipe) + 1);
					}
					currentProductionTimer = 0;
					if (currentRecipe instanceof RepairRecipe && rm.atFullHealth()) {
						// already at full health, dont try to repair further
						sendActivatorMessage(ChatColor.GOLD + name + " repaired to full health");
						deactivate();
						return;
					}
					if (pm.powerAvailable()) {
						// not enough materials, but if auto select is on, we might find another recipe
						// to run
						if (!hasInputMaterials() && isAutoSelect()) {
							IRecipe nextOne = getAutoSelectRecipe();
							if (nextOne != null) {
								sendActivatorMessage(ChatColor.GREEN + name + " automatically switched to recipe "
										+ nextOne.getName() + " and began running it");
								currentRecipe = nextOne;
							} else {
								deactivate();
								return;
							}
						}
						pm.setPowerCounter(0);
						scheduleUpdate();
						// keep going
					} else {
						deactivate();
					}
				}
			} else {
				sendActivatorMessage(ChatColor.GOLD + name + " deactivated, because it ran out of required materials");
				deactivate();
			}
		} else {
			sendActivatorMessage(ChatColor.GOLD + name + " deactivated, because the factory was destroyed");
			deactivate();
		}
	}

	/**
	 * @return All the recipes which are available for this instance
	 */
	public List<IRecipe> getRecipes() {
		return recipes;
	}

	/**
	 * Pylon recipes have a special functionality, which requires them to know all
	 * other factories with pylon recipes on the map. Because of that all of those
	 * factories are kept in a separated hashset, which is provided by this method
	 * 
	 * @return All factories with a pylon recipe
	 */
	public static HashSet<ProductionFactory> getPylonFactories() {
		return pylonFactories;
	}

	/**
	 * @return The recipe currently selected in this instance
	 */
	public IRecipe getCurrentRecipe() {
		return currentRecipe;
	}

	/**
	 * Changes the current recipe for this factory to the given one
	 * 
	 * @param pr Recipe to switch to
	 */
	public void setRecipe(IRecipe pr) {
		if (recipes.contains(pr)) {
			currentRecipe = pr;
		}
	}

	public int getRunCount(IRecipe r) {
		return runCount.get(r);
	}

	public int getRecipeLevel(IRecipe r) {
		return recipeLevel.get(r);
	}

	private void sendActivatorMessage(String msg) {
		if (activator != null) {
			Player p = Bukkit.getPlayer(activator);
			if (p != null) {
				p.sendMessage(msg);
			}
		}
	}

	/**
	 * Adds the given recipe to this factory
	 * 
	 * @param rec Recipe to add
	 */
	public void addRecipe(IRecipe rec) {
		recipes.add(rec);
		runCount.put(rec, 0);
		recipeLevel.put(rec, 1);
	}

	/**
	 * Removes the given recipe from this factory
	 * 
	 * @param rec Recipe to remove
	 */
	public void removeRecipe(IRecipe rec) {
		recipes.remove(rec);
		runCount.remove(rec);
		recipeLevel.remove(rec);
	}

	/**
	 * Sets the internal production timer
	 * 
	 * @param timer New timer
	 */
	public void setProductionTimer(int timer) {
		this.currentProductionTimer = timer;
	}

	/**
	 * @return Whether enough materials are available to run the currently selected
	 *         recipe at least once
	 */
	public boolean hasInputMaterials() {
		return currentRecipe.enoughMaterialAvailable(getInventory());
	}

	public IRecipe getAutoSelectRecipe() {
		for (IRecipe rec : recipes) {
			if (rec.enoughMaterialAvailable(getInventory())) {
				return rec;
			}
		}
		return null;
	}

	public static void removePylon(Factory f) {
		pylonFactories.remove(f);
	}

	public void upgrade(String name, List<IRecipe> recipes, ItemStack fuel, int fuelConsumptionIntervall,
			int updateTime, int maximumHealth, int damageAmountPerDecayIntervall, long gracePeriod,
			double citadelBreakReduction) {
		LoggingUtils.log("Upgrading " + getLogData() + " to " + name);
		pylonFactories.remove(this);
		deactivate();
		this.name = name;
		this.recipes = recipes;
		this.updateTime = updateTime;
		this.citadelBreakReduction = citadelBreakReduction;
		this.pm = new FurnacePowerManager(getFurnace(), fuel, fuelConsumptionIntervall);
		this.rm = new PercentageHealthRepairManager(maximumHealth, maximumHealth, 0, damageAmountPerDecayIntervall,
				gracePeriod);
		if (recipes.size() != 0) {
			setRecipe(recipes.get(0));
		} else {
			currentRecipe = null;
		}
		runCount = new HashMap<IRecipe, Integer>();
		for (IRecipe rec : recipes) {
			runCount.put(rec, 0);
		}
		for (IRecipe rec : recipes) {
			if (rec instanceof PylonRecipe) {
				pylonFactories.add(this);
				break;
			}
		}
	}

	public double getCitadelBreakReduction() {
		return citadelBreakReduction;
	}

}
