package reesercollins.FactoryMod.builders;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.factories.Factory.FactoryType;
import reesercollins.FactoryMod.structures.MultiBlockStructure;

public interface IFactoryBuilder {

	/**
	 * Called when a factory is to be build. This should only be called once all
	 * checks have been complete.
	 * 
	 * @param mbs The physical representation of the factory
	 * @param p   The player creating the factory
	 * @return The created factory object
	 */
	public Factory build(MultiBlockStructure mbs, Player p);

	/**
	 * Attempts to create a factory with the given block as new center block. If all
	 * blocks for a specific structure are there and other conditions needed for the
	 * factory type are fulfilled, the factory is created and added to the manager
	 * 
	 * @param b Center block
	 * @param p Player attempting to create the factor
	 */
	public void attemptCreation(Block b, Player p);

	/**
	 * Each factory has a unique name. There can be as many builders of the same
	 * type as needed, but they should never have the same name.
	 * 
	 * @return The name of this builder and its factory
	 */
	public FactoryType getType();

	/**
	 * When destroyed completely a factory may return a part of it's setup cost.
	 * This value specifies how much of the setup cost is returned (as a multiplier)
	 * 
	 * @return Multiplier of the setup cost which is returned upon destruction
	 */
	public double getReturnRate();

	/**
	 * All the factories created are represented through a MultiBlockStructure and
	 * this is the getter for it
	 * 
	 * @return Structure class of the factory created by this builder
	 */
	public Class<? extends MultiBlockStructure> getMultiBlockStructure();

}
