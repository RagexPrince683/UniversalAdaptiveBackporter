package ganymedes01.etfuturum.core.handlers.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.api.ArmorSoundsRegistry;
import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

import java.util.Map;
import java.util.WeakHashMap;

/** Tracks client-side armor slots using the native Forge living-update event. */
public final class ArmorSoundEventHandler {

	public static final ArmorSoundEventHandler INSTANCE = new ArmorSoundEventHandler();

	private final Map<EntityLivingBase, EquipmentState> equipmentByEntity = new WeakHashMap<>();

	private ArmorSoundEventHandler() {
	}

	@SubscribeEvent
	public void handleArmorSounds(LivingUpdateEvent event) {
		EntityLivingBase entity = event.entityLiving;
		if (!entity.worldObj.isRemote) return;

		EquipmentState state = equipmentByEntity.get(entity);
		if (state == null) {
			equipmentByEntity.put(entity, new EquipmentState(entity));
			return;
		}
		if (SpectatorUtils.isSpectator(entity) || SpectatorUtils.wasSpectator(entity)) {
			state.update(entity);
			return;
		}

		for (int slot = 1; slot <= 4; slot++) {
			ItemStack stack = entity.getEquipmentInSlot(slot);
			Item previous = state.get(slot);
			Item current = stack == null ? null : stack.getItem();
			if (current != null && current != previous) {
				String sound = ArmorSoundsRegistry.getEquipSound(stack);
				if (sound != null) {
					entity.worldObj.playSoundAtEntity(entity, sound, 1, 1);
				}
			}
			state.set(slot, current);
		}
	}

	private static final class EquipmentState {
		private Item feet;
		private Item legs;
		private Item chest;
		private Item head;

		private EquipmentState(EntityLivingBase entity) {
			update(entity);
		}

		private void update(EntityLivingBase entity) {
			for (int slot = 1; slot <= 4; slot++) {
				ItemStack stack = entity.getEquipmentInSlot(slot);
				set(slot, stack == null ? null : stack.getItem());
			}
		}

		private Item get(int slot) {
			switch (slot) {
				case 1: return feet;
				case 2: return legs;
				case 3: return chest;
				case 4: return head;
				default: throw new IllegalArgumentException("Not an armor slot: " + slot);
			}
		}

		private void set(int slot, Item item) {
			switch (slot) {
				case 1: feet = item; break;
				case 2: legs = item; break;
				case 3: chest = item; break;
				case 4: head = item; break;
				default: throw new IllegalArgumentException("Not an armor slot: " + slot);
			}
		}
	}
}
