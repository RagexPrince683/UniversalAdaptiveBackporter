package ganymedes01.etfuturum.world.structure.underground;

import com.google.gson.*;
import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.ModEnchantments;
import ganymedes01.etfuturum.world.LootEnchantmentOptions;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Released weighted tables with native items/functions. Unsupported content is explicitly deferred, never substituted. */
public final class UndergroundLoot {
	private static final Map<String, JsonObject> TABLES = new HashMap<>();
	private static final Set<String> DEFERRED = new HashSet<>();
	private UndergroundLoot() { }
	private static JsonObject table(String id) {
		return TABLES.computeIfAbsent(id, key -> {
			try (InputStream in = UndergroundLoot.class.getResourceAsStream("/data/loot/" + key.replace("minecraft:", "") + ".json")) {
				if (in == null) throw new IllegalArgumentException("Missing loot table " + key);
				return new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			} catch (IOException e) { throw new IllegalStateException(e); }
		});
	}
	public static List<ItemStack> roll(String id, Random random) { List<ItemStack> result = new ArrayList<>(); roll(id, random, result, 0); return result; }
	private static void roll(String id, Random random, List<ItemStack> result, int depth) {
		if (depth > 8) throw new IllegalStateException("Recursive loot table " + id);
		for (JsonElement value : table(id).getAsJsonArray("pools")) {
			JsonObject pool = value.getAsJsonObject(); if (!conditions(pool, random)) continue;
			JsonArray entries = pool.getAsJsonArray("entries"); int total = 0;
			for (JsonElement entry : entries) total += weight(entry.getAsJsonObject());
			int rolls = (int) number(pool.get("rolls"), random, 1);
			for (int i = 0; i < rolls && total > 0; i++) {
				int pick = random.nextInt(total); JsonObject chosen = null;
				for (JsonElement entry : entries) { pick -= weight(entry.getAsJsonObject()); if (pick < 0) { chosen = entry.getAsJsonObject(); break; } }
				if (chosen == null || !conditions(chosen, random)) continue;
				String type = chosen.get("type").getAsString();
				if (type.equals("minecraft:loot_table")) { roll(chosen.get("value").getAsString(), random, result, depth + 1); continue; }
				if (!type.equals("minecraft:item")) continue;
				ItemStack stack = item(chosen.get("name").getAsString()); if (stack == null) continue;
				if (chosen.has("functions")) for (JsonElement function : chosen.getAsJsonArray("functions")) { if (stack == null) break; stack = function(stack, function.getAsJsonObject(), random); }
				if (stack != null && stack.stackSize > 0) result.add(stack);
			}
		}
	}
	private static int weight(JsonObject entry) { return entry.has("weight") ? entry.get("weight").getAsInt() : 1; }
	private static boolean conditions(JsonObject object, Random random) {
		if (object.has("conditions")) for (JsonElement value : object.getAsJsonArray("conditions")) {
			JsonObject c = value.getAsJsonObject(); if (!c.get("condition").getAsString().equals("minecraft:random_chance")) throw new IllegalArgumentException("Unsupported loot condition " + c);
			if (random.nextFloat() >= c.get("chance").getAsFloat()) return false;
		}
		return true;
	}
	private static float number(JsonElement value, Random random, float fallback) {
		if (value == null) return fallback; if (value.isJsonPrimitive()) return value.getAsFloat();
		JsonObject range = value.getAsJsonObject(); float min = range.get("min").getAsFloat(), max = range.get("max").getAsFloat();
		return min + random.nextFloat() * (max - min + (min == (int) min && max == (int) max ? 1 : 0));
	}
	private static ItemStack function(ItemStack stack, JsonObject f, Random random) {
		String name = f.get("function").getAsString();
		if (name.equals("minecraft:exploration_map")) { deferred("exploration map markers"); return null; }
		if (name.equals("minecraft:set_count")) stack.stackSize = Math.min(stack.getMaxStackSize(), (int) number(f.get("count"), random, 1));
		else if (name.equals("minecraft:set_damage")) stack.setItemDamage((int) ((1 - Math.min(1, number(f.get("damage"), random, 1))) * stack.getMaxDamage()));
		else if (name.equals("minecraft:enchant_with_levels")) stack = EnchantmentHelper.addRandomEnchantment(random, stack, (int) number(f.get("levels"), random, 20));
		else if (name.equals("minecraft:enchant_randomly")) stack = randomEnchantment(stack, f, random);
		else if (name.equals("minecraft:set_potion")) {
			String potion = f.get("id").getAsString().replace("minecraft:", "");
			int meta = potion.contains("regeneration") ? 8193 : potion.contains("strength") ? 8201 : potion.contains("healing") ? 8197 : potion.contains("swiftness") ? 8194 : potion.contains("poison") ? 8196 : potion.contains("slowness") ? 8202 : potion.contains("weakness") ? 8200 : 0;
			if (potion.startsWith("strong_")) meta += 32; if (potion.startsWith("long_")) meta += 64;
			stack.setItemDamage(meta | (stack.getItemDamage() & 16384));
		} else if (name.equals("minecraft:set_ominous_bottle_amplifier")) stack.setItemDamage((int) number(f.get("amplifier"), random, 0));
		else if (name.equals("minecraft:set_enchantments")) {
			if ((!f.has("add") || !f.get("add").getAsBoolean()) && stack.hasTagCompound()) { stack.getTagCompound().removeTag("ench"); stack.getTagCompound().removeTag("StoredEnchantments"); }
			for (Map.Entry<String,JsonElement> entry : f.getAsJsonObject("enchantments").entrySet()) {
				Enchantment enchantment = ModEnchantments.byName(entry.getKey());
				if (enchantment == null || !ModEnchantments.available(enchantment)) { deferred("enchantment " + entry.getKey()); return null; }
				stack = enchanted(stack, enchantment, Math.max(1, Math.min(enchantment.getMaxLevel(), (int) number(entry.getValue(), random, 1))));
			}
		}
		else if (name.equals("minecraft:set_stew_effect")) {
			JsonArray effects = f.getAsJsonArray("effects"); JsonObject effect = effects.get(random.nextInt(effects.size())).getAsJsonObject();
			String effectName = effect.get("type").getAsString().replace("minecraft:", "");
			int id;
			switch (effectName) {
				case "night_vision": id = net.minecraft.potion.Potion.nightVision.id; break;
				case "jump_boost": id = net.minecraft.potion.Potion.jump.id; break;
				case "weakness": id = net.minecraft.potion.Potion.weakness.id; break;
				case "blindness": id = net.minecraft.potion.Potion.blindness.id; break;
				case "poison": id = net.minecraft.potion.Potion.poison.id; break;
				case "saturation": id = net.minecraft.potion.Potion.field_76443_y.id; break;
				default: deferred("stew effect " + effectName); return null;
			}
			net.minecraft.nbt.NBTTagCompound data = new net.minecraft.nbt.NBTTagCompound(), e = new net.minecraft.nbt.NBTTagCompound(); net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
			e.setByte("EffectId", (byte) id); e.setInteger("EffectDuration", (int) number(effect.get("duration"), random, 5) * 20); list.appendTag(e); data.setTag("Effects", list); stack.setTagCompound(data);
		} else throw new IllegalArgumentException("Unsupported loot function " + name);
		return stack;
	}
	private static ItemStack randomEnchantment(ItemStack stack, JsonObject function, Random random) {
		List<String> names = new ArrayList<>(); JsonElement options = function.get("options");
		if (options == null) names.addAll(LootEnchantmentOptions.randomLoot());
		else if (options.isJsonPrimitive()) names.addAll(LootEnchantmentOptions.expand(options.getAsString()));
		else for (JsonElement option : options.getAsJsonArray()) names.addAll(LootEnchantmentOptions.expand(option.getAsString()));
		boolean book = stack.getItem() == Items.book || stack.getItem() == Items.enchanted_book;
		names.removeIf(name -> { Enchantment enchantment = ModEnchantments.byName(name); return enchantment == null ? !book : !ModEnchantments.available(enchantment) || !book && !enchantment.canApply(stack); });
		if (names.isEmpty()) return null;
		String selected = names.get(random.nextInt(names.size())); Enchantment enchantment = ModEnchantments.byName(selected);
		if (enchantment == null) { deferred("enchantment " + selected); return null; }
		return enchanted(stack, enchantment, enchantment.getMinLevel() + random.nextInt(enchantment.getMaxLevel() - enchantment.getMinLevel() + 1));
	}
	private static ItemStack enchanted(ItemStack stack, Enchantment enchantment, int level) {
		if (stack.getItem() == Items.book) { ItemStack book = new ItemStack(Items.enchanted_book); if (stack.hasTagCompound()) book.setTagCompound((net.minecraft.nbt.NBTTagCompound) stack.getTagCompound().copy()); stack = book; }
		if (stack.getItem() == Items.enchanted_book) Items.enchanted_book.addEnchantment(stack, new EnchantmentData(enchantment, level));
		else stack.addEnchantment(enchantment, level);
		return stack;
	}
	public static ItemStack item(String id) {
		String name = id.replace("minecraft:", "");
		if (name.equals("prismarine")) return ganymedes01.etfuturum.ModBlocks.PRISMARINE_BLOCK.isEnabled() ? ganymedes01.etfuturum.ModBlocks.PRISMARINE_BLOCK.newItemStack() : null;
		if (name.equals("lapis_lazuli")) return new ItemStack(Items.dye, 1, 4);
		if (name.equals("dead_bush")) return new ItemStack(net.minecraft.init.Blocks.deadbush);
		if (name.endsWith("_stained_glass_pane")) {
			String[] colors = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
			for (int color = 0; color < colors.length; color++) if (name.equals(colors[color] + "_stained_glass_pane")) return new ItemStack(net.minecraft.init.Blocks.stained_glass_pane, 1, color);
		}
		if (name.equals("dark_oak_sapling")) return new ItemStack(net.minecraft.init.Blocks.sapling, 1, 5);
		if (name.equals("allium")) return new ItemStack(net.minecraft.init.Blocks.red_flower, 1, 2);
		if (name.equals("terracotta")) return new ItemStack(net.minecraft.init.Blocks.hardened_clay);
		if (name.equals("waxed_copper_block")) return ganymedes01.etfuturum.ModBlocks.COPPER_BLOCK.isEnabled() ? ganymedes01.etfuturum.ModBlocks.COPPER_BLOCK.newItemStack(1, 8) : null;
		String[] dyes = {"black", "red", "green", "brown", "blue", "purple", "cyan", "light_gray", "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white"};
		for (int color = 0; color < dyes.length; color++) if (name.equals(dyes[color] + "_dye")) {
			int modern = color == 15 ? 0 : color == 4 ? 1 : color == 3 ? 2 : color == 0 ? 3 : -1;
			return modern < 0 ? new ItemStack(Items.dye, 1, color) : ModItems.DYE.isEnabled() ? ModItems.DYE.newItemStack(1, modern) : null;
		}
		if (name.equals("cod") || name.equals("salmon") || name.equals("cooked_cod") || name.equals("cooked_salmon")) return new ItemStack(name.startsWith("cooked_") ? Items.cooked_fished : Items.fish, 1, name.endsWith("salmon") ? 1 : 0);
		if (name.equals("iron_nugget")) return ModItems.NUGGET_IRON.isEnabled() ? ModItems.NUGGET_IRON.newItemStack() : null;
		String[] nautilusArmor = {"copper", "golden", "iron", "diamond", "netherite"};
		for (int material = 0; material < nautilusArmor.length; material++) if (name.equals(nautilusArmor[material] + "_nautilus_armor")) return ModItems.NAUTILUS_ARMOR.isEnabled() ? ModItems.NAUTILUS_ARMOR.newItemStack(1, material) : null;
		if (name.equals("enchanted_golden_apple")) return new ItemStack(Items.golden_apple, 1, 1);
		if (name.startsWith("music_disc_")) {
			String record = name.substring("music_disc_".length());
			ModItems candidate = record.equals("relic") ? ModItems.RELIC_RECORD : record.equals("otherside") ? ModItems.OTHERSIDE_RECORD : record.equals("precipice") ? ModItems.PRECIPICE_RECORD : record.equals("creator") ? ModItems.CREATOR_RECORD : record.equals("creator_music_box") ? ModItems.CREATOR_MUSIC_BOX_RECORD : null;
			if (candidate != null && candidate.isEnabled()) return candidate.newItemStack();
			if (record.equals("13")) return new ItemStack(Items.record_13); if (record.equals("cat")) return new ItemStack(Items.record_cat);
		}
		if (name.equals("glow_berries")) return ModItems.GLOW_BERRIES_ITEM.isEnabled() ? ModItems.GLOW_BERRIES_ITEM.newItemStack() : null;
		if (name.equals("bamboo_planks")) return ganymedes01.etfuturum.ModBlocks.WOOD_PLANKS.isEnabled() ? ganymedes01.etfuturum.ModBlocks.WOOD_PLANKS.newItemStack(1, 4) : null;
		if (name.equals("bone_meal")) return new ItemStack(Items.dye, 1, 15);
		if (name.equals("acacia_planks")) return new ItemStack(net.minecraft.init.Blocks.planks, 1, 4);
		if (name.equals("splash_potion")) return new ItemStack(Items.potionitem, 1, 16384);
		Item item = GameRegistry.findItem("etfuturum", name); if (item == null) item = GameRegistry.findItem("minecraft", name);
		if (item == null) { deferred(id); return null; }
		if (!ganymedes01.etfuturum.configuration.ContentAvailability.available(item)) return null;
		return new ItemStack(item);
	}
	private static void deferred(String id) { if (DEFERRED.add(id)) org.apache.logging.log4j.LogManager.getLogger("UAB underground loot").warn("Deferred unavailable released loot: {}", id); }
	public static void fill(IInventory inventory, String table, Random random) {
		for (ItemStack stack : roll(table, random)) {
			List<Integer> empty = new ArrayList<>(); for (int i = 0; i < inventory.getSizeInventory(); i++) if (inventory.getStackInSlot(i) == null) empty.add(i);
			if (empty.isEmpty()) break; inventory.setInventorySlotContents(empty.get(random.nextInt(empty.size())), stack);
		}
		inventory.markDirty();
	}
}
