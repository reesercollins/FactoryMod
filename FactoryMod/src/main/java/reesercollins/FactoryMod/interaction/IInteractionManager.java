package reesercollins.FactoryMod.interaction;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;

public interface IInteractionManager {

	/**
	 * Called if a player right clicks a block, which is part of the factory of this
	 * manager
	 * 
	 * @param p Player who clicked
	 * @param b Block which was clicked
	 */
	public void rightClick(Player p, Block b, BlockFace bf);

	/**
	 * Called if a player left clicks a block, which is part of the factory of this
	 * manager
	 * 
	 * @param p Player who clicked
	 * @param b Block which was clicked
	 */
	public void leftClick(Player p, Block b, BlockFace bf);

	/**
	 * Called if a block, which is part of the factory of this manager is broken,
	 * this can have various causes such as players, fire or explosions
	 * 
	 * @param p Player who broke the block or null if no player was the direct cause
	 * @param b Block which was broken
	 */
	public void blockBreak(Player p, Block b);

	/**
	 * Called if a redstone event occurs for any block which is part of the factory
	 * of this manager
	 * 
	 * @param e Event which occured
	 */
	public void redstoneEvent(BlockRedstoneEvent e, Block factoryBlock);

}
