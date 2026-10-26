package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.*;

public class EntityWarden extends EntityMob {
	private final Map<UUID, Integer> anger = new LinkedHashMap<>();
	private int vibrationCooldown, sonicCooldown, sonicCharge, idleTicks, emergeTicks = 134;
	public EntityWarden(World world) { super(world); setSize(.9F, 2.9F); isImmuneToFire = true; tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.2, true)); tasks.addTask(6, new EntityAIWander(this, .8)); experienceValue = 5; }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(500); getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(30); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.3); getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1); getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(24); }
	public void vibration(Entity source, double x, double y, double z) {
		if (worldObj.isRemote || vibrationCooldown > 0 || emergeTicks > 0 || source == this || source instanceof EntityWarden || getDistanceSq(x, y, z) > 256 || source != null && source.isSneaking()) return;
		// Wool blocks vibration without making every ordinary wall acoustically opaque.
		double dx = posX - x, dy = posY + getEyeHeight() - y, dz = posZ - z; int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz) * 2));
		for (int i = 0; i <= steps; i++) { int bx = MathHelper.floor_double(x + dx * i / steps), by = MathHelper.floor_double(y + dy * i / steps), bz = MathHelper.floor_double(z + dz * i / steps); if (!worldObj.blockExists(bx, by, bz) || worldObj.getBlock(bx, by, bz) == net.minecraft.init.Blocks.wool) return; }
		vibrationCooldown = 40; idleTicks = 0; getNavigator().tryMoveToXYZ(x, y, z, 1);
		if (source instanceof EntityLivingBase living && (!(source instanceof EntityPlayer player) || !player.capabilities.disableDamage)) increaseAnger(living, 35);
	}
	private void increaseAnger(EntityLivingBase entity, int amount) { if (anger.size() >= 16 && !anger.containsKey(entity.getUniqueID())) anger.remove(anger.keySet().iterator().next()); int value = Math.min(150, anger.getOrDefault(entity.getUniqueID(), 0) + amount); anger.put(entity.getUniqueID(), value); if (value >= 80) setAttackTarget(entity); }
	@Override public boolean attackEntityFrom(DamageSource source, float amount) { if ((emergeTicks > 0 || idleTicks > 1200) && source != DamageSource.outOfWorld) return false; boolean hit = super.attackEntityFrom(source, amount); if (hit && !worldObj.isRemote && source.getEntity() instanceof EntityLivingBase entity) { increaseAnger(entity, 100); idleTicks = 0; } return hit; }
	@Override public boolean attackEntityAsMob(Entity target) { if (emergeTicks > 0 || idleTicks > 1200) return false; boolean hit = super.attackEntityAsMob(target); if (hit) { sonicCooldown = 40; target.addVelocity(-MathHelper.sin(rotationYaw * .0174533F) * .5, .4, MathHelper.cos(rotationYaw * .0174533F) * .5); target.velocityChanged = true; } return hit; }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (emergeTicks > 0) { if (emergeTicks == 134) playSound(ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.warden.emerge", 1, 1); dataWatcher.updateObject(20, (byte) 2); --emergeTicks; getNavigator().clearPathEntity(); motionX = motionZ = 0; return; }
		if (idleTicks > 1200) { dataWatcher.updateObject(20, (byte) 3); getNavigator().clearPathEntity(); motionX = motionZ = 0; if (++idleTicks >= 1300) setDead(); return; }
		dataWatcher.updateObject(20, (byte) 0);
		if (vibrationCooldown > 0) --vibrationCooldown; if (sonicCooldown > 0) --sonicCooldown;
		if (ticksExisted % 20 == 0) { Iterator<Map.Entry<UUID, Integer>> it = anger.entrySet().iterator(); while (it.hasNext()) { Map.Entry<UUID, Integer> entry = it.next(); if (entry.getValue() <= 1) it.remove(); else entry.setValue(entry.getValue() - 1); } }
		if (getAttackTarget() == null && ticksExisted % 20 == 0 && !anger.isEmpty()) for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(24, 20, 24))) { EntityLivingBase suspect = (EntityLivingBase) object; if (anger.getOrDefault(suspect.getUniqueID(), 0) >= 80 && suspect.isEntityAlive()) { setAttackTarget(suspect); break; } }
		EntityLivingBase target = getAttackTarget();
		if (target != null && (!target.isEntityAlive() || target instanceof EntityPlayer player && player.capabilities.disableDamage || getDistanceSqToEntity(target) > 1024)) { setAttackTarget(null); target = null; }
		if (target == null && ticksExisted % 100 == 0) {
			EntityLivingBase closest = null; double distance = 36;
			for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(6, 5, 6))) { EntityLivingBase entity = (EntityLivingBase) object; if (entity == this || entity instanceof EntityWarden || entity instanceof EntityPlayer player && player.capabilities.disableDamage) continue; if (getDistanceSqToEntity(entity) < distance) { closest = entity; distance = getDistanceSqToEntity(entity); } }
			if (closest != null) increaseAnger(closest, 35);
		}
		if (ticksExisted % 120 == 0) for (Object object : worldObj.getEntitiesWithinAABB(EntityPlayer.class, boundingBox.expand(20, 20, 20))) { EntityPlayer player = (EntityPlayer) object; if (!player.capabilities.disableDamage && ganymedes01.etfuturum.potion.ModPotions.darkness != null) player.addPotionEffect(new net.minecraft.potion.PotionEffect(ganymedes01.etfuturum.potion.ModPotions.darkness.id, 260, 0)); }
		if (target != null) {
			idleTicks = 0;
			if (getDistanceSqToEntity(target) <= 225 && Math.abs(target.posY - posY) <= 20 && getDistanceSqToEntity(target) > 9 && sonicCooldown == 0) { if (sonicCharge == 0) playSound(ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.warden.sonic_charge", 3, 1); if (++sonicCharge >= 34) { playSound(ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.warden.sonic_boom", 3, 1); target.attackEntityFrom(new EntityDamageSource("sonic_boom", this).setDamageBypassesArmor().setMagicDamage(), 10); double dx = target.posX - posX, dz = target.posZ - posZ, length = Math.max(.1, Math.sqrt(dx * dx + dz * dz)); target.addVelocity(dx / length, .5, dz / length); target.velocityChanged = true; sonicCharge = 0; sonicCooldown = 60; dataWatcher.updateObject(20, (byte) 0); } else dataWatcher.updateObject(20, (byte) 1); } else { sonicCharge = 0; dataWatcher.updateObject(20, (byte) 0); }
		} else if (!hasCustomNameTag() && ++idleTicks > 1200) { setAttackTarget(null); playSound(ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.warden.dig", 1, 1); }
	}
	public int getAnimationState() { return dataWatcher.getWatchableObjectByte(20); }
	@Override protected void dropFewItems(boolean player, int looting) { if (ganymedes01.etfuturum.ModBlocks.SCULK_CATALYST.isEnabled()) entityDropItem(ganymedes01.etfuturum.ModBlocks.SCULK_CATALYST.newItemStack(), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("EmergeTicks", emergeTicks); tag.setInteger("IdleTicks", idleTicks); NBTTagList list = new NBTTagList(); for (Map.Entry<UUID, Integer> entry : anger.entrySet()) { NBTTagCompound value = new NBTTagCompound(); value.setString("Suspect", entry.getKey().toString()); value.setInteger("Anger", entry.getValue()); list.appendTag(value); } tag.setTag("Anger", list); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); emergeTicks = MathHelper.clamp_int(tag.getInteger("EmergeTicks"), 0, 134); idleTicks = MathHelper.clamp_int(tag.getInteger("IdleTicks"), 0, 1300); anger.clear(); NBTTagList list = tag.getTagList("Anger", 10); for (int i = 0; i < Math.min(16, list.tagCount()); i++) { NBTTagCompound entry = list.getCompoundTagAt(i); try { anger.put(UUID.fromString(entry.getString("Suspect")), MathHelper.clamp_int(entry.getInteger("Anger"), 0, 150)); } catch (IllegalArgumentException ignored) { } } }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.warden.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.warden.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.warden.death"; }
}
