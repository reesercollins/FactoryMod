package reesercollins.FactoryMod.repair;

import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.utils.LoggingUtils;

public class PercentageHealthRepairManager implements IRepairManager {

	private int health;
	private Factory factory;
	private long breakTime;
	private int maximumHealth;
	private int damageAmountPerDecayInterval;
	private long gracePeriod;

	public PercentageHealthRepairManager(int initialHealth, int maximumHealth, long breakTime,
			int damageAmountPerDecayInterval, long gracePeriod) {
		this.health = initialHealth;
		this.maximumHealth = maximumHealth;
		this.breakTime = breakTime;
		this.damageAmountPerDecayInterval = damageAmountPerDecayInterval;
		this.gracePeriod = gracePeriod;
	}

	public int getMaximumHealth() {
		return maximumHealth;
	}

	public int getDamageAmountPerDecayInterval() {
		return damageAmountPerDecayInterval;
	}

	public long getGracePeriod() {
		return gracePeriod;
	}

	public int getRawHealth() {
		return health;
	}

	public long getBreakTime() {
		return breakTime;
	}

	public void setFactory(Factory factory) {
		this.factory = factory;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public void setBreakTime(long breakTime) {
		this.breakTime = breakTime;
	}

	public void repair(int amount) {
		health = Math.min(health + amount, maximumHealth);
		breakTime = 0;
	}

	public static void returnStuff(Factory factory) {
		double rate = FMPlugin.getManager().getBuilder(factory.getName()).getReturnRate();
		if (rate == 0.0) {
			return;
		}
		for (Entry<ItemStack, Integer> items : FMPlugin.getManager().getTotalSetupCost(factory).getEntrySet()) {
			int returnAmount = (int) (items.getValue() * rate);
			ItemMap im = new ItemMap();
			im.addItemAmount(items.getKey(), returnAmount);
			for (ItemStack is : im.getItemStackRepresentation()) {
				factory.getMultiBlockStructure().getCenter().getWorld()
						.dropItemNaturally(factory.getMultiBlockStructure().getCenter(), is);
			}
		}
	}

	@Override
	public void breakIt() {
		health = 0;
		if (breakTime == 0) {
			breakTime = System.currentTimeMillis();
		}
		if (factory.getMultiBlockStructure().releventBlocksDestroyed()) {
			LoggingUtils.log(factory.getLogData() + " removed because blocks were destroyed");
			FMPlugin.getManager().removeFactory(factory);
			returnStuff(factory);
		}

	}

	@Override
	public String getHealth() {
		return String.valueOf(health / (maximumHealth / 100)) + "." + String.valueOf(health % (maximumHealth / 100))
				+ " %";
	}

	@Override
	public boolean atFullHealth() {
		return health >= maximumHealth;
	}

	@Override
	public boolean inDisrepair() {
		return health <= 0;
	}

}
