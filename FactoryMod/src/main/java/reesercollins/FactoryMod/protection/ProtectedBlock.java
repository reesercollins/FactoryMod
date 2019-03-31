package reesercollins.FactoryMod.protection;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.factories.Factory.FactoryType;

public class ProtectedBlock {

	private Block block;
	private Class<? extends Factory> factoryClass;
	private FactoryType factoryType;
	private HashMap<OfflinePlayer, List<FactoryPermission>> permissions;

	public ProtectedBlock(Block block) {
		this.block = block;
		Factory fac = FMPlugin.getManager().getFactoryAt(block);
		if (fac != null) {
			this.factoryClass = fac.getClass();
			this.factoryType = fac.getType();
		}
	}

	public ProtectedBlock(Location location) {
		this(location.getBlock());
	}

	public ProtectedBlock(Block block, FactoryType type) {
		this.block = block;
		this.factoryType = type;
	}

	/**
	 * @return The block that this object represents
	 */
	public Block getBlock() {
		return block;
	}

	/**
	 * @return The factory class that this block is a part of.
	 */
	public Class<? extends Factory> getFactoryClass() {
		return factoryClass;
	}

	/**
	 * @return The result of the getType() function on the factory this block
	 *         represents.
	 */
	public FactoryType getFactoryType() {
		return factoryType;
	}

	public HashMap<OfflinePlayer, List<FactoryPermission>> getPermissions() {
		return permissions;
	}

	/**
	 * This method is used for determining if a given player has the given
	 * permission on this protected block
	 * 
	 * @param player     The player being queried
	 * @param permission The permission being searched for
	 * @return True if the player has permission, false if not
	 */
	public boolean playerHasPermission(OfflinePlayer player, FactoryPermission permission) {
		List<FactoryPermission> perms = permissions.get(player);
		if (perms == null) {
			return false;
		}
		return perms.contains(permission);
	}

	/**
	 * This method adds the given permission to the provided player. NOTE: Factory
	 * permissions only need to be added to the center block. Other blocks in the
	 * MultiBlockStructure will inherit its permissions
	 * 
	 * @param player     The player to add the permission to
	 * @param permission The permission to be added
	 */
	public void addPermissionToPlayer(OfflinePlayer player, FactoryPermission permission) {
		List<FactoryPermission> perms = permissions.get(player);
		if (perms == null || perms.contains(permission)) {
			return;
		}
		permissions.get(player).add(permission);
	}

	/**
	 * This method removes the given permission from the provided player. NOTE:
	 * Factory permissions only need to be removed from the center block. Other
	 * blocks in the MultiBlockStructure will inherit its permissions
	 * 
	 * @param player     The player to remove the permission from
	 * @param permission The permission to be removed
	 */
	public void removePermissionFromPlayer(OfflinePlayer player, FactoryPermission permission) {
		List<FactoryPermission> perms = permissions.get(player);
		if (perms == null || !perms.contains(permission)) {
			return;
		}
		permissions.get(player).remove(permission);
	}

}
