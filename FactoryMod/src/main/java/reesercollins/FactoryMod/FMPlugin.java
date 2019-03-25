package reesercollins.FactoryMod;

import org.bukkit.plugin.java.JavaPlugin;

public class FMPlugin extends JavaPlugin {
	
	private static FMPlugin plugin;
	private static FactoryManager manager;
	
	@Override
	public void onEnable() {
		if (plugin == null) {
			plugin = this;
		}
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public static FMPlugin getInstance() {
		return plugin;
	}
	
	public static FactoryManager getManager() {
		return manager;
	}
	
	public void info(String msg) {
		getLogger().warning(msg);
	}
	
	public void warning(String msg) {
		getLogger().warning(msg);
	}
	
	public void error(String msg, boolean isFatal) {
		getLogger().severe(msg);
		if (isFatal) getServer().getPluginManager().disablePlugin(this);
	}

}
