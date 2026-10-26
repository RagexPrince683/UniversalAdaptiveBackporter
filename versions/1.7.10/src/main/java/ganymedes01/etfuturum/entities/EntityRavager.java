package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityRavager extends EntityIllager {
	private int stunTicks, roarTicks;
	public EntityRavager(World world) { super(world); setSize(1.95F, 2.2F); stepHeight = 1; tasks.addTask(2, new EntityAIAttackOnCollide(this, 1, true)); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100); getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(12); getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(.75); }
	@Override public boolean attackEntityAsMob(Entity target) { if (stunTicks > 0 || roarTicks > 0) return false; if (target instanceof EntityPlayer player && player.isBlocking() && rand.nextBoolean()) { stunTicks = 40; return false; } boolean hit = super.attackEntityAsMob(target); if (hit) { target.addVelocity(-MathHelper.sin(rotationYaw * .0174533F), .5, MathHelper.cos(rotationYaw * .0174533F)); target.velocityChanged = true; } return hit; }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (stunTicks > 0) { getNavigator().clearPathEntity(); if (--stunTicks == 0) roarTicks = 20; }
		if (roarTicks > 0 && --roarTicks == 10) for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(4, 3, 4))) { EntityLivingBase target = (EntityLivingBase) object; if (target != this && !(target instanceof EntityIllager)) { target.attackEntityFrom(DamageSource.causeMobDamage(this), 6); double dx = target.posX - posX, dz = target.posZ - posZ, length = Math.max(.1, Math.sqrt(dx * dx + dz * dz)); target.addVelocity(dx / length, .4, dz / length); target.velocityChanged = true; } }
	}
	@Override protected void dropFewItems(boolean player, int looting) { dropItem(Items.saddle, 1); }
	@Override public double getMountedYOffset() { return 2.1; }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("StunTick", stunTicks); tag.setInteger("RoarTick", roarTicks); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); stunTicks = MathHelper.clamp_int(tag.getInteger("StunTick"), 0, 40); roarTicks = MathHelper.clamp_int(tag.getInteger("RoarTick"), 0, 20); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.ravager.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.ravager.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.ravager.death"; }
}
