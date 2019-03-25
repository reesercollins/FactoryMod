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
		String result = (months != 0 ? months + " months" : "") + (days != 0 ? days + " days" : "") + (hours != 0 ? hours + " hours" : "");
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

		// We don't inherit identifier, because each one is unique.
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

		String type = config.getString("type", (parentRecipe != null) ? parentRecipe.getTypeIdentifier() : null);
		if (type == null) {
			plugin.warning("No type specified for recipe at " + config.getCurrentPath() + ". Skipping the recipe.");
			return null;
		}
		return null;

	}

}
