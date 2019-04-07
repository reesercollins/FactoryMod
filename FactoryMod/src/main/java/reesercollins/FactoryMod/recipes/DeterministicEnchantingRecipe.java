package reesercollins.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;

public class DeterministicEnchantingRecipe extends InputRecipe {

	private ItemMap tool;
	private Enchantment enchant;
	private int level;

	public DeterministicEnchantingRecipe(String identifier, String name, int productionTime, ItemMap input,
			ItemMap tool, Enchantment enchant, int level) {
		super(identifier, name, productionTime, input);
		this.tool = tool;
		this.enchant = enchant;
		this.level = level;
	}

	public boolean enoughMaterialAvailable(Inventory i) {
		if (input.isContainedIn(i)) {
			ItemStack toolIs = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : i.getContents()) {
				if (is != null && toolIs.getType() == is.getType()
						&& toolIs.getEnchantmentLevel(enchant) == is.getEnchantmentLevel(enchant)) {
					return true;
				}
			}
		}
		return false;
	}

	public ItemMap getTool() {
		return tool;
	}

	public Enchantment getEnchant() {
		return enchant;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public void applyEffect(Inventory i, ProductionFactory pf) {
		logBeforeRecipeRun(i, pf);
		if (input.removeSafelyFrom(i)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : i.getContents()) {
				if (is != null && toolio.getType() == is.getType()
						&& toolio.getEnchantmentLevel(enchant) == is.getEnchantmentLevel(enchant)) {
					ItemMeta im = is.getItemMeta();
					im.removeEnchant(enchant);
					im.addEnchant(enchant, level, true);
					is.setItemMeta(im);
					break;
				}
			}
		}
		logAfterRecipeRun(i, pf);
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, ProductionFactory pf) {
		if (i == null) {
			List<ItemStack> bla = input.getItemStackRepresentation();
			bla.add(tool.getItemStackRepresentation().get(0));
			return bla;
		}
		List<ItemStack> returns = createLoredStacksForInfo(i);
		ItemStack toSt = tool.getItemStackRepresentation().get(0);
		ItemMeta meta = toSt.getItemMeta();
		meta.setLore(new ArrayList<String>(Arrays.asList(
				ChatColor.GREEN + "Enough materials for " + new ItemMap(toSt).getMultiplesContainedIn(i) + " runs")));
		toSt.setItemMeta(meta);
		returns.add(toSt);
		return returns;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, ProductionFactory pf) {
		ItemStack is = tool.getItemStackRepresentation().get(0);
		ItemMeta im = is.getItemMeta();
		im.removeEnchant(enchant);
		im.addEnchant(enchant, level, true);
		if (i != null) {
			im.setLore(new ArrayList<String>(Arrays.asList(ChatColor.GREEN + "Enough materials for "
					+ Math.min(tool.getMultiplesContainedIn(i), input.getMultiplesContainedIn(i)) + " runs")));
		}
		is.setItemMeta(im);
		List<ItemStack> stacks = new LinkedList<ItemStack>();
		stacks.add(is);
		return stacks;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		ItemStack is = tool.getItemStackRepresentation().get(0);
		ItemMeta im = is.getItemMeta();
		im.removeEnchant(enchant);
		im.addEnchant(enchant, level, true);
		im.setDisplayName(ChatColor.RESET + name);
		is.setItemMeta(im);
		return is;
	}

	@Override
	public RecipeType getTypeIdentifier() {
		return RecipeType.ENCHANT;
	}

}
