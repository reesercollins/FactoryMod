package reesercollins.FactoryMod.repair;

import reesercollins.FactoryMod.factories.Factory;

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

	@Override
	public void breakIt() {
		health = 0;
		if (breakTime == 0) {
			breakTime = System.currentTimeMillis();
		}
		// TODO Finish
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
