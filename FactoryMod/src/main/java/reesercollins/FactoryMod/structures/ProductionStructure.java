package reesercollins.FactoryMod.structures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Order of blocks for construction is as follows: - Crafting Table - Furnace -
 * Chest
 * 
 */
public class ProductionStructure extends MultiBlockStructure {

	private Location craftingTable;
	private Location furnace;
	private Location chest;
	private boolean complete;

	public ProductionStructure(Block center) {
		if (center.getType() == Material.CRAFTING_TABLE) {
			craftingTable = center.getLocation();
			for (Block b : searchForBlocksOnAllSides(center, Material.CHEST)) {
				BlockFace opposite = center.getFace(b).getOppositeFace();
				if (center.getRelative(opposite).getType() == Material.FURNACE) {
					chest = b.getLocation();
					furnace = center.getRelative(opposite).getLocation();
				}
			}
		}

		complete = (chest != null && furnace != null);
	}

	public ProductionStructure(List<Location> blocks) {
		craftingTable = blocks.get(0);
		furnace = blocks.get(1);
		chest = blocks.get(2);
	}

	@Override
	public boolean isValid() {
		return complete;
	}

	@Override
	public void checkIsValid() {
		complete = craftingTable != null && craftingTable.getBlock().getType() == Material.CRAFTING_TABLE
				&& furnace != null && furnace.getBlock().getType() == Material.FURNACE && chest != null
				&& chest.getBlock().getType() == Material.CHEST;
	}

	public Block getCraftingTable() {
		return craftingTable.getBlock();
	}

	public Block getFurnace() {
		return furnace.getBlock();
	}

	public Block getChest() {
		// sometimes a double chest will go across chunk borders and the other
		// half of the chest might be unloaded. To load the other half and the
		// full inventory this is needed to load the chunk
		MultiBlockStructure.searchForBlocksOnAllSides(chest.getBlock(), Material.CHEST);
		return chest.getBlock();
	}

	@Override
	public List<Location> getAllBlocks() {
		LinkedList<Location> result = new LinkedList<Location>();
		result.add(craftingTable);
		result.add(furnace);
		result.add(chest);
		return result;
	}

	@Override
	public List<Block> getReleventBlocks() {
		LinkedList<Block> result = new LinkedList<Block>();
		result.add(getCraftingTable());
		result.add(getFurnace());
		result.add(getChest());
		return result;
	}

	@Override
	public boolean releventBlocksDestroyed() {
		return craftingTable.getBlock().getType() != Material.CRAFTING_TABLE
				&& furnace.getBlock().getType() != Material.FURNACE
				&& chest.getBlock().getType() != Material.CHEST;
	}

	@Override
	public Location getCenter() {
		return craftingTable;
	}

}
