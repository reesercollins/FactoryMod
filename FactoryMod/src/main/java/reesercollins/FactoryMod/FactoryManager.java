package reesercollins.FactoryMod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import reesercollins.FactoryMod.builders.IFactoryBuilder;
import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.recipes.IRecipe;
import reesercollins.FactoryMod.structures.MultiBlockStructure;

public class FactoryManager {

	private FMPlugin plugin;
	private HashSet<Factory> factories;
	private HashMap<Location, Factory> locations;
	private Map<String, IRecipe> recipes;
	private HashMap<String, IFactoryBuilder> builders;
	private HashSet<Material> possibleCenterBlocks;
	private HashSet<Material> possibleInteractionBlocks;
	private Material factoryInteractionMaterial;
	private boolean logInventories;
	private HashMap<Class<? extends MultiBlockStructure>, HashMap<ItemMap, IFactoryBuilder>> factoryCreationRecipes;
	private HashMap<IFactoryBuilder, ItemMap> totalSetupCosts;
	private Set<String> compactLore;

	public FactoryManager(FMPlugin plugin, Material factoryInteractionMaterial, boolean logInventories,
			Map<String, String> factoryRenames) {
		this.plugin = plugin;
		this.factoryInteractionMaterial = factoryInteractionMaterial;
		this.logInventories = logInventories;

		factories = new HashSet<Factory>();
		locations = new HashMap<Location, Factory>();
		recipes = new HashMap<String, IRecipe>();
		builders = new HashMap<String, IFactoryBuilder>();
		possibleCenterBlocks = new HashSet<Material>();
		possibleInteractionBlocks = new HashSet<Material>();
		factoryCreationRecipes = new HashMap<Class<? extends MultiBlockStructure>, HashMap<ItemMap, IFactoryBuilder>>();
		totalSetupCosts = new HashMap<IFactoryBuilder, ItemMap>();
		compactLore = new HashSet<String>();

		// Normal Furnace, Chest, Crafting Table factory
		possibleCenterBlocks.add(Material.CRAFTING_TABLE);
		possibleInteractionBlocks.add(Material.CRAFTING_TABLE);
		possibleInteractionBlocks.add(Material.FURNACE);
		possibleInteractionBlocks.add(Material.CHEST);

		// Sorter
		possibleCenterBlocks.add(Material.DROPPER);
		possibleInteractionBlocks.add(Material.DROPPER);

		// Pipe
		possibleCenterBlocks.add(Material.DISPENSER);
		possibleInteractionBlocks.add(Material.DISPENSER);
	}

	/**
	 * Sets the lore used for compacting recipes. This is needed for the compact
	 * item listeners
	 * 
	 * @param lore Lore used for compacting items
	 */
	public void addCompactLore(String lore) {
		compactLore.add(lore);
	}

	/**
	 * @return Lore given to compacted items
	 */
	public boolean isCompactLore(String lore) {
		return compactLore.contains(lore);
	}

	public boolean logInventories() {
		return logInventories;
	}

	/**
	 * @return Which material is used to interact with factories, stick by default
	 */
	public Material getFactoryInteractionMaterial() {
		return factoryInteractionMaterial;
	}

	/**
	 * Gets the setup cost for a specific factory
	 * 
	 * @param c    Class of the structure type the factory is using
	 * @param name Name of the factory
	 * @return Setup cost if the factory if it was found or null if it wasn't
	 */
	public ItemMap getSetupCost(Class<? extends MultiBlockStructure> c, String name) {
		for (Entry<ItemMap, IFactoryBuilder> entry : factoryCreationRecipes.get(c).entrySet()) {
//			if (entry.getValue().getName().equals(name)) {
//				return entry.getKey();
//			}
		}
		return null;
	}

	/**
	 * Tries to get the factory which has a part at the given location
	 * 
	 * @param loc Location which is supposed to be part of a factory
	 * @return The factory which had a block at the given location or null if there
	 *         was no factory
	 */
	public Factory getFactoryAt(Location loc) {
		return getFactoryAt(loc.getBlock());
	}

	/**
	 * Tries to get the factory which has a part at the given block
	 * 
	 * @param b Block which is supposed to be part of a factory
	 * @return The factory which had a block at the given location or null if there
	 *         was no factory
	 */
	public Factory getFactoryAt(Block b) {
		return locations.get(b.getLocation());
	}

	/**
	 * Checks whether a part of a factory is at the given location
	 * 
	 * @param loc Location to check
	 * @return True if there is a factory block, false if not
	 */
	public boolean factoryExistsAt(Location loc) {
		return getFactoryAt(loc) != null;
	}

}
