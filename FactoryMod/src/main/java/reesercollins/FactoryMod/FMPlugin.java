package reesercollins.FactoryMod;

import org.bukkit.plugin.java.JavaPlugin;

import reesercollins.FactoryMod.interaction.clickable.MenuBuilder;
import reesercollins.FactoryMod.listeners.block.BlockBreakListener;
import reesercollins.FactoryMod.listeners.block.BlockBurnListener;
import reesercollins.FactoryMod.listeners.block.BlockRedstoneListener;
import reesercollins.FactoryMod.listeners.entity.EntityExplodeListener;
import reesercollins.FactoryMod.listeners.inventory.InventoryClickListener;
import reesercollins.FactoryMod.listeners.inventory.InventoryCloseListener;
import reesercollins.FactoryMod.listeners.player.PlayerInteractListener;
import reesercollins.FactoryMod.listeners.player.PlayerQuitListener;

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
		registerListeners();
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
	
	private void registerListeners() {
		plugin.getServer().getPluginManager().registerEvents(new BlockBreakListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new BlockBurnListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new BlockRedstoneListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new EntityExplodeListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new InventoryClickListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new InventoryCloseListener(), plugin);
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
