package ganymedes01.etfuturum.core.handlers;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.configuration.configs.ConfigEntities;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.*;
import ganymedes01.etfuturum.world.*;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.*;

public final class MobContentEvents {
	public static final MobContentEvents INSTANCE = new MobContentEvents();
	private MobContentEvents() { }
	@SubscribeEvent public void deepDarkSpawns(net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn event) {
		if (!event.world.isRemote && ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.containsCity(event.world, MathHelper.floor_double(event.x), MathHelper.floor_double(event.y), MathHelper.floor_double(event.z))) event.setResult(Event.Result.DENY);
	}
	@SubscribeEvent public void playerTick(TickEvent.PlayerTickEvent event) {
		EntityPlayer player = event.player;
		if (event.phase != TickEvent.Phase.END || player.worldObj.isRemote || player.ticksExisted % 20 != 0) return;
		NBTTagCompound data = player.getEntityData();
		for (String timer : new String[]{"EFRBadOmen", "EFRHeroOfVillage", "EFRTrialOmen"}) if (data.getInteger(timer) > 0) data.setInteger(timer, Math.max(0, data.getInteger(timer) - 20));
		if (ConfigEntities.enableRaids && data.getInteger("EFRBadOmen") > 0 && player.worldObj.difficultySetting != EnumDifficulty.PEACEFUL && !player.capabilities.disableDamage) {
			net.minecraft.village.Village village = player.worldObj.villageCollectionObj.findNearestVillage(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ), 0);
			if (village != null && RaidProgress.get(player.worldObj).start(player.worldObj, player, village)) data.setInteger("EFRBadOmen", 0);
		}
		if (player.ticksExisted % 12000 == 0) MobAcquisitionWorldgen.playerSpawns(player);
		int insomnia = player.isPlayerSleeping() ? 0 : Math.min(2147480, Math.max(0, data.getInteger("EFRTimeSinceRest")) + 1);
		data.setInteger("EFRTimeSinceRest", insomnia);
		ItemStack helmet = player.getCurrentArmor(3);
		if (helmet != null && helmet.getItem() == ModItems.TURTLE_HELMET.get() && !player.isInsideOfMaterial(Material.water) && !player.isPotionActive(net.minecraft.potion.Potion.waterBreathing)) player.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.waterBreathing.id, 200, 0));
		if (!ConfigEntities.enablePhantomSpawning || insomnia < 3600 || player.capabilities.disableDamage || player.worldObj.provider.dimensionId != 0 || player.worldObj.isDaytime()
				|| player.worldObj.difficultySetting == EnumDifficulty.PEACEFUL || !player.worldObj.getGameRules().getGameRuleBooleanValue("doMobSpawning") || player.ticksExisted % 1200 != 0 || player.getRNG().nextInt(insomnia) < 3600) return;
		int x = MathHelper.floor_double(player.posX), y = MathHelper.floor_double(player.posY), z = MathHelper.floor_double(player.posZ);
		if (y < 63 || !player.worldObj.canBlockSeeTheSky(x, y, z) || !player.worldObj.getEntitiesWithinAABB(EntityPhantom.class, player.boundingBox.expand(64, 64, 64)).isEmpty()) return;
		for (int i = 0, count = 1 + player.getRNG().nextInt(player.worldObj.difficultySetting.getDifficultyId()); i < count; i++) {
			int px = x + player.getRNG().nextInt(21) - 10, py = y + 20 + player.getRNG().nextInt(15), pz = z + player.getRNG().nextInt(21) - 10;
			if (py > 250 || !player.worldObj.blockExists(px, py, pz)) continue;
			EntityPhantom phantom = new EntityPhantom(player.worldObj); phantom.setLocationAndAngles(px + .5, py, pz + .5, 0, 0); phantom.setAttackTarget(player);
			if (player.worldObj.getCollidingBoundingBoxes(phantom, phantom.boundingBox).isEmpty() && player.worldObj.checkNoEntityCollision(phantom.boundingBox)) player.worldObj.spawnEntityInWorld(phantom);
		}
	}
	@SubscribeEvent public void zombieWater(LivingEvent.LivingUpdateEvent event) {
		if (event.entityLiving.worldObj.isRemote || event.entityLiving.getClass() != EntityZombie.class || !ConfigEntities.enableDrownedSpawning) return;
		EntityZombie zombie = (EntityZombie) event.entityLiving; NBTTagCompound data = zombie.getEntityData();
		int wet = zombie.isInsideOfMaterial(Material.water) ? Math.min(600, data.getInteger("EFRDrownedTime") + 1) : 0;
		data.setInteger("EFRDrownedTime", wet);
		if (!data.hasKey("EFRDrownedConversion") && wet >= 600) data.setInteger("EFRDrownedConversion", 300);
		if (!data.hasKey("EFRDrownedConversion")) return;
		int remaining = MathHelper.clamp_int(data.getInteger("EFRDrownedConversion"), 0, 300);
		data.setInteger("EFRDrownedConversion", Math.max(0, remaining - 1));
		if (remaining > 0) return;
		NBTTagCompound saved = new NBTTagCompound(); zombie.writeToNBT(saved);
		EntityDrowned drowned = new EntityDrowned(zombie.worldObj); drowned.readFromNBT(saved); drowned.setVillager(false);
		if (zombie.worldObj.spawnEntityInWorld(drowned)) { for (int slot = 0; slot < 5; slot++) zombie.setCurrentItemOrArmor(slot, null); zombie.setDead(); }
	}
	@SubscribeEvent public void bonemeal(BonemealEvent event) {
		if (!ModBlocks.SEAGRASS.isEnabled() || event.block == ModBlocks.SEAGRASS.get()) return; // IGrowable owns tall-seagrass growth.
		if (!ganymedes01.etfuturum.blocks.BlockAquaticPlant.sourceWater(event.world, event.x, event.y, event.z) || !ModBlocks.SEAGRASS.get().canPlaceBlockAt(event.world, event.x, event.y, event.z)) return;
		if (!event.world.isRemote) event.world.setBlock(event.x, event.y, event.z, ModBlocks.SEAGRASS.get(), 0, 2);
		event.setResult(Event.Result.ALLOW);
	}
	@SubscribeEvent public void oceanPlantBucket(net.minecraftforge.event.entity.player.FillBucketEvent event) {
		if (event.target != null && event.world.getBlock(event.target.blockX, event.target.blockY, event.target.blockZ) instanceof ganymedes01.etfuturum.blocks.BlockAquaticPlant) event.setCanceled(true);
	}
	@SubscribeEvent public void worldTick(TickEvent.WorldTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.world.isRemote) return;
		ganymedes01.etfuturum.command.CommandGameplayAudit.tick(event.world);
		if (event.world.provider.dimensionId == 0 && event.world.getTotalWorldTime() % 20 == 0) RaidProgress.get(event.world).tick(event.world);
	}
	@SubscribeEvent public void unload(WorldEvent.Unload event) { MobChestIndex.unload(event.world); AquaticPopulation.unload(event.world); ganymedes01.etfuturum.command.CommandGameplayAudit.unload(event.world); }
	@SubscribeEvent public void loadChunk(ChunkEvent.Load event) { if (!event.world.isRemote) for (Object object : event.getChunk().chunkTileEntityMap.values()) if (object instanceof net.minecraft.tileentity.TileEntity tile && MobChestIndex.chest(tile)) MobChestIndex.add(event.world, tile.xCoord, tile.yCoord, tile.zCoord); }
	@SubscribeEvent public void unloadChunk(ChunkEvent.Unload event) { MobChestIndex.unload(event.world, event.getChunk().xPosition, event.getChunk().zPosition); }
	@SubscribeEvent(priority = cpw.mods.fml.common.eventhandler.EventPriority.LOWEST) public void place(BlockEvent.PlaceEvent event) {
		if (event.world.isRemote) return;
		if (MobChestIndex.chest(event.world.getTileEntity(event.x, event.y, event.z))) MobChestIndex.add(event.world, event.x, event.y, event.z);
		if (event.placedBlock == Blocks.pumpkin && ModBlocks.COPPER_BLOCK.isEnabled() && event.world.getBlock(event.x, event.y - 1, event.z) == ModBlocks.COPPER_BLOCK.get()) {
			EntityCopperGolem golem = new EntityCopperGolem(event.world); golem.setLocationAndAngles(event.x + .5, event.y, event.z + .5, 0, 0);
			if (event.world.spawnEntityInWorld(golem)) { event.world.setBlockToAir(event.x, event.y, event.z); if (ModBlocks.COPPER_CHEST.isEnabled()) { event.world.setBlock(event.x, event.y - 1, event.z, ModBlocks.COPPER_CHEST.get(), 0, 3); MobChestIndex.add(event.world, event.x, event.y - 1, event.z); } }
		}
	}
	@SubscribeEvent public void breakBlock(BlockEvent.BreakEvent event) { if (!event.world.isRemote) { MobChestIndex.remove(event.world, event.x, event.y, event.z); vibration(event.getPlayer(), event.x, event.y, event.z); } }
	@SubscribeEvent public void note(NoteBlockEvent.Play event) { if (!event.world.isRemote) { AxisAlignedBB area = AxisAlignedBB.getBoundingBox(event.x - 16, event.y - 16, event.z - 16, event.x + 17, event.y + 17, event.z + 17); for (Object object : event.world.getEntitiesWithinAABB(EntityAllay.class, area)) ((EntityAllay) object).hearNote(event.x, event.y, event.z); } }
	@SubscribeEvent public void spawnList(WorldEvent.PotentialSpawns event) {
		if (event.type == net.minecraft.entity.EnumCreatureType.monster && ConfigEntities.enableOutposts && event.world.provider.dimensionId == 0 && RaidProgress.get(event.world).isOutpost(event.x, event.z) && event.y >= event.world.getTopSolidOrLiquidBlock(event.x, event.z) - 12) {
			event.list.clear(); ganymedes01.etfuturum.configuration.SpawnSettings.add(event.list, EntityPillager.class, 1, 1, 3);
		}
	}
	private static void vibration(net.minecraft.entity.Entity source, double x, double y, double z) { for (Object object : source.worldObj.getEntitiesWithinAABB(EntityWarden.class, source.boundingBox.expand(16, 16, 16))) ((EntityWarden) object).vibration(source, x, y, z); }
	@SubscribeEvent public void movement(LivingEvent.LivingUpdateEvent event) {
		if (event.entityLiving.worldObj.isRemote || event.entityLiving.ticksExisted % 10 != 0) return;
		if (event.entityLiving.onGround && event.entityLiving.getDistanceSq(event.entityLiving.prevPosX, event.entityLiving.prevPosY, event.entityLiving.prevPosZ) > .0025) vibration(event.entityLiving, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ);
	}
	@SubscribeEvent public void windFall(net.minecraftforge.event.entity.living.LivingFallEvent event) { NBTTagCompound data = event.entityLiving.getEntityData(); if (data.getLong("EFRWindLaunchUntil") > event.entityLiving.worldObj.getTotalWorldTime()) { event.distance = Math.max(0, (float) (data.getDouble("EFRWindLaunchY") - event.entityLiving.posY)); data.removeTag("EFRWindLaunchUntil"); data.removeTag("EFRWindLaunchY"); } }
	@SubscribeEvent public void hurt(LivingHurtEvent event) {
		if (event.entityLiving.worldObj.isRemote) return;
		if (event.source.getSourceOfDamage() instanceof EntityStatusArrow arrow && arrow.effect > 0 && arrow.effect < net.minecraft.potion.Potion.potionTypes.length && net.minecraft.potion.Potion.potionTypes[arrow.effect] != null) event.entityLiving.addPotionEffect(new net.minecraft.potion.PotionEffect(arrow.effect, arrow.duration, 0));
		if (event.entityLiving instanceof EntityWolf wolf && wolf.isTamed() && !event.source.isUnblockable()) {
			ItemStack armor = wolf.getEquipmentInSlot(3); if (armor != null && armor.getItem() == ModItems.WOLF_ARMOR.get()) { armor.damageItem(MathHelper.ceiling_float_int(event.ammount), wolf); event.ammount = 0; if (armor.stackSize <= 0) wolf.setCurrentItemOrArmor(3, null); }
		}
		vibration(event.entityLiving, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ);
	}
	@SubscribeEvent public void equipment(EntityInteractEvent event) {
		if (!(event.target instanceof EntityWolf wolf) || !wolf.isTamed() || wolf.getOwner() != event.entityPlayer) return;
		EntityPlayer player = event.entityPlayer; ItemStack held = player.getHeldItem(), armor = wolf.getEquipmentInSlot(3);
		if (held == null) return;
		if (held.getItem() == ModItems.WOLF_ARMOR.get() && armor == null && !wolf.isChild()) { if (!wolf.worldObj.isRemote) { ItemStack copy = held.copy(); copy.stackSize = 1; wolf.setCurrentItemOrArmor(3, copy); wolf.setEquipmentDropChance(3, 2); if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); } event.setCanceled(true); }
		else if (armor != null && held.getItem() == Items.shears) { if (!wolf.worldObj.isRemote) { wolf.setCurrentItemOrArmor(3, null); wolf.entityDropItem(armor, 0); held.damageItem(1, player); } event.setCanceled(true); }
		else if (armor != null && held.getItem() == ModItems.ARMADILLO_SCUTE.get() && armor.isItemDamaged()) { if (!wolf.worldObj.isRemote) { armor.setItemDamage(Math.max(0, armor.getItemDamage() - 8)); wolf.setCurrentItemOrArmor(3, armor.copy()); if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); } event.setCanceled(true); }
	}
}
