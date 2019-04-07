package reesercollins.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;

public class RandomEnchantingRecipe extends InputRecipe {

	private Material tool;
	private List<RandomEnchant> enchants;
	private static Random rng;

	public class RandomEnchant {
		private Enchantment enchant;
		private int level;
		private double chance;

		public RandomEnchant(Enchantment enchant, int level, double chance) {
			this.enchant = enchant;
			this.level = level;
			this.chance = chance;
		}
	}

	public RandomEnchantingRecipe(String identifier, String name, int productionTime, ItemMap input, Material tool,
			List<RandomEnchant> enchants) {
		super(identifier, name, productionTime, input);
		this.tool = tool;
		this.enchants = enchants;
		if (rng == null) {
			rng = new Random();
		}
	}

	@Override
	public void applyEffect(Inventory i, ProductionFactory pf) {
		logBeforeRecipeRun(i, pf);
		for (ItemStack is : input.getItemStackRepresentation()) {
			i.removeItem(is);
		}
		for (ItemStack is : i.getContents()) {
			if (is != null && is.getType() == tool && !is.getItemMeta().hasEnchants()) {
				boolean applied = false;
				while (!applied) {
					for (RandomEnchant re : enchants) {
						if (rng.nextDouble() <= re.chance) {
							is.getItemMeta().addEnchant(re.enchant, re.level, true);
							applied = true;
						}
					}
				}
				break;
			}
		}
		logAfterRecipeRun(i, pf);
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, ProductionFactory pf) {
		if (i == null) {
			List<ItemStack> bla = input.getItemStackRepresentation();
			bla.add(new ItemStack(tool));
			return bla;
		}
		List<ItemStack> returns = createLoredStacksForInfo(i);
		ItemStack toSt = new ItemStack(tool);
		ItemMeta meta = toSt.getItemMeta();
		meta.setLore(new ArrayList<String>(Arrays.asList(
				ChatColor.GREEN + "Enough materials for " + new ItemMap(toSt).getMultiplesContainedIn(i) + " runs")));
		toSt.setItemMeta(meta);
		returns.add(toSt);
		return returns;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, ProductionFactory pf) {
		ItemStack is = new ItemStack(tool);
		for (RandomEnchant re : enchants) {
			is.addEnchantment(re.enchant, re.level);
		}
		ItemMeta im = is.getItemMeta();
		List<String> lore = new ArrayList<String>();
		if (i != null) {
			lore.add(ChatColor.GREEN + "Enough materials for " + String.valueOf(Math
					.max(new ItemMap(new ItemStack(tool)).getMultiplesContainedIn(i), input.getMultiplesContainedIn(i)))
					+ " runs");
		}
		for (RandomEnchant re : enchants) {
			lore.add(ChatColor.YELLOW + String.valueOf(re.chance * 100) + " % chance for "
					+ re.enchant.getKey().toString().replace("minecraft:", "") + " " + re.level);
		}
		lore.add(ChatColor.LIGHT_PURPLE + "At least one guaranteed");
		im.setLore(lore);
		is.setItemMeta(im);
		List<ItemStack> stacks = new LinkedList<ItemStack>();
		stacks.add(is);
		return stacks;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		ItemStack is = new ItemStack(tool);
		for (RandomEnchant re : enchants) {
			is.addEnchantment(re.enchant, re.level);
		}
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.RESET + name);
		is.setItemMeta(im);
		return is;
	}

	@Override
	public RecipeType getTypeIdentifier() {
		return RecipeType.RANDOMENCHANT;
	}

}
