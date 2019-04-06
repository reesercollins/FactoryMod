package reesercollins.FactoryMod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.google.common.collect.Lists;

import reesercollins.FactoryMod.builders.IFactoryBuilder;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.recipes.IRecipe;
import reesercollins.FactoryMod.recipes.IRecipe.RecipeType;
import reesercollins.FactoryMod.recipes.InputRecipe;
import reesercollins.FactoryMod.recipes.ProductionRecipe;
import reesercollins.FactoryMod.recipes.PylonRecipe;
import reesercollins.FactoryMod.recipes.RecipeScalingUpgradeRecipe;
import reesercollins.FactoryMod.recipes.RepairRecipe;
import reesercollins.FactoryMod.recipes.UpgradeRecipe;
import reesercollins.FactoryMod.recipes.scaling.ProductionRecipeModifier;

public class ConfigParser {

	public static List<Material> leatherArmor = new LinkedList<Material>(Arrays.asList(Material.LEATHER_BOOTS,
			Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET, Material.LEATHER_LEGGINGS));

	public static List<Material> potions = new LinkedList<Material>(
			Arrays.asList(Material.POTION, Material.LINGERING_POTION, Material.SPLASH_POTION, Material.TIPPED_ARROW));

	// TODO Figure out what the fuck those egg things are.
	private static FMPlugin plugin;
	private HashMap<String, IRecipe> recipes;
	private FactoryManager manager;
	private int defaultUpdateTime;
	private ItemStack defaultFuel;
	private int defaultFuelConsumptionTime;
	private double defaultReturnRate;
	private long defaultBreakGracePeriod;
	private int defaultDamagePerBreakPeriod;
	private int defaultHealth;
	private long savingInterval;
	private boolean useYamlIdentifiers;
	private HashMap<String, IFactoryBuilder> upgradeBuilders;
	private HashMap<IFactoryBuilder, List<String>> recipeLists;
	private HashMap<RecipeScalingUpgradeRecipe, String[]> recipeScalingUpgradeMapping;

	public ConfigParser(FMPlugin plugin) {
		ConfigParser.plugin = plugin;
	}

	public FactoryManager parse() {
		// Get current copy of configuration file
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();

		boolean logInventories = config.getBoolean("log_inventories", true);
		Material factoryInteractionMaterial = Material.STICK;
		try {
			factoryInteractionMaterial = Material
					.getMaterial(config.getString("factory_interaction_material", "STICK"));
		} catch (IllegalArgumentException e) {
			plugin.warning(config.getString("factory_interaction_material") + " is not a valid material");
		}

		defaultUpdateTime = (int) parseTime(config.getString("default_update_time", "5"));
		defaultHealth = config.getInt("default_health", 10000);
		ItemMap dfuel = parseItemMap(config.getConfigurationSection("default_fuel"));
		if (dfuel.getTotalUniqueItemAmount() > 0) {
			defaultFuel = dfuel.getItemStackRepresentation().get(0);
		} else {
			plugin.warning("No default_fuel specified. Should be ItemMap");
		}
		defaultFuelConsumptionTime = (int) parseTime(config.getString("default_fuel_consumtion_interval", "20"));
		defaultReturnRate = config.getDouble("default_return_rate");
		defaultBreakGracePeriod = 50 * parseTime(config.getString("default_break_grace_period"));
		defaultDamagePerBreakPeriod = config.getInt("default_decay_amount", 21);
		savingInterval = parseTime(config.getString("saving_interval", "15m"));

		manager = new FactoryManager(plugin, factoryInteractionMaterial, logInventories, null);

		return null;
	}

	/**
	 * Parses time. This allows to specify human readable time values easily,
	 * instead of having to specify every amount in ticks or seconds. The unit of a
	 * number specified by the letter added after it, for example 5h means 5 hours
	 * or 34s means 34 seconds. Possible modifiers are: t (ticks), s (seconds), m
	 * (minutes), h (hours) and d (days). If no letter is added the value will be
	 * parsed as ticks.
	 * 
	 * @param arg String to be parsed.
	 * @return Time value in ticks
	 */
	public static long parseTime(String arg) {
		long result = 0;
		try {
			result += Long.parseLong(arg);
			return result;
		} catch (NumberFormatException e) {
		}

		arg.replaceAll(" ", "");

		while (!arg.equals("")) {
			int length = 0;
			switch (arg.charAt(arg.length() - 1)) {
			case 't':
				long ticks = getLastNumber(arg);
				result += ticks;
				length = String.valueOf(ticks).length() + 1;
				break;
			case 's':
				long seconds = getLastNumber(arg);
				result += seconds * 20;
				length = String.valueOf(seconds).length() + 1;
				break;
			case 'm':
				long minutes = getLastNumber(arg);
				result += minutes * 20 * 60;
				length = String.valueOf(minutes).length() + 1;
				break;
			case 'h':
				long hours = getLastNumber(arg);
				result += hours * 20 * 3600;
				length = String.valueOf(hours).length() + 1;
				break;
			case 'd':
				long days = getLastNumber(arg);
				result += 20 * 3600 * 24 * days;
				length = String.valueOf(days).length() + 1;
				break;
			default:
				Bukkit.getLogger().severe("Invalid time value in config:" + arg);
			}
			arg = arg.substring(0, arg.length() - length);
		}
		return result;
	}

	/**
	 * Parses milliseconds into a human-readable format.
	 * 
	 * @param arg The value to be parsed
	 * @return Formated time.
	 */
	public static String parseTime(long arg) {
		long months = arg / (1000L * 60L * 60L * 24L * 30L);
		long days = (arg / (1000L * 60L * 60L * 24L)) % 30L;
		long hours = (arg / (1000L * 60L * 60L)) % 24L;
		String result = (months != 0 ? months + " months" : "") + (days != 0 ? days + " days" : "")
				+ (hours != 0 ? hours + " hours" : "");
		if (result == "") {
			return "less than an hour";
		}
		return result;
	}

	private static long getLastNumber(String arg) {
		StringBuilder number = new StringBuilder();
		for (int i = arg.length() - 2; i >= 0; i--) {
			if (Character.isDigit(arg.charAt(i))) {
				number.insert(0, arg.substring(i, i + 1));
			} else {
				break;
			}
		}
		long result = Long.parseLong(number.toString());
		return result;
	}

	/**
	 * Creates an ItemMap containing all the items listed in the given configuration
	 * section
	 *
	 * @param config ConfigurationSection to parse the items from
	 * @return The item map created
	 */
	public static ItemMap parseItemMap(ConfigurationSection config) {
		ItemMap result = new ItemMap();
		if (config == null) {
			return result;
		}
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			ItemMap partMap = parseItemMapDirectly(current);
			result.merge(partMap);
		}
		return result;
	}

	public static ItemMap parseItemMapDirectly(ConfigurationSection current) {
		ItemMap im = new ItemMap();
		if (current == null) {
			return im;
		}
		Material m = null;
		try {
			m = Material.valueOf(current.getString("material"));
		} catch (IllegalArgumentException iae) {
			m = null;
		} finally {
			if (m == null) {
				plugin.error("Failed to find material of section " + current.getCurrentPath(), false);
				return im;
			}
		}
		ItemStack toAdd = new ItemStack(m);
		ItemMeta meta = toAdd.getItemMeta();
		if (meta == null) {
			plugin.error("No item meta found for" + current.getCurrentPath(), false);
		} else {
			String name = current.getString("name");
			if (name != null) {
				meta.setDisplayName(name);
			}
			List<String> lore = current.getStringList("lore");
			if (lore != null) {
				meta.setLore(lore);
			}
			if (current.isBoolean("unbreakable")) {
				meta.setUnbreakable(current.getBoolean("unbreakable"));
			}
			if (current.isBoolean("hideFlags") && current.getBoolean("hideFlags")) {
				for (ItemFlag flag : ItemFlag.values()) {
					meta.addItemFlags(flag);
				}
			}
			if (current.contains("enchants")) {
				for (String enchantKey : current.getConfigurationSection("enchants").getKeys(false)) {
					ConfigurationSection enchantConfig = current.getConfigurationSection("enchants")
							.getConfigurationSection(enchantKey);
					NamespacedKey enchantmentKey = NamespacedKey.minecraft(enchantConfig.getString("enchant"));
					if (enchantmentKey == null) {
						plugin.error("Failed to find enchantment with key " + enchantConfig.getString("enchant"),
								false);
					} else {
						Enchantment enchant = Enchantment.getByKey(enchantmentKey);
						int level = enchantConfig.getInt("level", 1);
						meta.addEnchant(enchant, level, true);
					}
				}
			}
			if (leatherArmor.contains(m)) {
				ConfigurationSection color = current.getConfigurationSection("color");
				Color leatherColor = null;
				if (color != null) {
					int red = color.getInt("red");
					int blue = color.getInt("blue");
					int green = color.getInt("green");
					leatherColor = Color.fromRGB(red, green, blue);
				} else {
					String hexColorCode = current.getString("color");
					if (hexColorCode != null) {
						Integer hexColor = Integer.parseInt(hexColorCode, 16);
						if (hexColor != null) {
							leatherColor = Color.fromRGB(hexColor);
						}
					}
				}
				if (leatherColor != null) {
					((LeatherArmorMeta) meta).setColor(leatherColor);
				}
			}
			if (m == Material.ENCHANTED_BOOK) {
				ConfigurationSection storedEnchantSection = current.getConfigurationSection("stored_enchants");
				if (storedEnchantSection != null) {
					EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) meta;
					for (String sEKey : storedEnchantSection.getKeys(false)) {
						ConfigurationSection currentStoredEnchantSection = storedEnchantSection
								.getConfigurationSection(sEKey);
						if (currentStoredEnchantSection != null) {
							NamespacedKey enchantmentKey = NamespacedKey
									.minecraft(currentStoredEnchantSection.getString("enchant"));
							if (enchantmentKey == null) {
								plugin.error("Failed to find enchantment with key "
										+ currentStoredEnchantSection.getString("enchant"), false);
							} else {
								Enchantment enchant = Enchantment.getByKey(enchantmentKey);
								int level = currentStoredEnchantSection.getInt("level", 1);
								enchantMeta.addStoredEnchant(enchant, level, true);
							}
						}
					}
				}
			}
			if (potions.contains(m)) {
				ConfigurationSection potion = current.getConfigurationSection("potion_effects");
				if (potion != null) {
					PotionType potType;
					try {
						potType = PotionType.valueOf(potion.getString("type", "AWKWARD"));
					} catch (IllegalArgumentException e) {
						plugin.warning("Expected potion type at " + potion.getCurrentPath() + ", but "
								+ potion.getString("type") + " is not a valid potion type");
						potType = PotionType.AWKWARD;
					}
					boolean upgraded = potion.getBoolean("upgraded", false);
					boolean extended = potion.getBoolean("extended", false);
					PotionMeta potMeta = (PotionMeta) meta;
					potMeta.setBasePotionData(new PotionData(potType, extended, upgraded));
					ConfigurationSection customEffects = potion.getConfigurationSection("custom_effects");
					if (customEffects != null) {
						List<PotionEffect> pots = parsePotionEffects(potion);
						for (PotionEffect pe : pots) {
							potMeta.addCustomEffect(pe, true);
						}
					}
				}
			}
		}
		int amount = current.getInt("amount", 1);
		toAdd.setAmount(amount);
		im.addItemStack(toAdd);
		return im;
	}

	/**
	 * Parses a potion effect
	 *
	 * @param configurationSection ConfigurationSection to parse the effect from
	 * @return The potion effect parsed
	 */
	public static List<PotionEffect> parsePotionEffects(ConfigurationSection configurationSection) {
		List<PotionEffect> potionEffects = Lists.newArrayList();
		if (configurationSection != null) {
			for (String name : configurationSection.getKeys(false)) {
				ConfigurationSection configEffect = configurationSection.getConfigurationSection(name);
				String type = configEffect.getString("type");
				if (type == null) {
					plugin.error("Expected potion type to be specified, but found no \"type\" option at "
							+ configEffect.getCurrentPath(), false);
					continue;
				}
				PotionEffectType effect = PotionEffectType.getByName(type);
				if (effect == null) {
					plugin.error("Expected potion type to be specified at " + configEffect.getCurrentPath()
							+ " but found " + type + " which is no valid type", false);
				}
				int duration = configEffect.getInt("duration", 200);
				int amplifier = configEffect.getInt("amplifier", 0);
				potionEffects.add(new PotionEffect(effect, duration, amplifier));
			}
		}
		return potionEffects;
	}

	/**
	 * Parses all recipes and sorts them into a hashmap by their name so they are
	 * ready to assign them to factories
	 * 
	 * @param config ConfigurationSection containing the recipe configurations
	 */
	private void parseRecipes(ConfigurationSection config) {
		recipes = new HashMap<String, IRecipe>();
		List<String> recipeKeys = new LinkedList<String>();
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				plugin.warning("Found invalid section that should not exist at " + config.getCurrentPath() + key);
				continue;
			}
			recipeKeys.add(key);
		}
		while (!recipeKeys.isEmpty()) {
			String currentIdent = recipeKeys.get(0);
			ConfigurationSection current = config.getConfigurationSection(currentIdent);
			if (useYamlIdentifiers) {
				// no support for inheritation when not using yaml identifiers
				boolean foundParent = false;
				while (!foundParent) {
					// keep track of already parsed sections, so we dont get stuck forever in cyclic
					// dependencies
					List<String> children = new LinkedList<String>();
					children.add(currentIdent);
					if (current.isString("inherit")) {
						// parent is defined for this recipe
						String parent = current.getString("inherit");
						if (recipes.containsKey(parent)) {
							// we already parsed the parent, so parsing this recipe is fine
							foundParent = true;
						} else {
							if (!recipeKeys.contains(parent)) {
								// specified parent doesnt exist
								plugin.warning("The recipe " + currentIdent + " specified " + parent
										+ " as parent, but this recipe could not be found");
								current = null;
								foundParent = true;
							} else {

								// specified parent exists, but wasnt parsed yet, so we do it first
								if (children.contains(parent)) {
									// cyclic dependency
									plugin.warning(
											"The recipe " + currentIdent + " specified a cyclic dependency with parent "
													+ parent + " it was skipped");
									current = null;
									foundParent = true;
									break;
								}
								currentIdent = parent;
								current = config.getConfigurationSection(parent);
							}
						}
					} else {
						// no parent is a parent as well
						foundParent = true;
					}
				}
			}
			recipeKeys.remove(currentIdent);
			if (current == null) {
				plugin.warning(String.format("Recipe %s unable to be added.", currentIdent));
				continue;
			}
			IRecipe recipe = parseRecipe(current);
			if (recipe == null) {
				plugin.warning(String.format("Recipe %s unable to be added.", currentIdent));
			} else {
				if (recipes.containsKey(recipe.getIdentifier())) {
					plugin.warning("Recipe identifier " + recipe.getIdentifier()
							+ " was found twice in the config. One instance was skipped");
				} else {
					recipes.put(recipe.getIdentifier(), recipe);
					manager.registerRecipe(recipe);
				}
			}
		}
	}

	/**
	 * Parses a single recipe
	 * 
	 * @param config ConfigurationSection to parse the recipe from
	 * @return The recipe created based on the data parse
	 */
	private IRecipe parseRecipe(ConfigurationSection config) {
		IRecipe result;
		IRecipe parentRecipe = null;
		if (config.isString("inherit") && useYamlIdentifiers) {
			parentRecipe = recipes.get(config.get("inherit"));
		}
		String name = config.getString("name", (parentRecipe != null) ? parentRecipe.getName() : null);
		if (name == null) {
			plugin.warning("No name specified for recipe at " + config.getCurrentPath() + ". Skipping the recipe.");
			return null;
		}
		// we dont inherit identifier, because that would make no sense
		String identifier = config.getString("identifier");
		if (identifier == null) {
			if (useYamlIdentifiers) {
				identifier = config.getName();
			} else {
				identifier = name;
			}
		}
		String prodTime = config.getString("production_time");
		if (prodTime == null && parentRecipe == null) {
			plugin.warning("No production time specied for recipe " + name + ". Skipping it");
			return null;
		}
		int productionTime;
		if (parentRecipe == null) {
			productionTime = (int) parseTime(prodTime);
		} else {
			productionTime = parentRecipe.getProductionTime();
		}
		String typeString = config.getString("type", (parentRecipe != null) ? parentRecipe.getName() : null);
		if (typeString == null) {
			plugin.warning("No type specified for recipe at " + config.getCurrentPath() + ". Skipping the recipe.");
			return null;
		}
		RecipeType type = RecipeType.valueOf(typeString);
		if (type == null) {
			plugin.warning("Invalid type specified for recipe at " + config.getCurrentPath() + ". Skipping the recipe.");
			return null;
		}
		ConfigurationSection inputSection = config.getConfigurationSection("input");
		ItemMap input;
		if (inputSection == null) {
			// no input specified, check parent
			if (!(parentRecipe instanceof InputRecipe)) {
				// default to empty input
				input = new ItemMap();
			} else {
				input = ((InputRecipe) parentRecipe).getInput();
			}
		} else {
			input = parseItemMap(inputSection);
		}
		switch (type) {
		case PRODUCTION:
			ConfigurationSection outputSection = config.getConfigurationSection("output");
			ItemMap output;
			if (outputSection == null) {
				if (!(parentRecipe instanceof ProductionRecipe)) {
					output = new ItemMap();
				} else {
					output = ((ProductionRecipe) parentRecipe).getOutput();
				}
			} else {
				output = parseItemMap(outputSection);
			}
			ProductionRecipeModifier modi = parseProductionRecipeModifier(config.getConfigurationSection("modi"));
			if (modi == null && parentRecipe instanceof ProductionRecipe) {
				modi = ((ProductionRecipe) parentRecipe).getModifier().clone();
			}
			result = new ProductionRecipe(identifier, name, productionTime, input, output, modi);
			break;
//		case COMPACT:
//			String compactedLore = config.getString("compact_lore",
//					(parentRecipe instanceof CompactingRecipe) ? ((CompactingRecipe) parentRecipe).getCompactedLore()
//							: null);
//			if (compactedLore == null) {
//				plugin.warning("No special lore specified for compaction recipe " + name + " it was skipped");
//				result = null;
//				break;
//			}
//			manager.addCompactLore(compactedLore);
//			List<Material> excluded = new LinkedList<Material>();
//			if (config.isList("excluded_materials")) {
//				for (String mat : config.getStringList("excluded_materials")) {
//					try {
//						excluded.add(Material.valueOf(mat));
//					} catch (IllegalArgumentException iae) {
//						plugin.warning(mat + " is not a valid material to exclude: " + config.getCurrentPath());
//					}
//				}
//			} else {
//				if (parentRecipe instanceof CompactingRecipe) {
//					// copy so they are not using same instance
//					for (Material m : ((CompactingRecipe) parentRecipe).getExcludedMaterials()) {
//						excluded.add(m);
//					}
//				}
//				// otherwise just leave list empty, as nothing is specified, which is fine
//			}
//			result = new CompactingRecipe(identifier, input, excluded, name, productionTime, compactedLore);
//			break;
//		case DECOMPACT:
//			String decompactedLore = config.getString("compact_lore",
//					(parentRecipe instanceof DecompactingRecipe)
//							? ((DecompactingRecipe) parentRecipe).getCompactedLore()
//							: null);
//			if (decompactedLore == null) {
//				plugin.warning("No special lore specified for decompaction recipe " + name + " it was skipped");
//				result = null;
//				break;
//			}
//			manager.addCompactLore(decompactedLore);
//			result = new DecompactingRecipe(identifier, input, name, productionTime, decompactedLore);
//			break;
		case REPAIR:
			int health = config.getInt("health_gained",
					(parentRecipe instanceof RepairRecipe) ? ((RepairRecipe) parentRecipe).getHealth() : 0);
			if (health == 0) {
				plugin.warning("Health gained from repair recipe " + name
						+ " is set to or was defaulted to 0, this might not be what was intended");
			}
			result = new RepairRecipe(identifier, name, productionTime, input, health);
			break;
		case UPGRADE:
			String upgradeName = config.getString("factory");
			IFactoryBuilder builder;
			if (upgradeName == null) {
				if (parentRecipe instanceof UpgradeRecipe) {
					builder = ((UpgradeRecipe) parentRecipe).getBuilder();
				} else {
					builder = null;
				}
			} else {
				builder = upgradeBuilders.get(upgradeName);
			}
			if (builder == null) {
				plugin.warning("Could not find factory " + upgradeName + " for upgrade recipe " + name);
				result = null;
			} else {
				result = new UpgradeRecipe(identifier, name, productionTime, input, builder);
			}
			break;
//		case "AOEREPAIR":
//			// This is untested and should not be used for now
//			plugin.warning(
//					"This recipe is not tested or even completly developed, use it with great care and don't expect it to work");
//			ItemMap tessence = parseItemMap(config.getConfigurationSection("essence"));
//			if (tessence.getTotalUniqueItemAmount() > 0) {
//				ItemStack essence = tessence.getItemStackRepresentation().get(0);
//				int repPerEssence = config.getInt("repair_per_essence");
//				int range = config.getInt("range");
//				result = new AOERepairRecipe(identifier, name, productionTime, essence, range, repPerEssence);
//			} else {
//				plugin.severe("No essence specified for AOEREPAIR " + config.getCurrentPath());
//				result = null;
//			}
//			break;
		case PYLON:
			ConfigurationSection outputSec = config.getConfigurationSection("output");
			ItemMap outputMap;
			if (outputSec == null) {
				if (!(parentRecipe instanceof PylonRecipe)) {
					outputMap = new ItemMap();
				} else {
					outputMap = ((PylonRecipe) parentRecipe).getOutput().clone();
				}
			} else {
				outputMap = parseItemMap(outputSec);
			}
			if (outputMap.getTotalItemAmount() == 0) {
				plugin.warning("Pylon recipe " + name + " has an empty output specified");
			}
			int weight = config.getInt("weight",
					(parentRecipe instanceof PylonRecipe) ? ((PylonRecipe) parentRecipe).getWeight() : 20);
			result = new PylonRecipe(identifier, name, productionTime, input, outputMap, weight);
			break;
//		case "ENCHANT":
//			Enchantment enchant = Enchantment.getByName(config.getString("enchant",
//					(parentRecipe instanceof DeterministicEnchantingRecipe)
//							? ((DeterministicEnchantingRecipe) parentRecipe).getEnchant().getName()
//							: null));
//			if (enchant == null) {
//				plugin.warning(
//						"No enchant specified for deterministic enchanting recipe " + name + ". It was skipped.");
//				result = null;
//				break;
//			}
//			int level = config.getInt("level",
//					(parentRecipe instanceof DeterministicEnchantingRecipe)
//							? ((DeterministicEnchantingRecipe) parentRecipe).getLevel()
//							: 1);
//			ConfigurationSection toolSection = config.getConfigurationSection("enchant_item");
//			ItemMap tool;
//			if (toolSection == null) {
//				if (!(parentRecipe instanceof DeterministicEnchantingRecipe)) {
//					tool = new ItemMap();
//				} else {
//					tool = ((DeterministicEnchantingRecipe) parentRecipe).getTool().clone();
//				}
//			} else {
//				tool = parseItemMap(toolSection);
//			}
//			if (tool.getTotalItemAmount() == 0) {
//				plugin.warning("Deterministic enchanting recipe " + name
//						+ " had no tool to enchant specified, it was skipped");
//				result = null;
//				break;
//			}
//			result = new DeterministicEnchantingRecipe(identifier, name, productionTime, input, tool, enchant, level);
//			break;
//		case "RANDOM":
//			ConfigurationSection outputSect = config.getConfigurationSection("outputs");
//			Map<ItemMap, Double> outputs = new HashMap<ItemMap, Double>();
//			ItemMap displayThis = null;
//			if (outputSect == null) {
//				if (parentRecipe instanceof RandomOutputRecipe) {
//					// clone it
//					for (Entry<ItemMap, Double> entry : ((RandomOutputRecipe) parentRecipe).getOutputs().entrySet()) {
//						outputs.put(entry.getKey().clone(), entry.getValue());
//					}
//					displayThis = ((RandomOutputRecipe) parentRecipe).getDisplayMap();
//				} else {
//					plugin.severe("No outputs specified for random recipe " + name + " it was skipped");
//					result = null;
//					break;
//				}
//			} else {
//				double totalChance = 0.0;
//				String displayMap = outputSect.getString("display");
//				for (String key : outputSect.getKeys(false)) {
//					ConfigurationSection keySec = outputSect.getConfigurationSection(key);
//					if (keySec != null) {
//						double chance = keySec.getDouble("chance");
//						totalChance += chance;
//						ItemMap im = parseItemMap(keySec);
//						outputs.put(im, chance);
//						if (key.equals(displayMap)) {
//							displayThis = im;
//							plugin.debug("Displaying " + displayMap + " as recipe label");
//						}
//					}
//				}
//				if (Math.abs(totalChance - 1.0) > 0.0001) {
//					plugin.warning(
//							"Sum of output chances for recipe " + name + " is not 1.0. Total sum is: " + totalChance);
//				}
//			}
//			result = new RandomOutputRecipe(identifier, name, productionTime, input, outputs, displayThis);
//			break;
//		case "COSTRETURN":
//			double factor = config.getDouble("factor",
//					(parentRecipe instanceof FactoryMaterialReturnRecipe)
//							? ((FactoryMaterialReturnRecipe) parentRecipe).getFactor()
//							: 1.0);
//			result = new FactoryMaterialReturnRecipe(identifier, name, productionTime, input, factor);
//			break;
//		case "LOREENCHANT":
//			ConfigurationSection toolSec = config.getConfigurationSection("loredItem");
//			ItemMap toolMap;
//			if (toolSec == null) {
//				if (!(parentRecipe instanceof LoreEnchantRecipe)) {
//					toolMap = new ItemMap();
//				} else {
//					toolMap = ((LoreEnchantRecipe) parentRecipe).getTool().clone();
//				}
//			} else {
//				toolMap = parseItemMap(toolSec);
//			}
//			if (toolMap.getTotalItemAmount() == 0) {
//				plugin.warning("Lore enchanting recipe " + name + " had no tool to enchant specified, it was skipped");
//				result = null;
//				break;
//			}
//			List<String> appliedLore = config.getStringList("appliedLore");
//			if (appliedLore == null || appliedLore.size() == 0) {
//				if (parentRecipe instanceof LoreEnchantRecipe) {
//					appliedLore = ((LoreEnchantRecipe) parentRecipe).getAppliedLore();
//				} else {
//					plugin.warning("No lore to apply found for lore enchanting recipe " + name + ". It was skipped");
//					result = null;
//					break;
//				}
//			}
//			List<String> overwrittenLore = config.getStringList("overwrittenLore");
//			if (overwrittenLore == null || overwrittenLore.size() == 0) {
//				if (parentRecipe instanceof LoreEnchantRecipe) {
//					overwrittenLore = ((LoreEnchantRecipe) parentRecipe).getOverwrittenLore();
//				} else {
//					// having no lore to be overwritten is completly fine
//					overwrittenLore = new LinkedList<String>();
//				}
//			}
//			result = new LoreEnchantRecipe(identifier, name, productionTime, input, toolMap, appliedLore,
//					overwrittenLore);
//			break;
		case RECIPEMODIFIERUPGRADE:
			int rank = config.getInt("rank");
			String toUpgrade = config.getString("recipeUpgraded");
			if (toUpgrade == null) {
				plugin.warning("No recipe to upgrade specified at " + config.getCurrentPath());
				return null;
			}
			String followUpRecipe = config.getString("followUpRecipe");
			result = new RecipeScalingUpgradeRecipe(identifier, name, productionTime, input, null, rank, null);
			String[] data = { toUpgrade, followUpRecipe };
			recipeScalingUpgradeMapping.put((RecipeScalingUpgradeRecipe) result, data);
			break;
//		case "DUMMY":
//			result = new DummyParsingRecipe(identifier, name, productionTime, null);
//			break;
		default:
			plugin.error("Could not identify type " + config.getString("type") + " as a valid recipe identifier",
					false);
			result = null;
		}
		if (result != null) {
			((InputRecipe) result)
					.setFuelConsumptionInterval((int) parseTime(config.getString("fuel_consumption_interval", "-1")));
			plugin.info("Parsed recipe " + name);
		}
		return result;
	}

	private ProductionRecipeModifier parseProductionRecipeModifier(ConfigurationSection config) {
		ProductionRecipeModifier modi = new ProductionRecipeModifier();
		if (config == null) {
			return null;
		}
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				plugin.warning("Found invalid config value at " + config.getCurrentPath() + " " + key
						+ ". Only identifiers for recipe modifiers allowed at this level");
				continue;
			}
			int minimumRunAmount = current.getInt("minimumRunAmount");
			int maximumRunAmount = current.getInt("maximumRunAmount");
			double minimumMultiplier = current.getDouble("baseMultiplier");
			double maximumMultiplier = current.getDouble("maximumMultiplier");
			int rank = current.getInt("rank");
			modi.addConfig(minimumRunAmount, maximumRunAmount, minimumMultiplier, maximumMultiplier, rank);
		}
		return modi;
	}

}
