package reesercollins.FactoryMod.itemHandling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMap {

	private static final Logger log = Bukkit.getLogger();

	private HashMap<ItemStack, Integer> items;
	private int totalItems;

	/**
	 * Create an empty ItemMap
	 */
	public ItemMap() {
		items = new HashMap<>();
		totalItems = 0;
	}

	/**
	 * Create an ItemMap with the contents of the inventory provided. The ItemMap
	 * will not stay in sync with the inventory unless told to update.
	 * 
	 * @param inv The inventory to copy.
	 */
	public ItemMap(Inventory inv) {
		totalItems = 0;
		update(inv);
	}

	/**
	 * Create an ItemMap based off a single ItemStack.
	 * 
	 * @param is ItemStack to be added.
	 */
	public ItemMap(ItemStack is) {
		items = new HashMap<>();
		totalItems = 0;
		addItemStack(is);
	}

	/**
	 * Create an ItemMap based on a collection of ItemStacks.
	 * 
	 * @param stacks The collection of stacks to be added.
	 */
	public ItemMap(Collection<ItemStack> stacks) {
		items = new HashMap<>();
		addAll(stacks);
	}

	/**
	 * Clones the ItemStack, and adds it to the ItemMap, combining stacks where
	 * possible.
	 * 
	 * @param is The ItemStack to be added.
	 */
	public void addItemStack(ItemStack input) {
		if (input != null) {
			ItemStack is = createMapConformCopy(input);
			if (is == null) {
				return;
			}

			Integer i;
			if ((i = items.get(is)) != null) {
				items.put(is, i + input.getAmount());
			} else {
				items.put(is, input.getAmount());
			}
			totalItems += input.getAmount();
		}
	}

	/**
	 * Remove ItemStack from ItemMap. If ItemStack amount is less than the amount in
	 * the ItemMap, the difference stays as an entry.
	 * 
	 * @param input The ItemStack to remove.
	 */
	public void removeItemStack(ItemStack input) {
		ItemStack is = createMapConformCopy(input);
		if (is == null) {
			return;
		}
		Integer value = items.get(is);
		if (value != null) {
			int newVal = value - input.getAmount();
			if (newVal > 0) {
				items.put(is, newVal);
			} else {
				items.remove(is);
			}
		}
	}

	/**
	 * Completely removes an item type from the ItemMap, regardless of the amount of
	 * items in the ItemStack.
	 * 
	 * @param input The ItemStack to remove.
	 */
	public void removeItemStackCompletely(ItemStack input) {
		ItemStack is = createMapConformCopy(input);
		if (is != null) {
			items.remove(is);
		}
	}

	/**
	 * Attempts to remove the content of this ItemMap from the given inventory. If
	 * it fails to find all the required items it will stop and return false
	 *
	 * @param i Inventory to remove from
	 * @return True if everything was successfully removed, false if not
	 */
	public boolean removeSafelyFrom(Inventory i) {
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			int amountToRemove = entry.getValue();
			ItemStack is = entry.getKey();
			for (ItemStack inventoryStack : i.getContents()) {
				if (inventoryStack == null) {
					continue;
				}
				if (inventoryStack.getType() == is.getType()) {
					ItemMap compareMap = new ItemMap(inventoryStack);
					int removeAmount = Math.min(amountToRemove, compareMap.getAmount(is));
					if (removeAmount != 0) {
						ItemStack cloneStack = inventoryStack.clone();
						cloneStack.setAmount(removeAmount);
						if (i.removeItem(cloneStack).values().size() != 0) {
							return false;
						} else {
							amountToRemove -= removeAmount;
							if (amountToRemove <= 0) {
								break;
							}
						}
					}
				}
			}
			if (amountToRemove > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds all ItemStacks in the collection with the same functionality of
	 * {{@link #addItemStack(ItemStack)}.
	 * 
	 * @param stacks The ItemStacks to add.
	 */
	public void addAll(Collection<ItemStack> stacks) {
		for (ItemStack is : stacks) {
			if (is != null) {
				addItemStack(is);
			}
		}
	}

	/**
	 * Adds the given amount the the ItemStack in the ItemMap.
	 * 
	 * @param input  The ItemStack to add to.
	 * @param amount The amount to add.
	 */
	public void addItemAmount(ItemStack input, int amount) {
		ItemStack copy = createMapConformCopy(input);
		if (copy == null) {
			return;
		}
		copy.setAmount(amount);
		addItemStack(copy);
	}

	/**
	 * Gets an instance of an ItemMap only containing ItemStacks of the given
	 * Material in the current ItemMap.
	 * 
	 * @param mat The Material to match.
	 * @return A new ItemMap instance with only matching ItemStacks.
	 */
	public ItemMap getStacksByMaterial(Material mat) {
		ItemMap result = new ItemMap();
		for (ItemStack is : items.keySet()) {
			if (is.getType() == mat) {
				result.addItemAmount(is.clone(), items.get(is));
			}
		}
		return result;
	}

	public ItemMap getStacksByMaterial(ItemStack is) {
		return getStacksByMaterial(is.getType());
	}

	/**
	 * Gets an instance of an ItemMap only containing ItemStacks matching the given
	 * lore in the current ItemMap.
	 * 
	 * @param lore The lore to match.
	 * @return A new ItemMap instance with only matching ItemStacks.
	 */
	public ItemMap getStacksByLore(List<String> lore) {
		ItemMap result = new ItemMap();
		for (ItemStack is : items.keySet()) {
			if (is.getItemMeta() != null && is.getItemMeta().getLore().equals(lore)) {
				result.addItemAmount(is.clone(), items.get(is));
			}
		}
		return result;
	}

	/**
	 * Gets the total number of items matching the given ItemStack in the current
	 * ItemMap.
	 * 
	 * @param is The ItemStack to match.
	 * @return The total number of items.
	 */
	public int getAmount(ItemStack is) {
		ItemMap matSubMap = getStacksByMaterial(is);
		int amount = 0;
		for (Entry<ItemStack, Integer> entry : matSubMap.getEntrySet()) {
			ItemStack current = entry.getKey();
			if (is.isSimilar(current)) {
				amount += entry.getValue();
			}
		}
		return amount;
	}

	/**
	 * @return How many items are stored in this map, total.
	 */
	public int getTotalItemAmount() {
		return totalItems;
	}

	/**
	 * @return The number of different items in this map.
	 */
	public int getTotalUniqueItemAmount() {
		return items.keySet().size();
	}

	@SuppressWarnings("unchecked")
	public Set<Entry<ItemStack, Integer>> getEntrySet() {
		return ((HashMap<ItemStack, Integer>) items.clone()).entrySet();
	}

	/**
	 * Checks whether an inventory contains exactly what's described in this ItemMap
	 *
	 * @param i Inventory to compare
	 * @return True if the inventory is identical with this instance, false if not
	 */
	public boolean containedExactlyIn(Inventory i) {
		ItemMap invMap = new ItemMap(i);
		for (Entry<ItemStack, Integer> entry : items.entrySet()) {
			if (!entry.getValue().equals(invMap.getAmount(entry.getKey()))) {
				return false;
			}
		}
		for (ItemStack is : i.getContents()) {
			if (is == null) {
				continue;
			}
			if (getStacksByMaterial(is).getTotalUniqueItemAmount() == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether this instance is completely contained in the given inventory,
	 * which means every stack in this instance is also in the given inventory and
	 * the amount the same or bigger as in this instance
	 *
	 * @param i Inventory to check
	 * @return True if this instance is completely contained in the given inventory,
	 *         false if not
	 */
	public boolean isContainedIn(Inventory i) {
		ItemMap invMap = new ItemMap(i);
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			if (entry.getValue() > invMap.getAmount(entry.getKey())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether this instance would completely fit into the given inventory
	 *
	 * @param i Inventory to check
	 * @return True if this ItemMap's item representation would completely fit in
	 *         the inventory, false if not
	 */
	public boolean fitsIn(Inventory i) {
		int size;
		if (i instanceof PlayerInventory) {
			size = 36;
		} else {
			size = i.getSize();
		}
		ItemMap invCopy = new ItemMap();
		for (ItemStack is : i.getStorageContents()) {
			invCopy.addItemStack(is);
		}
		ItemMap instanceCopy = this.clone();
		instanceCopy.merge(invCopy);
		return instanceCopy.getItemStackRepresentation().size() <= size;
	}

	/**
	 * Checks how often this ItemMap is contained in the given Inventory or how
	 * often this ItemMap could be removed from the given inventory before creating
	 * negative stacks
	 *
	 * @param i Inventory to check
	 * @return How often this map is contained in the given inventory or
	 *         Integer.MAX_VALUE if this instance is empty
	 */
	public int getMultiplesContainedIn(Inventory i) {
		ItemMap invMap = new ItemMap(i);
		int res = Integer.MAX_VALUE;
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			int pulledAmount = invMap.getAmount(entry.getKey());
			int multiples = pulledAmount / entry.getValue();
			res = Math.min(res, multiples);
		}
		return res;
	}

	/**
	 * Multiplies the whole content of this instance by the given multiplier
	 *
	 * @param multiplier Multiplier to scale the amount of the contained items with
	 */
	public void multiplyContent(double multiplier) {
		totalItems = 0;
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			items.put(entry.getKey(), (int) (entry.getValue() * multiplier));
			totalItems += (int) (entry.getValue() * multiplier);
		}
	}

	/**
	 * Turns this item map into a list of ItemStacks, with amounts that do not
	 * surpass the maximum allowed stack size for each ItemStack
	 *
	 * @return List of stack-size conforming ItemStacks
	 */
	public LinkedList<ItemStack> getItemStackRepresentation() {
		LinkedList<ItemStack> result = new LinkedList<>();
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			ItemStack is = entry.getKey();
			Integer amount = entry.getValue();
			while (amount != 0) {
				ItemStack toAdd = is.clone();
				int addAmount = Math.min(amount, is.getMaxStackSize());
				toAdd.setAmount(addAmount);
				result.add(toAdd);
				amount -= addAmount;
			}
		}
		return result;
	}

	/**
	 * Instead of converting into many stacks of maximum size, this creates a stack
	 * with an amount of one for each entry and adds the total item amount and stack
	 * count as lore, which is needed to display larger ItemMaps in inventories
	 *
	 * @return UI representation of large ItemMap
	 */
	public List<ItemStack> getLoredItemCountRepresentation() {
		List<ItemStack> items = new LinkedList<>();
		List<String> lore = new ArrayList<String>();
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			ItemStack is = entry.getKey().clone();
			lore.add(ChatColor.GOLD + "Total item count: " + entry.getValue());
			if (entry.getValue() > entry.getKey().getType().getMaxStackSize()) {
				int stacks = entry.getValue() / is.getType().getMaxStackSize();
				int extra = entry.getValue() % is.getType().getMaxStackSize();
				StringBuilder out = new StringBuilder(ChatColor.GOLD.toString());
				if (stacks != 0) {
					out.append(stacks + " stack" + (stacks == 1 ? "" : "s"));
				}
				if (extra != 0) {
					out.append(" and " + extra);
					out.append(" item" + (extra == 1 ? "" : "s"));
				}
				lore.add(out.toString());
			}

			ItemMeta meta = is.getItemMeta();
			meta.setLore(lore);
			is.setItemMeta(meta);
			items.add(is);
		}
		return items;
	}

	/**
	 * Merges the given item map into this instance
	 *
	 * @param im ItemMap to merge
	 */
	public void merge(ItemMap im) {
		for (Entry<ItemStack, Integer> entry : im.getEntrySet()) {
			addItemAmount(entry.getKey(), entry.getValue());
		}
	}

	public void update(Inventory inv) {
		items = new HashMap<>();
		totalItems = 0;
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack is = inv.getItem(i);
			if (is != null) {
				addItemStack(is);
			}
		}
	}

	/**
	 * Clones this map
	 */
	@Override
	public ItemMap clone() {
		ItemMap clone = new ItemMap();
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			clone.addItemAmount(entry.getKey(), entry.getValue());
		}
		return clone;
	}

	@Override
	public int hashCode() {
		int result = 0;
		for (Entry<ItemStack, Integer> entry : items.entrySet()) {
			result += entry.hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		String res = "";
		for (ItemStack is : getItemStackRepresentation()) {
			res += is.toString() + ";";
		}
		return res;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ItemMap) {
			ItemMap im = (ItemMap) o;
			if (im.getTotalItemAmount() == getTotalItemAmount()) {
				return im.getEntrySet().equals(getEntrySet());
			}
		}
		return false;
	}

	private static ItemStack createMapConformCopy(ItemStack is) {
		ItemStack copy = is.clone();
		copy.setAmount(1);
		net.minecraft.server.v1_13_R2.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(copy);
		if (nmsItemStack == null) {
			log.info("Attempted to create ItemMap compliant copy of " + copy.toString()
					+ " but was unable to. This item is not able to be held in inventories.");
			return null;
		}
		nmsItemStack.setRepairCost(0);
		copy = CraftItemStack.asBukkitCopy(nmsItemStack);
		return copy;
	}

}
