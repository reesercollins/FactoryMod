package reesercollins.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.builders.IFactoryBuilder;
import reesercollins.FactoryMod.builders.ProductionBuilder;
import reesercollins.FactoryMod.factories.ProductionFactory;
import reesercollins.FactoryMod.itemHandling.ItemMap;

public class UpgradeRecipe extends InputRecipe {

	private IFactoryBuilder builder;

	public UpgradeRecipe(String identifier, String name, int productionTime, ItemMap input, IFactoryBuilder builder) {
		super(identifier, name, productionTime, input);
		this.builder = builder;
	}

	public void applyEffect(Inventory i, ProductionFactory pf) {
		logAfterRecipeRun(i, pf);
		if (input.isContainedIn(i)) {
			if (input.removeSafelyFrom(i)) {
				ProductionBuilder e = (ProductionBuilder) builder;
				pf.upgrade(e.getName(), e.getRecipes(), e.getFuel(), e.getFuelConsumptionInterval(), e.getUpdateTime(),
						e.getMaximumHealth(), e.getDamagePerDamagingPeriod(), e.getBreakGracePeriod(),
						e.getBreakReduction());
			}
		}
		logAfterRecipeRun(i, pf);
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = ((InputRecipe) ((ProductionBuilder) builder).getRecipes().get(0))
				.getOutputRepresentation(null, null).get(0);
		res.setAmount(1);
		ItemMeta im = res.getItemMeta();
		im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		im.setDisplayName(getName());
		res.setItemMeta(im);
		return res;
	}

	public List<ItemStack> getInputRepresentation(Inventory i, ProductionFactory pf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		LinkedList<ItemStack> result = new LinkedList<ItemStack>();
		ItemMap inventoryMap = new ItemMap(i);
		ItemMap possibleRuns = new ItemMap();
		for (Entry<ItemStack, Integer> entry : input.getEntrySet()) {
			if (inventoryMap.getAmount(entry.getKey()) != 0) {
				possibleRuns.addItemAmount(entry.getKey(), inventoryMap.getAmount(entry.getKey()) / entry.getValue());
			} else {
				possibleRuns.addItemAmount(entry.getKey(), 0);
			}
		}
		for (ItemStack is : input.getItemStackRepresentation()) {
			List<String> lore = new ArrayList<String>();
			ItemMeta im = is.getItemMeta();
			if (possibleRuns.getAmount(is) != 0) {
				lore.add(ChatColor.GREEN + "Enough of this material available to upgrade");
			} else {
				lore.add(ChatColor.RED + "Not enough of this materials available to upgrade");
			}
			im.setLore(lore);
			is.setItemMeta(im);
			result.add(is);
		}
		return result;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i, ProductionFactory pf) {
		List<ItemStack> res = new LinkedList<ItemStack>();

		ItemStack cr = new ItemStack(Material.CRAFTING_TABLE);
		ItemMeta crMeta = cr.getItemMeta();
		crMeta.setDisplayName(builder.getName());
		crMeta.setLore(
				new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Upgrade to get new and better recipes")));
		cr.setItemMeta(crMeta);
		res.add(cr);

		ItemStack fur = new ItemStack(Material.FURNACE);
		ItemMeta furMeta = fur.getItemMeta();
		furMeta.setDisplayName(builder.getName());
		List<String> furLore = new ArrayList<String>();
		furLore.add(ChatColor.LIGHT_PURPLE + "Recipes:");
		for (IRecipe rec : ((ProductionBuilder) builder).getRecipes()) {
			furLore.add(ChatColor.YELLOW + rec.getName());
		}
		furMeta.setLore(furLore);
		fur.setItemMeta(furMeta);
		res.add(fur);

		ItemStack che = new ItemStack(Material.CHEST);
		ItemMeta cheMeta = che.getItemMeta();
		List<String> cheLore = new ArrayList<String>();
		cheLore.add(ChatColor.LIGHT_PURPLE + "Careful, you can not");
		cheLore.add(ChatColor.LIGHT_PURPLE + "revert upgrades!");
		cheMeta.setLore(cheLore);
		cheMeta.setDisplayName(builder.getName());
		che.setItemMeta(cheMeta);
		res.add(che);
		return res;
	}

	public IFactoryBuilder getBuilder() {
		return builder;
	}

}
