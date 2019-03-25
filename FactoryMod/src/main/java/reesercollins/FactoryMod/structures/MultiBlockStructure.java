package reesercollins.FactoryMod.structures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public abstract class MultiBlockStructure {
	
	public static BlockFace[] allBlockSides = new BlockFace[] {
		BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST	
	};
	
	public static BlockFace[] cardinalBlockSides = new BlockFace[] {
		BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
	};
	
	/**
	 * Checks all sides of a block for all blocks matching the material given.
	 * 
	 * @param block The blocks to search around.
	 * @param material The material to match.
	 * @return A list containing all blocks matching the material.
	 */
	public static List<Block> searchForBlocksOnAllSides(Block block, Material material) {
		LinkedList<Block> blocks = new LinkedList<>();
		for (BlockFace face : allBlockSides) {
			Block side = block.getRelative(face);
			if (side.getType() == material) {
				blocks.add(side);
			}
		}
		return blocks;
	}
	
	/**
	 * Checks all cardinal directions around a block for all blocks matching the material given.
	 * 
	 * @param block The blocks to search around.
	 * @param material The material to match.
	 * @return A list containing all blocks matching the material.
	 */
	public static List<Block> searchForBlocksOnSides(Block block, Material material) {
		LinkedList<Block> blocks = new LinkedList<>();
		for (BlockFace face : cardinalBlockSides) {
			Block side = block.getRelative(face);
			if (side.getType() == material) {
				blocks.add(side);
			}
		}
		return blocks;
	}
	
	public static List<Block> getAdjacentBlocks(Block block) {
		List<Block> blocks = new LinkedList<>();
		for (BlockFace face : allBlockSides) {
			blocks.add(block.getRelative(face));
		}
		return blocks;
	}
	
	/**
	 * @return True if all blocks in this factory are where they are supposed to be.
	 */
	public abstract boolean isValid();
	
	/**
	 * Checks whether all blocks in this factory are where they are supposed to be, 
	 * and updates the return value of {@link #isValid()}
	 */
	public abstract void checkIsValid();
	
	/**
	 * Gets all blocks that are part of this factory. Please refer to each factory's class
	 * description for the correct order of blocks that the list should be placed in.
	 * 
	 * @return All blocks in this factory.
	 */
	public abstract List<Location> getAllBlocks();
	
	/**
	 * @return All blocks in the factory which can be interacted with (e.g. Chests, Furnaces)
	 */
	public abstract List<Block> getReleventBlocks();
	
	/**
	 * @return True if <strong>all</strong> relevant blocks (returned by {{@link #getReleventBlocks()}) have been destroyed/replaced.
	 */
	public abstract boolean releventBlocksDestroyed();
	
	/**
	 * @return The center block of the factory. This is different for each type of factory. 
	 * Refer to the class description for more information.
	 */
	public abstract Location getCenter();

}
