package reesercollins.FactoryMod.interaction.clickable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DecorationStack extends Clickable {

	public DecorationStack(ItemStack item) {
		super(item);
	}

	@Override
	public void clicked(Player p) {
		// Just for decoration
	}

}
