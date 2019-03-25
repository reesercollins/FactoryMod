package reesercollins.FactoryMod.interaction.clickable;

import org.bukkit.inventory.ItemStack;

public abstract class Clickable implements IClickable {

	protected ItemStack item;

	public Clickable(ItemStack item) {
		this.item = item;
	}

	/**
	 * @return Which item stack represents this clickable
	 */
	@Override
	public ItemStack getItemStack() {
		return item;
	}

	@Override
	public void addedToInventory(ClickableInventory inv, int slot) {
		// dont need anything for static representation
	}

}
