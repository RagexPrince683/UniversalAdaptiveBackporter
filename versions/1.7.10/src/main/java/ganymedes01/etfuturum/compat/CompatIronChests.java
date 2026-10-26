package ganymedes01.etfuturum.compat;

import cpw.mods.ironchest.ChestChangerType;
import cpw.mods.ironchest.IronChest;
import cpw.mods.ironchest.IronChestType;
import cpw.mods.ironchest.ItemChestChanger;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.items.ItemBarrelUpgrade;
import ganymedes01.etfuturum.items.ItemShulkerBoxUpgrade;
import org.apache.commons.lang3.tuple.Pair;
import java.util.LinkedHashMap;
import java.util.IdentityHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import ganymedes01.etfuturum.api.utils.RecipeHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class CompatIronChests {
	private static final Map<String, ItemChestChanger> upgradeItems = new LinkedHashMap<>();
	private static final Map<String, ChestChangerType> upgradeTypes = new LinkedHashMap<>();
	private static final Map<ChestChangerType, Pair<IronChestType, IronChestType>> upgradeMappings = new IdentityHashMap<>();
	private static final Map<String, IronChestType> tiers = new LinkedHashMap<>();
	private static double renderDistance;
	static {
		// Collects all enabled chest upgrade typee
		renderDistance = getFieldOrDefault(IronChest.class, null, "TRANSPARENT_RENDER_INSIDE", Boolean.class, true)
				? getFieldOrDefault(IronChest.class, null, "TRANSPARENT_RENDER_DISTANCE", Double.class, 128D) : 0F;
		for(IronChestType type : IronChestType.values()) {
			if(callOrDefault(type, "isEnabled", Boolean.class, true)) {
				tiers.put(type.name(), type);
			}
		}
		for(ChestChangerType type : ChestChangerType.values()) {
			IronChestType source = getRequiredField(type, "source", IronChestType.class);
			IronChestType target = getRequiredField(type, "target", IronChestType.class);
			boolean isEnabled = isAllowed(type) && tierExists(source.name()) && tierExists(target.name());
			if(isEnabled) {
				upgradeTypes.put(type.name(), type);
				upgradeMappings.put(type, Pair.of(source, target));
			}
		}
	}

	public static boolean upgradeExists(String from, String to) {
		return upgradeTypes.containsKey(from+to);
	}

	public static boolean tierExists(String type) {
		return tiers.containsKey(type);
	}

	public static void init() {
		for(ChestChangerType type : upgradeTypes.values()) {
			ItemChestChanger item = getRequiredField(type, "item", ItemChestChanger.class);
			boolean isEnabled = item != null && item.delegate.name() != null;
			if(isEnabled) {
				upgradeItems.put(type.name(), item);
			}
		}
	}

	public static void registerRecipes() {
		if(ModItems.BARREL_UPGRADE.isEnabled()) {
			ItemBarrelUpgrade upgrade = ((ItemBarrelUpgrade) ModItems.BARREL_UPGRADE.get());
			for (int i = 0; i < upgrade.types.length; i++) {
				Item icUpgrade = upgradeItems.get(upgrade.getSource(i) + upgrade.getTarget(i));
				RecipeHelper.addShapedRecipe(ModItems.BARREL_UPGRADE.newItemStack(1, i), "X", 'X', new ItemStack(icUpgrade));
				RecipeHelper.addShapedRecipe(new ItemStack(icUpgrade), "X", 'X', ModItems.BARREL_UPGRADE.newItemStack(1, i));
			}
		}
		if(ModItems.SHULKER_BOX_UPGRADE.isEnabled()) {
			ItemShulkerBoxUpgrade upgrade = ((ItemShulkerBoxUpgrade) ModItems.SHULKER_BOX_UPGRADE.get());
			for (int i = 0; i < upgrade.types.length; i++) {
				Item icUpgrade = upgradeItems.get(upgrade.getSource(i) + upgrade.getTarget(i));
					RecipeHelper.addShapelessRecipe(RecipeHelper.Priority.NORMAL, ModItems.SHULKER_BOX_UPGRADE.newItemStack(1, i), ModItems.SHULKER_SHELL.newItemStack(), new ItemStack(icUpgrade));
					RecipeHelper.addShapedRecipe(RecipeHelper.Priority.NORMAL, new ItemStack(icUpgrade), "X", 'X', ModItems.SHULKER_BOX_UPGRADE.newItemStack(1, i));
			}
		}
	}

	@Nullable
	public static String getUpgradeName(String from, Item item) {
		if(item instanceof ItemChestChanger changer && changer.getType().canUpgrade(tiers.get(from))) {
			Pair<IronChestType, IronChestType> types = upgradeMappings.get(changer.getType());
			if(types.getLeft().name().equals(from.toUpperCase())) {
				return types.getRight().name();
			}
		}
		return null;
	}

	public static String getNextBarrelUpgrade(String current, ItemStack stack) {
		if(ModItems.BARREL_UPGRADE.isEnabled()) {
			if(stack.getItem() instanceof ItemBarrelUpgrade upgrade && upgrade.getSource(stack.getItemDamage()).equals(current)
					&& upgradeTypes.containsKey(current+upgrade.getTarget(stack.getItemDamage()))) {
				return upgrade.getTarget(stack.getItemDamage());
			}
			return null;
		}
		return CompatIronChests.getUpgradeName(current, stack.getItem());
	}

	public static String getNextShulkerUpgrade(String current, ItemStack stack) {
		if(ModItems.SHULKER_BOX_UPGRADE.isEnabled()) {
			if(stack.getItem() instanceof ItemShulkerBoxUpgrade upgrade && upgrade.getSource(stack.getItemDamage()).equals(current)
					&& upgradeTypes.containsKey(current+upgrade.getTarget(stack.getItemDamage()))) {
				return upgrade.getTarget(stack.getItemDamage());
			}
			return null;
		}
		return CompatIronChests.getUpgradeName(current.replace("VANILLA", "WOOD"), stack.getItem());
	}

	public static boolean enableCrystalRendering() {
		return crystalRenderDistance() > 0;
	}

	public static double crystalRenderDistance() {
		return renderDistance;
	}

	private static boolean isAllowed(ChestChangerType type) {
		try {
			Method method = findMethod(type.getClass(), "isAllowed");
			method.setAccessible(true);
			return Boolean.class.cast(method.invoke(type));
		} catch (NoSuchMethodException e) {
			return getFieldOrDefault(type.getClass(), type, "isAllowed", Boolean.class, true);
		} catch (Exception e) {
			return true;
		}
	}

	private static <T> T callOrDefault(Object receiver, String name, Class<T> cast, T def) {
		try {
			Method method = findMethod(receiver.getClass(), name);
			method.setAccessible(true);
			return cast.cast(method.invoke(receiver));
		} catch (Exception e) {
			return def;
		}
	}

	private static <T> T getFieldOrDefault(Class<?> owner, Object receiver, String name, Class<T> cast, T def) {
		try {
			return getField(owner, receiver, name, cast);
		} catch (Exception e) {
			return def;
		}
	}

	private static <T> T getRequiredField(Object receiver, String name, Class<T> cast) {
		try {
			return getField(receiver.getClass(), receiver, name, cast);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T getField(Class<?> owner, Object receiver, String name, Class<T> cast) throws ReflectiveOperationException {
		Field field = findField(owner, name);
		field.setAccessible(true);
		return cast.cast(field.get(receiver));
	}

	private static Field findField(Class<?> owner, String name) throws NoSuchFieldException {
		for(Class<?> type = owner; type != null; type = type.getSuperclass()) {
			try {
				return type.getDeclaredField(name);
			} catch (NoSuchFieldException ignored) { }
		}
		throw new NoSuchFieldException(name);
	}

	private static Method findMethod(Class<?> owner, String name) throws NoSuchMethodException {
		for(Class<?> type = owner; type != null; type = type.getSuperclass()) {
			try {
				return type.getDeclaredMethod(name);
			} catch (NoSuchMethodException ignored) { }
		}
		throw new NoSuchMethodException(name);
	}
}
