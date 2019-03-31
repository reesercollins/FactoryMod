package reesercollins.FactoryMod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import reesercollins.FactoryMod.builders.IFactoryBuilder;
import reesercollins.FactoryMod.builders.ProductionBuilder;
import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.factories.Factory.FactoryType;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.recipes.IRecipe;
import reesercollins.FactoryMod.recipes.UpgradeRecipe;
import reesercollins.FactoryMod.structures.MultiBlockStructure;
import reesercollins.FactoryMod.structures.ProductionStructure;
import reesercollins.FactoryMod.utils.FileManager;
import reesercollins.FactoryMod.utils.LoggingUtils;

public class FactoryManager {

	private FMPlugin plugin;
	private FileManager fileManager;
	private HashSet<Factory> factories;
	private HashMap<Location, Factory> locations;
	private Map<String, IRecipe> recipes;
	private HashMap<FactoryType, IFactoryBuilder> builders;
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
		this.fileManager = new FileManager(this, factoryRenames);
		this.factoryInteractionMaterial = factoryInteractionMaterial;
		this.logInventories = logInventories;

		factories = new HashSet<Factory>();
		locations = new HashMap<Location, Factory>();
		recipes = new HashMap<String, IRecipe>();
		builders = new HashMap<FactoryType, IFactoryBuilder>();
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
			if (entry.getValue().getType().equals(name)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Adds a factory and the locations of its blocks to the manager
	 * 
	 * @param f Factory to add
	 */
	public void addFactory(Factory f) {
		factories.add(f);
		for (Block b : f.getMultiBlockStructure().getReleventBlocks()) {
			locations.put(b.getLocation(), f);
		}
	}

	/**
	 * Removes a factory from the manager
	 * 
	 * @param f Factory to remove
	 */
	public void removeFactory(Factory f) {
		if (f.isActive()) {
			f.deactivate();
		}
		factories.remove(f);
		ProductionFactory.removePylon(f);
		for (Location b : f.getMultiBlockStructure().getAllBlocks()) {
			locations.remove(b);
		}
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
	 * Gets all the factories within a certain range of a given location
	 * 
	 * @param l     Location on which the search is centered
	 * @param range maximum distance from the center allowed
	 * @return All of the factories which are less or equal than the given range
	 *         away from the given location
	 */
	public List<Factory> getNearbyFactories(Location l, int range) {
		List<Factory> facs = new LinkedList<Factory>();
		for (Factory f : factories) {
			if (f.getMultiBlockStructure().getCenter().distance(l) <= range) {
				facs.add(f);
			}
		}
		return facs;
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

	/**
	 * Adds a factory builder to the manager and associates it with a specific setup
	 * cost in items and a specific MultiBlockStructure which is the physical
	 * representation of the factory created by the builder.
	 * 
	 * @param blockStructureClass Class inheriting from MultiBlockStructure, which
	 *                            physically represents the factories created by the
	 *                            builder
	 * @param recipe              Item cost to create the factory
	 * @param builder             Encapsulates the factory itself
	 */
	public void addFactoryCreationBuilder(Class<? extends MultiBlockStructure> blockStructureClass, ItemMap recipe,
			IFactoryBuilder builder) {
		HashMap<ItemMap, IFactoryBuilder> builders = factoryCreationRecipes.get(blockStructureClass);
		if (builders == null) {
			builders = new HashMap<ItemMap, IFactoryBuilder>();
			factoryCreationRecipes.put(blockStructureClass, builders);
		}
		builders.put(recipe, builder);
		this.builders.put(builder.getType(), builder);
	}

	public void addFactoryUpgradeBuilder(IFactoryBuilder builder) {
		builders.put(builder.getType(), builder);
	}

	public ItemMap getTotalSetupCost(Factory f) {
		return getTotalSetupCost(getBuilder(f.getType()));
	}

	public ItemMap getTotalSetupCost(IFactoryBuilder b) {
		return totalSetupCosts.get(b);
	}

	public void calculateTotalSetupCosts() {
		for (HashMap<ItemMap, IFactoryBuilder> maps : factoryCreationRecipes.values()) {
			for (Entry<ItemMap, IFactoryBuilder> entry : maps.entrySet()) {
				totalSetupCosts.put(entry.getValue(), entry.getKey());
			}
		}
		for (IFactoryBuilder builder : getAllBuilders().values()) {
			totalSetupCosts.put(builder, calculateTotalSetupCost(builder));
		}
	}

	private ItemMap calculateTotalSetupCost(IFactoryBuilder builder) {
		ItemMap map = null;
		map = totalSetupCosts.get(builder);
		if (map != null) {
			return map;
		}
		for (IFactoryBuilder superBuilder : getAllBuilders().values()) {
			if (superBuilder instanceof ProductionBuilder) {
				for (IRecipe recipe : ((ProductionBuilder) superBuilder).getRecipes()) {
					if (recipe instanceof UpgradeRecipe && ((UpgradeRecipe) recipe).getBuilder() == builder) {
						map = calculateTotalSetupCost(superBuilder);
						if (map == null) {
							plugin.warning("Could not calculate total setupcost for " + builder.getType()
									+ ". It's parent factory  " + superBuilder.getType() + " is impossible to set up");
							break;
						}
						map = map.clone(); // so we dont mess with the original
											// setup costs
						map.merge(((UpgradeRecipe) recipe).getInput());
						return map;
					}
				}

			}
		}
		return map;
	}

	/**
	 * Attempts to create a factory with the given block as new center block. If all
	 * blocks for a specific structure are there and other conditions needed for the
	 * factory type are fullfilled, the factory is created and added to the manager
	 * 
	 * @param b Center block
	 * @param p Player attempting to create the factory
	 */
	public void attemptCreation(Block b, Player p) {
		// this method should probably be taken apart and the individual logic should be
		// exported in
		// a class that fits each factory type
		if (!factoryExistsAt(b.getLocation())) {
			// Cycle through possible structures here
			if (b.getType() == Material.CRAFTING_TABLE) {
				ProductionStructure ps = new ProductionStructure(b);
				if (ps.isValid()) {
					if (ps.blockedByExistingFactory()) {
						p.sendMessage(ChatColor.RED
								+ "At least one of the blocks of this factory is already part of another factory");
						return;
					}
					HashMap<ItemMap, IFactoryBuilder> builders = factoryCreationRecipes.get(ProductionStructure.class);
					if (builders != null) {
						IFactoryBuilder builder = null;
						for (Entry<ItemMap, IFactoryBuilder> entry : builders.entrySet()) {
							if (entry.getKey()
									.containedExactlyIn(((Chest) (ps.getChest().getState())).getInventory())) {
								builder = entry.getValue();
								break;
							}
						}
						if (builder != null) {
							Factory f = builder.build(ps, p);
							if (f != null) {
								((Chest) (ps.getChest().getState())).getInventory().clear();
								addFactory(f);
								p.sendMessage(ChatColor.GREEN + "Successfully created " + f.getType());
								LoggingUtils.log(f.getLogData() + " was created by " + p.getName());
							}
						} else {
							p.sendMessage(ChatColor.RED + "There is no factory with the given creation materials");
						}
					}
					return;
				}
			}
//			if (b.getType() == Material.DISPENSER) {
//				PipeStructure ps = new PipeStructure(b);
//				if (ps.isComplete()) {
//					if (ps.blockedByExistingFactory()) {
//						p.sendMessage(ChatColor.RED
//								+ "At least one of the blocks of this factory is already part of another factory");
//						return;
//					}
//					HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.get(PipeStructure.class);
//					if (eggs != null) {
//						IFactoryEgg egg = null;
//						for (Entry<ItemMap, IFactoryEgg> entry : eggs.entrySet()) {
//							if (entry.getKey()
//									.containedExactlyIn((((Dispenser) (ps.getStart().getState())).getInventory()))) {
//								egg = entry.getValue();
//								break;
//							}
//						}
//						if (egg != null) {
//							if (ps.getGlassColor() != ((PipeEgg) egg).getColor()) {
//								p.sendMessage(ChatColor.RED + "You dont have the right color of glass for this pipe");
//								return;
//							}
//							if (ps.getLength() > ((PipeEgg) egg).getMaximumLength()) {
//								p.sendMessage(ChatColor.RED + "You cant make pipes of this type, which are that long");
//								return;
//							}
//							Factory f = egg.hatch(ps, p);
//							if (f != null) {
//								((Dispenser) (ps.getStart().getState())).getInventory().clear();
//								addFactory(f);
//								p.sendMessage(ChatColor.GREEN + "Successfully created " + f.getName());
//								LoggingUtils.log(f.getLogData() + " was created by " + p.getName());
//								FactoryMod.sendResponse("PipeCreation", p);
//							}
//
//						} else {
//							p.sendMessage(ChatColor.RED + "There is no pipe with the given creation materials");
//							FactoryMod.sendResponse("WrongPipeCreationItems", p);
//						}
//					}
//					return;
//				} else {
//					p.sendMessage(ChatColor.RED + "This pipe is not set up the right way");
//					FactoryMod.sendResponse("WrongPipeBlockSetup", p);
//				}
//			}
//			if (b.getType() == Material.DROPPER) {
//				BlockFurnaceStructure bfs = new BlockFurnaceStructure(b);
//				if (bfs.isComplete()) {
//					if (bfs.blockedByExistingFactory()) {
//						p.sendMessage(ChatColor.RED
//								+ "At least one of the blocks of this factory is already part of another factory");
//						return;
//					}
//					HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.get(BlockFurnaceStructure.class);
//					if (eggs != null) {
//						IFactoryEgg egg = null;
//						for (Entry<ItemMap, IFactoryEgg> entry : eggs.entrySet()) {
//							if (entry.getKey().containedExactlyIn(
//									((Dropper) (bfs.getCenter().getBlock().getState())).getInventory())) {
//								egg = entry.getValue();
//								break;
//							}
//						}
//						if (egg != null) {
//							Factory f = egg.hatch(bfs, p);
//							if (f != null) {
//								((Dropper) (bfs.getCenter().getBlock().getState())).getInventory().clear();
//								addFactory(f);
//								p.sendMessage(ChatColor.GREEN + "Successfully created " + f.getName());
//								LoggingUtils.log(f.getLogData() + " was created by " + p.getName());
//								FactoryMod.sendResponse("SorterCreation", p);
//							}
//
//						} else {
//							p.sendMessage(ChatColor.RED + "There is no sorter with the given creation materials");
//							FactoryMod.sendResponse("WrongSorterCreationItems", p);
//						}
//					}
//				} else {
//					p.sendMessage(ChatColor.RED + "This sorter is not set up the right way");
//					FactoryMod.sendResponse("WrongSorterBlockSetup", p);
//				}
//			}
		}
	}

	/**
	 * Checks whether a specific material is a possible center block for a factory
	 * and whether a factory could potentially created from a block with this
	 * material
	 * 
	 * @param m Material to check
	 * @return true if the material could be the one of a possible center block,
	 *         false if not
	 */
	public boolean isPossibleCenterBlock(Material m) {
		return possibleCenterBlocks.contains(m);
	}

	/**
	 * Checks whether the given material is an interaction material and whether a
	 * reaction should be tried to get when one of those blocks is part of a factory
	 * and interacted with
	 * 
	 * @param m Material to check
	 * @return True if the material is a possible interaction material, false if not
	 */
	public boolean isPossibleInteractionBlock(Material m) {
		return possibleInteractionBlocks.contains(m);
	}

	/**
	 * Gets all factories which currently exist. Do not mess with the hashset
	 * returned as it is used in other places
	 * 
	 * @return All existing factory instances
	 */
	public HashSet<Factory> getAllFactories() {
		synchronized (factories) {
			return new HashSet<Factory>(factories);
		}
	}

	/**
	 * Gets a specific factory builder based on it's type
	 * 
	 * @param name Type of builder
	 * @return The builder with the given name or null if no such builder exists
	 */
	public IFactoryBuilder getBuilder(FactoryType type) {
		return builders.get(type);
	}

	/**
	 * @return All builders contained in this manager
	 */
	public HashMap<FactoryType, IFactoryBuilder> getAllBuilders() {
		return builders;
	}

	/**
	 * Gets the recipe with the given identifier, if it exists
	 * 
	 * @param type Identifier of the recipe
	 * @return Recipe with the given identifier or null if either the recipe doesn't
	 *         exist or the given string was null
	 */
	public IRecipe getRecipe(String identifier) {
		if (identifier == null) {
			return null;
		}
		return recipes.get(identifier);
	}

	/**
	 * Registers a recipe and add it to the recipe tracking.
	 * 
	 * @param recipe Recipe to register
	 */
	public void registerRecipe(IRecipe recipe) {
		recipes.put(recipe.getIdentifier(), recipe);
	}

	public void saveFactories() {
		plugin.info("Attempting to save factory data");
		fileManager.save(getAllFactories());
	}

	public void loadFactories() {
		plugin.info("Attempting to load factory data");
		fileManager.load(builders);
	}

}
