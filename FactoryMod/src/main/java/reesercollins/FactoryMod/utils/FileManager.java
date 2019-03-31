package reesercollins.FactoryMod.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.FactoryManager;
import reesercollins.FactoryMod.builders.IFactoryBuilder;
import reesercollins.FactoryMod.builders.ProductionBuilder;
import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.factories.Factory.FactoryType;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.protection.FactoryPermission;
import reesercollins.FactoryMod.protection.ProtectedBlock;
import reesercollins.FactoryMod.recipes.IRecipe;
import reesercollins.FactoryMod.recipes.IRecipe.RecipeType;
import reesercollins.FactoryMod.repair.PercentageHealthRepairManager;

public class FileManager {

	private FMPlugin plugin;
	private FactoryManager manager;
	private File saveFile;
	private File backup;

	private Map<String, String> factoryRenames;

	private static int saveFileVersion = 1;

	public FileManager(FactoryManager manager, Map<String, String> factoryRenames) {
		plugin = FMPlugin.getInstance();
		this.factoryRenames = factoryRenames;
		this.manager = manager;
		saveFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "factoryData.yml");
		backup = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "factoryDataPreviousSave.yml");
	}

	public static ProtectedBlock getBlockProtection(Location location) {
		File worldFile = new File(FMPlugin.getInstance().getDataFolder().getAbsolutePath() + File.separator
				+ "protections" + File.separator + location.getWorld().getName() + ".yml");
		if (!worldFile.exists()) {
			return null;
		}
		try {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(worldFile);
			ConfigurationSection blockSection = config.getConfigurationSection(
					location.getBlock().getX() + "-" + location.getBlock().getY() + "-" + location.getBlock().getZ());
			if (blockSection == null) {
				return null;
			}
			String typeString = blockSection.getString("type");
			if (typeString == null) {
				return null;
			}
			FactoryType type = FactoryType.valueOf(typeString);
			if (type == null) {
				return null;
			}
			ProtectedBlock block = new ProtectedBlock(location.getBlock(), type);
			
			ConfigurationSection permissions = blockSection.getConfigurationSection("permissions");
			for (String key : permissions.getKeys(false)) {
				OfflinePlayer p = FMPlugin.getInstance().getServer().getOfflinePlayer(UUID.fromString(key));
				List<String> perms = permissions.getStringList(key);
				if (perms == null || perms.isEmpty()) {
					continue;
				}
				for (String perm : perms) {
					FactoryPermission fPerm = FactoryPermission.valueOf(perm);
					if (fPerm != null) {
						block.addPermissionToPlayer(p, fPerm);
					}
				}
			}
			return block;
		} catch (Exception e) {
			FMPlugin.getInstance().error("Error when loading block protection " + worldFile.getAbsolutePath(), false);
			e.printStackTrace();
		}
		return null;
	}

	public static void saveBlockProtection(ProtectedBlock block) {
		File worldFile = new File(FMPlugin.getInstance().getDataFolder().getAbsolutePath() + File.separator
				+ "protections" + File.separator + block.getBlock().getWorld().getName() + ".yml");
		if (!worldFile.exists()) {
			worldFile.mkdirs();
			try {
				worldFile.createNewFile();
			} catch (IOException e) {
				FMPlugin.getInstance().error("Unable to create file " + worldFile.getAbsolutePath(), false);
				e.printStackTrace();
				return;
			}
		}
		try {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(worldFile);
			ConfigurationSection blockSection = config.createSection(
					block.getBlock().getX() + "-" + block.getBlock().getY() + "-" + block.getBlock().getZ());
			blockSection.set("type", block.getFactoryType());
			ConfigurationSection permissionsSection = blockSection.createSection("permissions");
			for (Entry<OfflinePlayer, List<FactoryPermission>> entry : block.getPermissions().entrySet()) {
				List<String> permNames = new ArrayList<String>();
				for (FactoryPermission perm : entry.getValue()) {
					permNames.add(perm.toString());
				}
				permissionsSection.set(entry.getKey().getUniqueId().toString(), permNames);
			}
		} catch (Exception e) {
			FMPlugin.getInstance().error("Error when saving block protection " + worldFile.getAbsolutePath(), false);
			e.printStackTrace();
		}
	}

	public void save(Collection<Factory> factories) {
		if (saveFile.exists()) {
			// old save file exists, so it is our new backup now
			if (backup.exists()) {
				backup.delete();
			}
			saveFile.renameTo(backup);
		}
		try {
			saveFile.createNewFile();
			YamlConfiguration config = YamlConfiguration.loadConfiguration(saveFile);
			config.set("version", saveFileVersion);
			for (Factory f : factories) {
				String current = serializeLocation(f.getMultiBlockStructure().getCenter());
				config.set(current + ".name", f.getType());
				ConfigurationSection blockSection = config.getConfigurationSection(current).createSection("blocks");
				configureLocation(blockSection, f.getMultiBlockStructure().getAllBlocks());
				if (f instanceof ProductionFactory) {
					ProductionFactory pf = (ProductionFactory) f;
					config.set(current + ".type", "production");
					config.set(current + ".health",
							((PercentageHealthRepairManager) pf.getRepairManager()).getRawHealth());
					config.set(current + ".breakTime",
							((PercentageHealthRepairManager) pf.getRepairManager()).getBreakTime());
					config.set(current + ".runtime", pf.getRunningTime());
					config.set(current + ".selectedRecipe", pf.getCurrentRecipe().getType());
					config.set(current + ".autoSelect", pf.isAutoSelect());
					List<String> recipeList = new LinkedList<String>();
					for (IRecipe rec : pf.getRecipes()) {
						recipeList.add(rec.getIdentifier());
					}
					config.set(current + ".recipes", recipeList);
					if (pf.getActivator() == null) {
						config.set(current + ".activator", "null");
					} else {
						config.set(current + ".activator", pf.getActivator().toString());
					}
					for (IRecipe i : ((ProductionFactory) f).getRecipes()) {
						config.set(current + ".runcounts." + i.getType(), pf.getRunCount(i));
						config.set(current + ".recipeLevels." + i.getType(), pf.getRecipeLevel(i));
					}
				} // else if (f instanceof Pipe) {
//					Pipe p = (Pipe) f;
//					config.set(current + ".type", "PIPE");
//					config.set(current + ".runtime", p.getRunTime());
//					List<String> mats = new LinkedList<String>();
//					List<Material> materials = p.getAllowedMaterials();
//					if (materials != null) {
//						for (Material m : materials) {
//							mats.add(m.toString());
//						}
//					}
//					config.set(current + ".materials", mats);
//				} else if (f instanceof Sorter) {
//					Sorter s = (Sorter) f;
//					config.set(current + ".runtime", s.getRunTime());
//					config.set(current + ".type", "SORTER");
//					for (BlockFace face : MultiBlockStructure.allBlockSides) {
//						config.set(current + ".faces." + face.toString(),
//								s.getItemsForSide(face).getItemStackRepresentation().toArray());
//					}
//				}
			}
			config.save(saveFile);
			plugin.info("Successfully saved factory data");
		} catch (Exception e) {
			// In case anything goes wrong while saving we always keep the
			// latest valid backup
			plugin.error("Fatal error while trying to save factory data", true);
			e.printStackTrace();
			saveFile.delete();
		}
	}

	private void configureLocation(ConfigurationSection config, List<Location> locations) {
		int count = 0;
		for (Location loc : locations) {
			String identifier = "a" + count++ + serializeLocation(loc);
			config.set(identifier + ".world", loc.getWorld().getName());
			config.set(identifier + ".x", loc.getBlockX());
			config.set(identifier + ".y", loc.getBlockY());
			config.set(identifier + ".z", loc.getBlockZ());
		}
	}

	private String serializeLocation(Location loc) {
		return loc.getWorld().getName() + "#" + loc.getBlockX() + "#" + loc.getBlockY() + "#" + loc.getBlockZ();
	}

	public void load(Map<FactoryType, IFactoryBuilder> builders) {
		if (saveFile.exists()) {
			loadFromFile(saveFile, builders);
		} else {
			plugin.warning("No default save file found");
			if (backup.exists()) {
				plugin.info("Backup file found, loading backup");
				loadFromFile(backup, builders);
			} else {
				plugin.warning(
						"No backup save file found. If you are not starting this plugin for the first time you should be VERY worried now");
			}
		}
	}

	private void loadFromFile(File f, Map<FactoryType, IFactoryBuilder> builders) {
		int counter = 0;
		YamlConfiguration config = YamlConfiguration.loadConfiguration(saveFile);
		int loadedVersion = config.getInt("version", 1);
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				continue;
			}
			String typeString = current.getString("type");
			FactoryType type = FactoryType.valueOf(typeString);
			String name = current.getString("name");
			int runtime = current.getInt("runtime");
			List<Location> blocks = new LinkedList<Location>();
			Set<String> blockKeys = current.getConfigurationSection("blocks").getKeys(false);
			Collections.sort(new LinkedList<String>(blockKeys));
			for (String blockKey : blockKeys) {
				ConfigurationSection currSec = current.getConfigurationSection("blocks")
						.getConfigurationSection(blockKey);
				String worldName = currSec.getString("world");
				int x = currSec.getInt("x");
				int y = currSec.getInt("y");
				int z = currSec.getInt("z");
				World w = Bukkit.getWorld(worldName);
				blocks.add(new Location(w, x, y, z));
			}
			switch (type) {
			case PRODUCTION:
				if (loadedVersion == 1) {
					// need to sort the locations properly, because they werent previously
					List<Location> sortedList = new LinkedList<Location>();
					int totalX = 0;
					int totalY = 0;
					int totalZ = 0;
					for (Location loc : blocks) {
						totalX += loc.getBlockX();
						totalY += loc.getBlockY();
						totalZ += loc.getBlockZ();
					}
					Location center = new Location(blocks.get(0).getWorld(), totalX / 3, totalY / 3, totalZ / 3);
					if (!blocks.contains(center)) {
						plugin.warning("Failed to convert location for factory at " + blocks.get(0).toString()
								+ "; calculated center: " + center.toString());
					} else {
						blocks.remove(center);
						sortedList.add(center);
						// we cant guarantee that this will work, it might very well fail for partially
						// broken factories, but it's the best thing I got
						if (blocks.get(0).getBlock().getType() == Material.CHEST) {
							sortedList.add(blocks.get(1));
							sortedList.add(blocks.get(0));
						} else {
							sortedList.add(blocks.get(0));
							sortedList.add(blocks.get(1));
						}
						blocks = sortedList;
					}

				}
				ProductionBuilder builder = (ProductionBuilder) builders.get(name);
				if (builder == null) {
					String replaceName = factoryRenames.get(name);
					if (replaceName != null) {
						builder = (ProductionBuilder) builders.get(replaceName);
					}
					if (builder == null) {
						plugin.warning("Save file contained factory named " + name
								+ " , but no factory with this name was found in the config");
						continue;
					} else {
						name = replaceName;
					}
				}
				int health = current.getInt("health");
				long breakTime = current.getLong("breakTime", 0);
				String selectedRecipeStr = current.getString("selectedRecipe");
				RecipeType selectedRecipe = RecipeType.valueOf(selectedRecipeStr);
				List<String> recipes = current.getStringList("recipes");
				boolean autoSelect = current.getBoolean("autoSelect", false);
				if (recipes == null) {
					recipes = new LinkedList<String>();
				}
				ProductionFactory fac = (ProductionFactory) builder.revive(blocks, health, selectedRecipe, runtime,
						breakTime, recipes);
				String activator = current.getString("activator", "null");
				UUID acti;
				if (activator.equals("null")) {
					acti = null;
				} else {
					acti = UUID.fromString(activator);
				}
				fac.setActivator(acti);
				ConfigurationSection runCounts = current.getConfigurationSection("runcounts");
				if (runCounts != null) {
					for (String countKey : runCounts.getKeys(false)) {
						int runs = runCounts.getInt(countKey);
						for (IRecipe r : fac.getRecipes()) {
							if (r.getType().equals(countKey)) {
								fac.setRunCount(r, runs);
								break;
							}
						}
					}
				}
				ConfigurationSection recipeLevels = current.getConfigurationSection("recipeLevels");
				if (recipeLevels != null) {
					for (String countKey : recipeLevels.getKeys(false)) {
						int runs = recipeLevels.getInt(countKey);
						for (IRecipe r : fac.getRecipes()) {
							if (r.getType().equals(countKey)) {
								fac.setRecipeLevel(r, runs);
								break;
							}
						}
					}
				}
				fac.setAutoSelect(autoSelect);
				manager.addFactory(fac);
				counter++;
				break;
//			case "PIPE":
//				PipeEgg pipeEgg = (PipeEgg) builders.get(name);
//				if (pipeEgg == null) {
//					String replaceName = factoryRenames.get(name);
//					if (replaceName != null) {
//						pipeEgg = (PipeEgg) builders.get(replaceName);
//					}
//					if (pipeEgg == null) {
//						plugin.warning("Save file contained factory named " + name
//								+ " , but no factory with this name was found in the config");
//						continue;
//					} else {
//						name = replaceName;
//					}
//				}
//				List<Material> mats = new LinkedList<Material>();
//				if (current.isSet("materials")) {
//					for (String mat : current.getStringList("materials")) {
//						mats.add(Material.valueOf(mat));
//					}
//				} else {
//					mats = null;
//				}
//				if (mats.size() == 0) {
//					mats = null;
//				}
//				Factory p = pipeEgg.revive(blocks, mats, runtime);
//				manager.addFactory(p);
//				counter++;
//				break;
//			case "SORTER":
//				Map<BlockFace, ItemMap> assignments = new HashMap<BlockFace, ItemMap>();
//				SorterEgg sorterEgg = (SorterEgg) builders.get(name);
//				if (sorterEgg == null) {
//					String replaceName = factoryRenames.get(name);
//					if (replaceName != null) {
//						sorterEgg = (SorterEgg) builders.get(replaceName);
//					}
//					if (sorterEgg == null) {
//						plugin.warning("Save file contained factory named " + name
//								+ " , but no factory with this name was found in the config");
//						continue;
//					} else {
//						name = replaceName;
//					}
//				}
//				for (String face : current.getConfigurationSection("faces").getKeys(false)) {
//					List<ItemStack> stacks = (List<ItemStack>) current.getConfigurationSection("faces").get(face);
//					// it works, okay?
//					ItemMap map = new ItemMap(stacks);
//					assignments.put(BlockFace.valueOf(face), map);
//				}
//				Factory s = sorterEgg.revive(blocks, assignments, runtime);
//				manager.addFactory(s);
//				counter++;
//				break;
			}
		}
		plugin.info("Loaded " + counter + " factory from save file");
	}

}
