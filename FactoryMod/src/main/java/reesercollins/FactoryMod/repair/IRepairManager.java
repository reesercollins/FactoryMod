package reesercollins.FactoryMod.repair;

public interface IRepairManager {

	/**
	 * Sets the health to 0 and makes the factory unusable
	 */
	public void breakIt();

	/**
	 * @return How much health the factory represented currently has as a nice
	 *         string for output messages
	 */
	public String getHealth();

	/**
	 * @return Whether the factory represented is currently at full health
	 */
	public boolean atFullHealth();

	/**
	 * @return Whether the factory is in disrepair currently
	 */
	public boolean inDisrepair();

}
