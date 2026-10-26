package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityArmadillo extends EntityAnimal {
	private int calmTicks, scuteTicks = 12000;
	public EntityArmadillo(World world) { super(world); setSize(.7F, .65F); tasks.addTask(0, new EntityAISwimming(this)); tasks.addTask(2, new EntityAIMate(this, 1)); tasks.addTask(3, new EntityAITempt(this, 1.1, Items.spider_eye, false)); tasks.addTask(4, new EntityAIFollowParent(this, 1.1)); tasks.addTask(5, new EntityAIWander(this, 1)); }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(12); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.14); }
	public boolean isRolled() { return dataWatcher.getWatchableObjectByte(20) != 0; }
	@Override public boolean isBreedingItem(ItemStack stack) { return !isRolled() && stack != null && stack.getItem() == Items.spider_eye; }
	@Override public EntityAgeable createChild(EntityAgeable mate) { return new EntityArmadillo(worldObj); }
	@Override public boolean interact(EntityPlayer player) { ItemStack held = player.getHeldItem(); if (held != null && held.getItem() == ModItems.BRUSH.get() && !isChild()) { if (!worldObj.isRemote) { ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, ModItems.ARMADILLO_SCUTE.newItemStack(), .1F); held.damageItem(16, player); } return true; } return super.interact(player); }
	@Override public boolean attackEntityFrom(DamageSource source, float amount) { if (isRolled() && !source.isUnblockable()) amount = Math.max(0, amount - 1) * .5F; boolean hit = super.attackEntityFrom(source, amount); if (hit && !worldObj.isRemote) calmTicks = 100; return hit; }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (ticksExisted % 10 == 0 && !isInWater()) for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(7, 3, 7))) { EntityLivingBase entity = (EntityLivingBase) object; if (entity.isEntityUndead() || entity instanceof EntityPlayer player && (!player.capabilities.disableDamage && (player.isSprinting() || player.ridingEntity != null))) { calmTicks = 100; break; } }
		if (calmTicks > 0) --calmTicks;
		dataWatcher.updateObject(20, (byte) (calmTicks > 0 && !isInWater() && !isBurning() ? 1 : 0));
		if (isRolled()) { getNavigator().clearPathEntity(); motionX = motionZ = 0; }
		if (!isChild() && --scuteTicks <= 0) { scuteTicks = 6000 + rand.nextInt(6000); if (worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot")) ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, ModItems.ARMADILLO_SCUTE.newItemStack(), .1F); }
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("ScuteTime", scuteTicks); tag.setInteger("ScaredTicks", calmTicks); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); scuteTicks = tag.hasKey("ScuteTime") ? MathHelper.clamp_int(tag.getInteger("ScuteTime"), 0, 12000) : 12000; calmTicks = MathHelper.clamp_int(tag.getInteger("ScaredTicks"), 0, 100); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.armadillo.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.armadillo.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.armadillo.death"; }
}
