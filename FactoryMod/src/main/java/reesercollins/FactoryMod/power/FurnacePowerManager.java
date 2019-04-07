package reesercollins.FactoryMod.power;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import reesercollins.FactoryMod.itemHandling.ItemMap;

public class FurnacePowerManager implements IPowerManager {

	private ItemStack fuel;
	private int powerCounter;
	private int fuelConsumptionInterval;
	private Block furnace;

	public FurnacePowerManager(Block furnace, ItemStack fuel, int fuelConsumptionInterval) {
		this.fuel = fuel;
		this.fuelConsumptionInterval = fuelConsumptionInterval;
		this.furnace = furnace;
	}

	public FurnacePowerManager(ItemStack fuel, int fuelConsumptionInterval) {
		this.fuel = fuel;
		this.fuelConsumptionInterval = fuelConsumptionInterval;
	}

	public int getFuelAmountAvailable() {
		return new ItemMap(((Furnace) furnace.getState()).getInventory()).getAmount(fuel);
	}

	@Override
	public void consumePower() {
		FurnaceInventory fi = ((Furnace) furnace.getState()).getInventory();
		fi.removeItem(fuel);
	}

	@Override
	public boolean powerAvailable() {
		if (furnace.getType() != Material.FURNACE) {
			throw new UnknownError("Was expecting FURNACE, instead got " + furnace.getType());
		}
		ItemMap im = new ItemMap(((Furnace) furnace.getState()).getInventory());
		return im.getAmount(fuel) > 0;
	}

	@Override
	public int getPowerConsumptionInterval() {
		return fuelConsumptionInterval;
	}

	@Override
	public int getPowerCounter() {
		return powerCounter;
	}

	@Override
	public void increasePowerCounter(int amount) {
		powerCounter += amount;
	}

	@Override
	public void setPowerCounter(int value) {
		powerCounter = value;
	}

}
