package reesercollins.FactoryMod;

import org.bukkit.plugin.java.JavaPlugin;

import reesercollins.FactoryMod.interaction.clickable.MenuBuilder;

public class FMPlugin extends JavaPlugin {

	private static FMPlugin plugin;
	private static FactoryManager manager;
	private static MenuBuilder menuBuilder;

	@Override
	public void onEnable() {
		if (plugin == null) {
			plugin = this;
		}
		ConfigParser parser = new ConfigParser(this);
		manager = parser.parse();
		menuBuilder = new MenuBuilder(parser.getDefaultMenuFactory());
		manager.loadFactories();
	}

	@Override
	public void onDisable() {
		manager.shutDown();
	}

	public static FMPlugin getInstance() {
		return plugin;
	}

	public static FactoryManager getManager() {
		return manager;
	}

	public static MenuBuilder getMenuBuilder() {
		return menuBuilder;
	}

	public void info(String msg) {
		getLogger().warning(msg);
	}

	public void warning(String msg) {
		getLogger().warning(msg);
	}

	public void error(String msg, boolean isFatal) {
		getLogger().severe(msg);
		if (isFatal)
			getServer().getPluginManager().disablePlugin(this);
	}

}
