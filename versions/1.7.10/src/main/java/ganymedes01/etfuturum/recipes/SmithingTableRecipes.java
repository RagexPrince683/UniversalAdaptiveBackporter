package ganymedes01.etfuturum.recipes;

import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SmithingTableRecipes {

	private static Object upgradeTemplate() { return ganymedes01.etfuturum.configuration.ContentAvailability.enabled("template_smithing") ? ModItems.NETHERITE_UPGRADE_SMITHING_TEMPLATE.newItemStack() : null; }

	private static final SmithingTableRecipes instance = new SmithingTableRecipes();
	private final List<SmithingTableRecipe> recipes = new ArrayList<>();

	public static SmithingTableRecipes getInstance() {
		return instance;
	}

	public static void init() {
		if (ConfigBlocksItems.enableNetherite) {
			SmithingTableRecipes.getInstance().addUpgrade(new ItemStack(ModItems.NETHERITE_PICKAXE.get()), "ingotNetherite", new ItemStack(Items.diamond_pickaxe, 1, OreDictionary.WILDCARD_VALUE));
			SmithingTableRecipes.getInstance().addUpgrade(new ItemStack(ModItems.NETHERITE_AXE.get()), "ingotNetherite", new ItemStack(Items.diamond_axe, 1, OreDictionary.WILDCARD_VALUE));
			SmithingTableRecipes.getInstance().addUpgrade(new ItemStack(ModItems.NETHERITE_SPADE.get()), "ingotNetherite", new ItemStack(Items.diamond_shovel, 1, OreDictionary.WILDCARD_VALUE));
			SmithingTableRecipes.getInstance().addUpgrade(ModItems.NETHERITE_SPEAR.newItemStack(), "ingotNetherite", new ItemStack(ModItems.DIAMOND_SPEAR.get(), 1, OreDictionary.WILDCARD_VALUE));
			SmithingTableRecipes.getInstance().addUpgrade(ModItems.NAUTILUS_ARMOR.newItemStack(1, 4), "ingotNetherite", ModItems.NAUTILUS_ARMOR.newItemStack(1, 3));
			SmithingTableRecipes.getInstance().addUpgrade(new ItemStack(ModItems.NETHERITE_SWORD.get()), "ingotNetherite", new ItemStack(Items.diamond_sword, 1, OreDictionary.WILDCARD_VALUE));
			SmithingTableRecipes.getInstance().addUpgrade(new ItemStack(ModItems.NETHERITE_HOE.get()), "ingotNetherite", new ItemStack(Items.diamond_hoe, 1, OreDictionary.WILDCARD_VALUE));

			SmithingTableRecipes.getInstance().addUpgrade(new ItemStack(ModItems.NETHERITE_HELMET.get()), "ingotNetherite", new ItemStack(Items.diamond_helmet, 1, OreDictionary.WILDCARD_VALUE));
			SmithingTableRecipes.getInstance().addUpgrade(new ItemStack(ModItems.NETHERITE_CHESTPLATE.get()), "ingotNetherite", new ItemStack(Items.diamond_chestplate, 1, OreDictionary.WILDCARD_VALUE));
			SmithingTableRecipes.getInstance().addUpgrade(new ItemStack(ModItems.NETHERITE_LEGGINGS.get()), "ingotNetherite", new ItemStack(Items.diamond_leggings, 1, OreDictionary.WILDCARD_VALUE));
			SmithingTableRecipes.getInstance().addUpgrade(new ItemStack(ModItems.NETHERITE_BOOTS.get()), "ingotNetherite", new ItemStack(Items.diamond_boots, 1, OreDictionary.WILDCARD_VALUE));
		}
	}

	private void addUpgrade(ItemStack result, Object material, Object base) { recipes.add(new SmithingTableRecipe(base, material, result, true, upgradeTemplate())); }

	public void addRecipe(ItemStack result, Object materialSlot, Object toolSlot) {
		instance.recipes.add(new SmithingTableRecipe(toolSlot, materialSlot, result, true));
	}

	public void addRecipeNoNBT(ItemStack result, Object materialSlot, Object toolSlot) {
		instance.recipes.add(new SmithingTableRecipe(toolSlot, materialSlot, result, false));
	}

	public void addRecipe(SmithingTableRecipe recipe) {
		instance.recipes.add(recipe);
	}

	public List<SmithingTableRecipe> getRecipes() {
		return recipes;
	}
	public boolean isBase(ItemStack stack) {
		if (stack != null && stack.getItem() instanceof net.minecraft.item.ItemArmor) return true;
		for (SmithingTableRecipe recipe : recipes) if (ingredientMatches(recipe.input, stack)) return true;
		return false;
	}
	public boolean isMaterial(ItemStack stack) {
		if (ArmorTrims.material(stack) != null) return true;
		for (SmithingTableRecipe recipe : recipes) if (ingredientMatches(recipe.material, stack)) return true;
		return false;
	}
	public static boolean ingredientMatches(Object ingredient, ItemStack stack) {
		if (ingredient == null) return stack == null;
		if (ingredient instanceof ItemStack) return OreDictionary.itemMatches((ItemStack) ingredient, stack, false);
		if (ingredient instanceof List<?>) for (Object candidate : (List<?>) ingredient) if (candidate instanceof ItemStack && OreDictionary.itemMatches((ItemStack) candidate, stack, false)) return true;
		return false;
	}

	public ItemStack findMatchingRecipe(InventoryCrafting p_82787_1_, World p_82787_2_) {
		if (p_82787_1_.getSizeInventory() >= 3) {
			ItemStack trim = ArmorTrims.apply(p_82787_1_.getStackInSlot(2), p_82787_1_.getStackInSlot(0), p_82787_1_.getStackInSlot(1));
			if (trim != null) return trim;
		}
		for (SmithingTableRecipe recipe : this.recipes) {
			if (recipe.matches(p_82787_1_, p_82787_2_)) {
				return recipe.getCraftingResult(p_82787_1_);
			}
		}

		return null;
	}

	public static class SmithingTableRecipe implements IRecipe {
		/**
		 * Should we copy the NBT values from the tool slot?
		 */
		private final boolean copyNBT;
		private final Object input;
		private final Object material;
		private final ItemStack output;
		private final Object template;
		public Object getTemplate() { return template; }

		public SmithingTableRecipe(Object toolSlot, Object materialSlot, ItemStack result, boolean copy) {
			this(toolSlot, materialSlot, result, copy, null);
		}

		public SmithingTableRecipe(Object toolSlot, Object materialSlot, ItemStack result, boolean copy, Object template) {
			this.template = template;
			copyNBT = copy;

			output = result;

			Object[] recipe = new Object[]{toolSlot, materialSlot};

			for (int idx = 0; idx < 2; idx++) {
				Object in = recipe[idx];

				if (in instanceof ItemStack) {
					recipe[idx] = ((ItemStack) in).copy();
				} else if (in instanceof Item) {
					recipe[idx] = new ItemStack((Item) in);
				} else if (in instanceof Block) {
					recipe[idx] = new ItemStack((Block) in, 1, OreDictionary.WILDCARD_VALUE);
				} else if (in instanceof String) {
					recipe[idx] = OreDictionary.getOres((String) in);
				} else {
					String ret = "Invalid smithing table recipe: ";
					for (Object tmp : recipe) {
						ret += tmp + ", ";
					}
					ret += output;
					throw new RuntimeException(ret);
				}
			}

			input = recipe[0];
			material = recipe[1];
		}

		//TODO: Check if vanilla copies the whole tag compound or just the name and enchantments.
		@Override
		public ItemStack getCraftingResult(InventoryCrafting var1) {
			ItemStack stack = output.copy();
			if (copyNBT) {
				ItemStack toCopy = var1.getStackInSlot(0);
				if (toCopy.hasTagCompound()) {
					stack.setTagCompound((net.minecraft.nbt.NBTTagCompound) toCopy.getTagCompound().copy());
				}
				if (toCopy.isItemStackDamageable() && stack.isItemStackDamageable()) {
					stack.setItemDamage(toCopy.getItemDamage());
				}
			}
			return stack;
		}

		/**
		 * Returns the size of the recipe area
		 */
		@Override
		public int getRecipeSize() {
			return template == null ? 2 : 3;
		}

		@Override
		public ItemStack getRecipeOutput() {
			return output;
		}

		public Object getInputItem() {
			return input;
		}

		public Object getMaterial() {
			return material;
		}

		/**
		 * Used to check if a recipe matches current crafting inventory
		 */
		@Override
		public boolean matches(InventoryCrafting inv, World world) {
			if (inv.getSizeInventory() < 2 || inv.getSizeInventory() < 3 && template != null) return false;
			Object[] recipe = inv.getSizeInventory() >= 3 ? new Object[]{input, material, template} : new Object[]{input, material};
			for (int x = 0; x < recipe.length; x++) {
				Object target;

				target = recipe[x];

				ItemStack slot = inv.getStackInSlot(x);

				if (target instanceof ItemStack) {
					if (!OreDictionary.itemMatches((ItemStack) target, slot, false)) {
						return false;
					}
				} else if (target instanceof ArrayList) {
					boolean matched = false;

					@SuppressWarnings("unchecked")
					Iterator<ItemStack> itr = ((ArrayList<ItemStack>) target).iterator();
					while (itr.hasNext() && !matched) {
						matched = OreDictionary.itemMatches(itr.next(), slot, false);
					}

					if (!matched) {
						return false;
					}
				} else if (target == null && slot != null) {
					return false;
				}
			}

			return true;
		}
	}
}
