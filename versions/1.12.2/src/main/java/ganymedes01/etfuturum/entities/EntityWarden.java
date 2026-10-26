package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.network.datasync.*;
import ganymedes01.etfuturum.*;
import net.minecraft.world.World;
import java.util.*;

public class EntityWarden extends EntityMob {
	private static final DataParameter<Byte> ANIMATION=EntityDataManager.createKey(EntityWarden.class,DataSerializers.BYTE);
	private final Map<UUID, Integer> anger = new LinkedHashMap<>();
	private int vibrationCooldown, sonicCooldown, sonicCharge, idleTicks, emergeTicks = 134;
	public EntityWarden(World world) { super(world); setSize(.9F, 2.9F); isImmuneToFire = true; tasks.addTask(2, new EntityAIAttackMelee(this, 1.2, true)); tasks.addTask(6, new EntityAIWander(this, .8)); experienceValue = 5; }
	@Override protected void entityInit() { super.entityInit(); dataManager.register(ANIMATION,(byte)0); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(500); getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(30); getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.3); getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1); getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24); }
	public void vibration(Entity source, double x, double y, double z) {
		if (world.isRemote || vibrationCooldown > 0 || emergeTicks > 0 || source == this || source instanceof EntityWarden || getDistanceSq(x, y, z) > 256 || source != null && source.isSneaking()) return;
		// Wool blocks vibration without making every ordinary wall acoustically opaque.
		double dx = posX - x, dy = posY + getEyeHeight() - y, dz = posZ - z; int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz) * 2));
		for (int i = 0; i <= steps; i++) { int bx = MathHelper.floor(x + dx * i / steps), by = MathHelper.floor(y + dy * i / steps), bz = MathHelper.floor(z + dz * i / steps); if (!world.isBlockLoaded(new BlockPos(bx,by,bz)) || world.getBlockState(new BlockPos(bx,by,bz)).getBlock() == net.minecraft.init.Blocks.WOOL) return; }
		vibrationCooldown = 40; idleTicks = 0; getNavigator().tryMoveToXYZ(x, y, z, 1);
		if (source instanceof EntityLivingBase && (!(source instanceof EntityPlayer) || !((EntityPlayer)source).capabilities.disableDamage)) increaseAnger((EntityLivingBase)source,35);
	}
	private void increaseAnger(EntityLivingBase entity, int amount) { if (anger.size() >= 16 && !anger.containsKey(entity.getUniqueID())) anger.remove(anger.keySet().iterator().next()); int value = Math.min(150, anger.getOrDefault(entity.getUniqueID(), 0) + amount); anger.put(entity.getUniqueID(), value); if (value >= 80) setAttackTarget(entity); }
	@Override public boolean attackEntityFrom(DamageSource source, float amount) { if ((emergeTicks > 0 || idleTicks > 1200) && source != DamageSource.OUT_OF_WORLD) return false; boolean hit = super.attackEntityFrom(source, amount); if (hit && !world.isRemote && source.getTrueSource() instanceof EntityLivingBase) { increaseAnger((EntityLivingBase)source.getTrueSource(), 100); idleTicks = 0; } return hit; }
	@Override public boolean attackEntityAsMob(Entity target) { if (emergeTicks > 0 || idleTicks > 1200) return false; boolean hit = super.attackEntityAsMob(target); if (hit) { sonicCooldown = 40; target.addVelocity(-MathHelper.sin(rotationYaw * .0174533F) * .5, .4, MathHelper.cos(rotationYaw * .0174533F) * .5); target.velocityChanged = true; } return hit; }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (world.isRemote || isAIDisabled()) return;
		if (emergeTicks > 0) { if (emergeTicks == 134) playSound(ModernSounds.get("entity.warden.emerge"), 1, 1); dataManager.set(ANIMATION, (byte) 2); --emergeTicks; getNavigator().clearPath(); motionX = motionZ = 0; return; }
		if (idleTicks > 1200) { dataManager.set(ANIMATION, (byte) 3); getNavigator().clearPath(); motionX = motionZ = 0; if (++idleTicks >= 1300) setDead(); return; }
		dataManager.set(ANIMATION, (byte) 0);
		if (vibrationCooldown > 0) --vibrationCooldown; if (sonicCooldown > 0) --sonicCooldown;
		if (ticksExisted % 20 == 0) { Iterator<Map.Entry<UUID, Integer>> it = anger.entrySet().iterator(); while (it.hasNext()) { Map.Entry<UUID, Integer> entry = it.next(); if (entry.getValue() <= 1) it.remove(); else entry.setValue(entry.getValue() - 1); } }
		if (getAttackTarget() == null && ticksExisted % 20 == 0 && !anger.isEmpty()) for (Object object : world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(24, 20, 24))) { EntityLivingBase suspect = (EntityLivingBase) object; if (anger.getOrDefault(suspect.getUniqueID(), 0) >= 80 && suspect.isEntityAlive()) { setAttackTarget(suspect); break; } }
		EntityLivingBase target = getAttackTarget();
		if (target != null && (!target.isEntityAlive() || target instanceof EntityPlayer && ((EntityPlayer)target).capabilities.disableDamage || getDistanceSq(target) > 1024)) { setAttackTarget(null); target = null; }
		if (target == null && ticksExisted % 100 == 0) {
			EntityLivingBase closest = null; double distance = 36;
			for (Object object : world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(6, 5, 6))) { EntityLivingBase entity = (EntityLivingBase) object; if (entity == this || entity instanceof EntityWarden || entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.disableDamage) continue; if (getDistanceSq(entity) < distance) { closest = entity; distance = getDistanceSq(entity); } }
			if (closest != null) increaseAnger(closest, 35);
		}
		if (ticksExisted % 120 == 0) for (Object object : world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().grow(20, 20, 20))) { EntityPlayer player = (EntityPlayer) object; if (!player.capabilities.disableDamage && true) player.addPotionEffect(new net.minecraft.potion.PotionEffect(AquaticEffects.DARKNESS,260,0)); }
		if (target != null) {
			idleTicks = 0;
			if (getDistanceSq(target) <= 225 && Math.abs(target.posY - posY) <= 20 && getDistanceSq(target) > 9 && sonicCooldown == 0) { if (sonicCharge == 0) playSound(ModernSounds.get("entity.warden.sonic_charge"), 3, 1); if (++sonicCharge >= 34) { playSound(ModernSounds.get("entity.warden.sonic_boom"), 3, 1); target.attackEntityFrom(new EntityDamageSource("sonic_boom", this).setDamageBypassesArmor().setMagicDamage(), 10); double dx = target.posX - posX, dz = target.posZ - posZ, length = Math.max(.1, Math.sqrt(dx * dx + dz * dz)); target.addVelocity(dx / length, .5, dz / length); target.velocityChanged = true; sonicCharge = 0; sonicCooldown = 60; dataManager.set(ANIMATION, (byte) 0); } else dataManager.set(ANIMATION, (byte) 1); } else { sonicCharge = 0; dataManager.set(ANIMATION, (byte) 0); }
		} else if (!hasCustomName() && ++idleTicks > 1200) { setAttackTarget(null); playSound(ModernSounds.get("entity.warden.dig"), 1, 1); }
	}
	public int getAnimationState() { return dataManager.get(ANIMATION); }
	@Override protected void dropFewItems(boolean player, int looting) { if(Content.available("sculk_catalyst"))entityDropItem(Content.stack("sculk_catalyst"),0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("EmergeTicks", emergeTicks); tag.setInteger("IdleTicks", idleTicks); NBTTagList list = new NBTTagList(); for (Map.Entry<UUID, Integer> entry : anger.entrySet()) { NBTTagCompound value = new NBTTagCompound(); value.setString("Suspect", entry.getKey().toString()); value.setInteger("Anger", entry.getValue()); list.appendTag(value); } tag.setTag("Anger", list); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); emergeTicks = MathHelper.clamp(tag.getInteger("EmergeTicks"), 0, 134); idleTicks = MathHelper.clamp(tag.getInteger("IdleTicks"), 0, 1300); anger.clear(); NBTTagList list = tag.getTagList("Anger", 10); for (int i = 0; i < Math.min(16, list.tagCount()); i++) { NBTTagCompound entry = list.getCompoundTagAt(i); try { anger.put(UUID.fromString(entry.getString("Suspect")), MathHelper.clamp(entry.getInteger("Anger"), 0, 150)); } catch (IllegalArgumentException ignored) { } } }

	@Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.warden.ambient");}
	@Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.warden.hurt");}
	@Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.warden.death");}
}
