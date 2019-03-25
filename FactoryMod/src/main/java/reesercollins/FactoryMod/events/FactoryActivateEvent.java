package reesercollins.FactoryMod.events;

import org.bukkit.entity.Player;

import reesercollins.FactoryMod.factories.Factory;

public class FactoryActivateEvent extends FactoryModEvent {

	private Factory fac;
	private Player activator;

	public FactoryActivateEvent(Factory fac, Player activator) {
		this.fac = fac;
		this.activator = activator;
	}

	/**
	 * @return The factory being activated
	 */
	public Factory getFactory() {
		return fac;
	}

	/**
	 * @return The player activating the factory or null if it was not activated by
	 *         a player
	 */
	public Player getActivator() {
		return activator;
	}

}
