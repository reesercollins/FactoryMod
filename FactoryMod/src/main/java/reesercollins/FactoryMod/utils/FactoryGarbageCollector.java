package reesercollins.FactoryMod.utils;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.factories.Factory;
import reesercollins.FactoryMod.repair.PercentageHealthRepairManager;

public class FactoryGarbageCollector implements Runnable {

	@Override
	public void run() {
		for (Factory f : FMPlugin.getManager().getAllFactories()) {
			if (f.getRepairManager() instanceof PercentageHealthRepairManager) {
				PercentageHealthRepairManager rm = (PercentageHealthRepairManager) f.getRepairManager();
				long graceTime = rm.getGracePeriod();
				long broke = rm.getBreakTime();
				if (broke != 0) {
					if (System.currentTimeMillis() - broke > graceTime) {
						LoggingUtils.log(f.getLogData() + " has been at no health for too long and is being removed");
						FMPlugin.getManager().removeFactory(f);
					}
				} else {
					rm.setHealth(rm.getRawHealth() - rm.getDamageAmountPerDecayInterval());
					if (rm.getRawHealth() <= 0) {
						rm.breakIt();
					}
				}
			}
		}
	}

}
