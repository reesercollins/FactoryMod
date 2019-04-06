package reesercollins.FactoryMod.factories;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.DirectionalContainer;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.interaction.IInteractionManager;
import reesercollins.FactoryMod.power.IPowerManager;
import reesercollins.FactoryMod.repair.IRepairManager;
import reesercollins.FactoryMod.structures.MultiBlockStructure;

public abstract class Factory implements Runnable {

	protected IInteractionManager im;
	protected IRepairManager rm;
	protected IPowerManager pm;
	protected boolean active;
	protected MultiBlockStructure mbs;
	protected int updateTime;
	protected String name;
	protected int threadId;

	public Factory(IInteractionManager im, IRepairManager rm, IPowerManager pm, MultiBlockStructure mbs, int updateTime,
			String name) {
		this.im = im;
		this.rm = rm;
		this.mbs = mbs;
		this.pm = pm;
		this.updateTime = updateTime;
		this.name = name;
	}
	
	public enum FactoryType {
		PRODUCTION,
		SORTER,
		PIPE;
	}

	/**
	 * @return The manager which handles health, repairs and decay of the factory
	 */
	public IRepairManager getRepairManager() {
		return rm;
	}

	/**
	 * @return The manager which handles any sort of player interaction with the
	 *         factory
	 */
	public IInteractionManager getInteractionManager() {
		return im;
	}

	/**
	 * @return The manager which handles power and it's consumption for this factory
	 */
	public IPowerManager getPowerManager() {
		return pm;
	}

	/**
	 * @return Whether this factory is currently turned on
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return The physical structure representing this factory
	 */
	public MultiBlockStructure getMultiBlockStructure() {
		return mbs;
	}

	/**
	 * @return How often this factory is updated when it's turned on, measured in
	 *         ticks
	 */
	public int getUpdateTime() {
		return updateTime;
	}

	/**
	 * Names are not unique for factory instances, but simply describe a broader
	 * functionality group. Factories implemented by the same class can have
	 * different names, but factories with the same name should have the exact same
	 * functionality
	 * 
	 * @return name of this factory
	 */
	public String getName() {
		return name;
	}

	/**
	 * Activates this factory
	 */
	public abstract void activate();

	/**
	 * Deactivates this factory
	 */
	public abstract void deactivate();

	/**
	 * Attempts to turn this factory on and does any checks needed
	 * 
	 * @param p         Player turning the factory on or null if something other
	 *                  than a player is attempting to turn it on
	 * @param onStartUp Whether this factory is just being reactivated after a
	 *                  restart/reload and any permissions checks should be bypassed
	 */
	public abstract void attemptToActivate(Player p, boolean onStartUp);

	public void scheduleUpdate() {
		threadId = FMPlugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(FMPlugin.getInstance(),
				this, (long) updateTime);
	}

	public void turnFurnaceOn(Block f) {
		if (f.getType() != Material.FURNACE) {
			return;
		}
		
		Furnace furnace = (Furnace) f.getState();
			
		ItemStack[] oldContents = furnace.getInventory().getStorageContents();
		BlockFace facing = ((DirectionalContainer) furnace.getData()).getFacing();
		furnace.getInventory().clear();
		furnace = (Furnace) f.getState().getBlockData();
		((DirectionalContainer) furnace).setFacingDirection(facing);
		furnace.update();
		furnace.setBurnTime(Short.MAX_VALUE);
		furnace.getInventory().setContents(oldContents);
	}

	public String getLogData() {
		return name + " at " + mbs.getCenter().toString();
	}

	public void turnFurnaceOff(Block f) {
		if (f.getType() != Material.FURNACE) {
			return;
		}
		Furnace furnace = (Furnace) f.getState();
		furnace.setBurnTime((short) 0);
		furnace.update();
	}

}
