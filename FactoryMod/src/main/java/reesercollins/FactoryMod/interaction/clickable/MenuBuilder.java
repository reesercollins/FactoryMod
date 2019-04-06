package reesercollins.FactoryMod.interaction.clickable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import reesercollins.FactoryMod.FMPlugin;
import reesercollins.FactoryMod.FactoryManager;
import reesercollins.FactoryMod.builders.IFactoryBuilder;
import reesercollins.FactoryMod.builders.ProductionBuilder;
import reesercollins.FactoryMod.itemHandling.ItemMap;
import reesercollins.FactoryMod.recipes.IRecipe;
import reesercollins.FactoryMod.recipes.InputRecipe;
import reesercollins.FactoryMod.recipes.UpgradeRecipe;
import reesercollins.FactoryMod.structures.ProductionStructure;

public class MenuBuilder {

	private FactoryManager manager;
	private Map<UUID, String> factoryViewed = new HashMap<UUID, String>();
	// child is key, parent is value
	private Map<String, String> parentFactories = new HashMap<String, String>();
	private DecorationStack input;
	private IFactoryBuilder defaultMenu;

	public MenuBuilder(String defaultFactory) {
		manager = FMPlugin.getManager();
		for (IFactoryBuilder builder : manager.getAllBuilders().values()) {
			if (builder instanceof ProductionBuilder) {
				ProductionBuilder prodBuilder = (ProductionBuilder) builder;
				for (IRecipe rec : prodBuilder.getRecipes()) {
					if (rec instanceof UpgradeRecipe) {
						parentFactories.put(((UpgradeRecipe) rec).getBuilder().getName(), builder.getName());
					}
				}
			}
		}
		ItemStack inp = new ItemStack(Material.PAPER);
		ItemMeta inpMeta = inp.getItemMeta();
		inpMeta.setDisplayName("Input");
		inpMeta.setLore(new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "The items below are required")));
		inp.setItemMeta(inpMeta);
		input = new DecorationStack(inp);

		ItemStack out = new ItemStack(Material.PAPER);
		ItemMeta outMeta = out.getItemMeta();
		outMeta.setDisplayName("Output");
		outMeta.setLore(new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "The output of this recipe")));
		out.setItemMeta(outMeta);

		if (defaultFactory != null) {
			defaultMenu = manager.getBuilder(defaultFactory);
		}
	}

	public void openFactoryBrowser(Player p, String startingFactory) {
		IFactoryBuilder builder;
		if (startingFactory == null) {
			builder = defaultMenu;
			if (builder == null) {
				builder = manager.getAllBuilders().values().iterator().next();
			}
		} else {
			builder = manager.getBuilder(startingFactory);
		}
		if (builder == null) {
			String comp = startingFactory.toLowerCase();
			for (Entry<String, IFactoryBuilder> entry : manager.getAllBuilders().entrySet()) {
				if (entry.getKey().toLowerCase().equals(comp)) {
					builder = entry.getValue();
					break;
				}
			}
			if (builder == null) {
				FMPlugin.getInstance().warning("There is no factory with name " + comp);
				p.sendMessage(ChatColor.RED + "There is no factory with the name you entered");
				return;
			}
		}

		if (builder instanceof ProductionBuilder) {
			ProductionBuilder prodBuilder = (ProductionBuilder) builder;
			factoryViewed.put(p.getUniqueId(), builder.getName());
			ClickableInventory browser = new ClickableInventory(InventoryType.CHEST, prodBuilder.getName());

			ItemStack creationStack = new ItemStack(Material.CHEST);
			ItemMeta creationMeta = creationStack.getItemMeta();
			creationMeta.setDisplayName("Setup");
			creationMeta.setLore(
					new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to display more information",
							ChatColor.LIGHT_PURPLE + "on how to setup this factory")));
			creationStack.setItemMeta(creationMeta);

			Clickable creationClickable = new Clickable(creationStack) {
				@Override
				public void clicked(Player p) {
					openSetupBrowser(p, factoryViewed.get(p.getUniqueId()));
				}
			};
			browser.setSlot(creationClickable, 10);

			ItemStack recipeStack = new ItemStack(Material.CRAFTING_TABLE);
			ItemMeta recipeMeta = recipeStack.getItemMeta();
			recipeMeta.setDisplayName("Recipies");
			recipeMeta.setLore(
					new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to display all recipes",
							ChatColor.LIGHT_PURPLE + "this factory can run")));
			recipeStack.setItemMeta(recipeMeta);

			Clickable recipeClickable = new Clickable(recipeStack) {
				@Override
				public void clicked(Player p) {
					openRecipeBrowser(p, factoryViewed.get(p.getUniqueId()));
				}
			};
			browser.setSlot(recipeClickable, 13);

			ItemStack upgradeStack = new ItemStack(Material.FURNACE);
			ItemMeta upgradeMeta = upgradeStack.getItemMeta();
			upgradeMeta.setDisplayName("Upgrades");
			upgradeMeta.setLore(new ArrayList<String>(
					Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to display more information about",
							ChatColor.LIGHT_PURPLE + "the possible upgrades to this factory")));
			upgradeStack.setItemMeta(upgradeMeta);

			Clickable upgradeClickable = new Clickable(upgradeStack) {
				@Override
				public void clicked(Player p) {
					openUpgradeBrowser(p, factoryViewed.get(p.getUniqueId()));
				}
			};
			browser.setSlot(upgradeClickable, 16);
			browser.showInventory(p);
		}
	}

	private void openRecipeBrowser(Player p, String facName) {
		ProductionBuilder builder = (ProductionBuilder) manager.getBuilder(facName);
		List<IRecipe> recipes = builder.getRecipes();
		int size = (recipes.size() / 9) + 2;
		if ((recipes.size() % 9) == 0) {
			size--;
		}
		size *= 9;

		ClickableInventory recipeInv = new ClickableInventory(size, "Recipe for " + facName);

		int j = 0;
		for (int i = 0; i < recipes.size(); i++) {
			if (recipes.get(i) == null) {
				continue;
			}
			InputRecipe rec = ((InputRecipe) recipes.get(i));
			ItemStack is = rec.getRecipeRepresentation();
			Clickable c = new Clickable(is) {
				@Override
				public void clicked(Player p) {
					openDetailedRecipeBrowser(p, this.getItemStack().getItemMeta().getDisplayName());
				}
			};
			recipeInv.setSlot(c, j++);
		}

		ItemStack backStack = new ItemStack(Material.ARROW);
		ItemMeta backMeta = backStack.getItemMeta();
		backMeta.setDisplayName("Back to factory overview");
		backMeta.setLore(new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to go back")));
		backStack.setItemMeta(backMeta);
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player p) {
				openFactoryBrowser(p, factoryViewed.get(p.getUniqueId()));
			}
		};
		recipeInv.setSlot(backClickable, size - 5);
		recipeInv.showInventory(p);
	}

	private void openSetupBrowser(Player p, String facName) {
		ProductionBuilder builder = (ProductionBuilder) manager.getBuilder(facName);
		ProductionBuilder parentBuilder = (ProductionBuilder) manager.getBuilder(parentFactories.get(facName));
		ClickableInventory ci = new ClickableInventory(54, "Create a " + builder.getName()); // Bukkit has 32 char limit
																								// on
																								// inventory
		ItemStack cr = new ItemStack(Material.CRAFTING_TABLE);
		ItemMeta crMeta = cr.getItemMeta();
		ItemStack fur = new ItemStack(Material.FURNACE);
		ItemMeta furMeta = fur.getItemMeta();
		ItemStack che = new ItemStack(Material.CHEST);
		ItemMeta cheMeta = che.getItemMeta();
		if (parentBuilder == null) {// creation factory
			crMeta.setLore(
					new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "This factory can be created with",
							ChatColor.LIGHT_PURPLE + "a normal crafting table, furnace and chest")));
			cheMeta.setLore(
					new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Arrange the 3 blocks like this,",
							ChatColor.LIGHT_PURPLE + "put the materials below in the chest",
							ChatColor.LIGHT_PURPLE + "and hit the crafting table with a stick")));
			cr.setItemMeta(crMeta);
			che.setItemMeta(cheMeta);
			DecorationStack furnDec = new DecorationStack(fur);
			DecorationStack chestDec = new DecorationStack(che);
			DecorationStack craStack = new DecorationStack(cr);
			ci.setSlot(furnDec, 3);
			ci.setSlot(craStack, 4);
			ci.setSlot(chestDec, 5);
			ItemMap im = manager.getSetupCost(ProductionStructure.class, builder.getName());
			int slot = 0;
			for (ItemStack is : im.getItemStackRepresentation()) {
				DecorationStack dec = new DecorationStack(is);
				ci.setSlot(dec, slot++);
			}
		} else {
			UpgradeRecipe rec = null;
			for (IRecipe parentRec : parentBuilder.getRecipes()) {
				if (parentRec instanceof UpgradeRecipe && ((UpgradeRecipe) parentRec).getBuilder().equals(builder)) {
					rec = (UpgradeRecipe) parentRec;
				}
			}

			crMeta.setLore(new ArrayList<String>(
					Arrays.asList(ChatColor.LIGHT_PURPLE + "Upgrade from a " + parentBuilder.getName())));
			cr.setItemMeta(crMeta);
			Clickable craCli = new Clickable(cr) {
				@Override
				public void clicked(Player arg0) {
					openFactoryBrowser(arg0, parentFactories.get(factoryViewed.get(arg0.getUniqueId())));
				}
			};
			ci.setSlot(craCli, 4);

			furMeta.setLore(new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to display information",
					ChatColor.LIGHT_PURPLE + "on this factory")));
			fur.setItemMeta(furMeta);
			Clickable furCli = new Clickable(fur) {
				@Override
				public void clicked(Player arg0) {
					openFactoryBrowser(arg0, parentFactories.get(factoryViewed.get(arg0.getUniqueId())));
				}
			};
			ci.setSlot(furCli, 3);

			Clickable cheCli = new Clickable(che) {
				@Override
				public void clicked(Player arg0) {
					openFactoryBrowser(arg0, parentFactories.get(factoryViewed.get(arg0.getUniqueId())));
				}
			};
			ci.setSlot(cheCli, 5);

			int slot = 0;
			List<ItemStack> itms = rec.getInput().getItemStackRepresentation();
			if (itms.size() > 27) {
				itms = rec.getInput().getLoredItemCountRepresentation();
			}

			for (ItemStack is : itms) {
				DecorationStack dec = new DecorationStack(is);
				ci.setSlot(dec, slot++);
			}
		}
		ci.setSlot(input, 22);
		ItemStack backStack = new ItemStack(Material.ARROW);
		ItemMeta backMeta = backStack.getItemMeta();
		backMeta.setDisplayName("Back to factory overview");
		backMeta.setLore(new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to go back")));
		backStack.setItemMeta(backMeta);
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player arg0) {
				openFactoryBrowser(arg0, factoryViewed.get(arg0.getUniqueId()));
			}
		};
		ci.setSlot(backClickable, 18);
		ci.showInventory(p);
	}

	private void openUpgradeBrowser(Player p, String facName) {
		ProductionBuilder builder = (ProductionBuilder) manager.getBuilder(factoryViewed.get(p.getUniqueId()));
		List<IRecipe> upgrades = new LinkedList<IRecipe>();
		for (IRecipe recipe : builder.getRecipes()) {
			if (recipe instanceof UpgradeRecipe) {
				upgrades.add(recipe);
			}
		}

		ClickableInventory ci = new ClickableInventory(Math.max(18, (upgrades.size() / 9) * 9), "Possible upgrades");
		if (upgrades.size() == 0) {
			ItemStack bar = new ItemStack(Material.BARRIER);
			ItemMeta barMeta = bar.getItemMeta();
			barMeta.setDisplayName("No upgrades available");
			barMeta.setLore(new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to go back")));
			bar.setItemMeta(barMeta);
			Clickable noUpgrades = new Clickable(bar) {
				@Override
				public void clicked(Player p) {
					openFactoryBrowser(p, factoryViewed.get(p.getUniqueId()));
				}
			};
			ci.setSlot(noUpgrades, 4);
		} else {
			for (IRecipe recipe : upgrades) {
				ItemStack recStack = ((InputRecipe) recipe).getRecipeRepresentation();
				ItemMeta recMeta = recStack.getItemMeta();
				recMeta.setLore(new ArrayList<String>(
						Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to display more information")));
				recStack.setItemMeta(recMeta);
				Clickable c = new Clickable(((InputRecipe) recipe).getRecipeRepresentation()) {
					@Override
					public void clicked(Player p) {
						openDetailedRecipeBrowser(p, this.getItemStack().getItemMeta().getDisplayName());
					}
				};
				ci.addSlot(c);
			}
		}
		ItemStack backStack = new ItemStack(Material.ARROW);
		ItemMeta backMeta = backStack.getItemMeta();
		backMeta.setDisplayName("Back to factory overview");
		backMeta.setLore(new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to go back")));
		backStack.setItemMeta(backMeta);
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player p) {
				openFactoryBrowser(p, factoryViewed.get(p.getUniqueId()));
			}
		};
		ci.setSlot(backClickable, 17);
		ci.showInventory(p);
	}

	private void openDetailedRecipeBrowser(Player p, String recipeName) {
		if (recipeName == null) {
			FMPlugin.getInstance().warning("Recipe name cannot be null in openDetailedRecipeBrowser calls");
			return;
		}
		ProductionBuilder builder = (ProductionBuilder) manager.getBuilder(factoryViewed.get(p.getUniqueId()));
		InputRecipe rec = null;
		for (IRecipe recipe : builder.getRecipes()) {
			if (recipe == null || recipe.getName() == null) {
				FMPlugin.getInstance().warning("Null recipe or recipe name registered with " + builder.getName());
				continue;
			}
			if (recipeName.equals(recipe.getName())) {
				rec = (InputRecipe) recipe;
				break;
			}
		}
		if (rec == null) {
			FMPlugin.getInstance().warning("There is no recipe with name " + recipeName);
			p.sendMessage(ChatColor.RED + "There is no recipe that matches " + recipeName);
			return;
		}
		ClickableInventory ci = new ClickableInventory(54, recipeName);
		ItemStack inputStack = new ItemStack(Material.PAPER);
		ItemMeta inputMeta = inputStack.getItemMeta();
		inputMeta.setDisplayName("Input materials");
		inputMeta.setLore(new ArrayList<String>(
				Arrays.asList(ChatColor.LIGHT_PURPLE + "The materials required to run this recipe")));
		inputStack.setItemMeta(inputMeta);
		DecorationStack inputClickable = new DecorationStack(inputStack);
		ci.setSlot(inputClickable, 4);
		int index = 0;
		List<ItemStack> ins = rec.getInputRepresentation(null, null);
		if (ins.size() > 18) {
			ins = new ItemMap(ins).getLoredItemCountRepresentation();
		}
		for (ItemStack is : ins) {
			Clickable c = new DecorationStack(is);
			ci.setSlot(c, index++);
		}

		ItemStack outputStack = new ItemStack(Material.PAPER);
		ItemMeta outputMeta = outputStack.getItemMeta();
		outputMeta.setDisplayName("Output/effect");
		outputStack.setItemMeta(outputMeta);
		DecorationStack outputClickable = new DecorationStack(outputStack);

		ItemStack backStack = new ItemStack(Material.ARROW);
		ItemMeta backMeta = backStack.getItemMeta();
		backMeta.setDisplayName("Back to recipe overview");
		backMeta.setLore(new ArrayList<String>(Arrays.asList(ChatColor.LIGHT_PURPLE + "Click to go back")));
		backStack.setItemMeta(backMeta);
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player p) {
				openRecipeBrowser(p, factoryViewed.get(p.getUniqueId()));
			}
		};
		ci.setSlot(backClickable, 27);

		ci.setSlot(outputClickable, 31);
		index = 27;
		List<ItemStack> out = rec.getOutputRepresentation(null, null);
		if (out.size() > 18) {
			out = new ItemMap(out).getLoredItemCountRepresentation();
		}
		for (ItemStack is : out) {
			Clickable c;
			if (rec instanceof UpgradeRecipe) {
				c = new Clickable(is) {
					@Override
					public void clicked(Player p) {
						IFactoryBuilder builder = manager.getBuilder(factoryViewed.get(p.getUniqueId()));
						for (IRecipe re : ((ProductionBuilder) builder).getRecipes()) {
							if (re instanceof UpgradeRecipe && ((UpgradeRecipe) re).getBuilder().getName()
									.equals(this.getItemStack().getItemMeta().getDisplayName())) {
								openFactoryBrowser(p, ((UpgradeRecipe) re).getBuilder().getName());
								break;
							}
						}
					}
				};
			} else {
				c = new DecorationStack(is);
			}
			ci.setSlot(c, index++);
		}
		int fuelInterval = rec.getFuelConsumptionInterval() != -1 ? rec.getFuelConsumptionInterval()
				: builder.getFuelConsumptionInterval();
		int fuelConsumed = rec.getProductionTime() / fuelInterval;
		ItemStack fuels = builder.getFuel().clone();
		fuels.setAmount(fuelConsumed);
		ItemStack fuelStack;
		if (fuelConsumed > fuels.getType().getMaxStackSize()) {
			fuelStack = new ItemMap(fuels).getLoredItemCountRepresentation().get(0);
		} else {
			fuelStack = fuels;
		}
		ItemMeta fuelMeta = fuelStack.getItemMeta();
		fuelMeta.setLore(new ArrayList<String>(Arrays
				.asList(ChatColor.LIGHT_PURPLE + "Total duration of " + rec.getProductionTime() / 20 + " seconds")));
		ci.setSlot(new DecorationStack(fuelStack), 30);
		ci.showInventory(p);
	}
}
