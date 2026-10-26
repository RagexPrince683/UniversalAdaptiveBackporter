package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityZoglin extends EntityMob {
	public EntityZoglin(World world) {
		super(world);
		setSize(1.4F, 1.4F);
		isImmuneToFire = true;
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, new EntityAIAttackOnCollide(this, 1, true));
		tasks.addTask(5, new EntityAIWander(this, 0.7));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 10, true, false,
				entity -> !(entity instanceof EntityZoglin) && !(entity instanceof EntityCreeper)));
	}
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(16, 0); }
	public void setGrowingAge(int age) { dataWatcher.updateObject(16, age); }
	@Override public boolean isChild() { return dataWatcher.getWatchableObjectInt(16) < 0; }
	@Override protected boolean isAIEnabled() { return true; }
	@Override public EnumCreatureAttribute getCreatureAttribute() { return EnumCreatureAttribute.UNDEAD; }
	@Override protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.6);
	}
	@Override public boolean attackEntityAsMob(Entity target) { boolean hit = target.attackEntityFrom(net.minecraft.util.DamageSource.causeMobDamage(this), isChild() ? .5F : 3 + rand.nextInt(6)); if (hit && !isChild()) { double yaw = rotationYaw * Math.PI / 180; target.addVelocity(-Math.sin(yaw) * .5, .25, Math.cos(yaw) * .5); target.velocityChanged = true; } return hit; }
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote && isChild()) setGrowingAge(dataWatcher.getWatchableObjectInt(16) + 1); }
	@Override protected void dropFewItems(boolean player, int looting) { int count = rand.nextInt(2) + rand.nextInt(looting + 1); if (count > 0) dropItem(Items.rotten_flesh, count); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Age", dataWatcher.getWatchableObjectInt(16)); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); setGrowingAge(Math.min(0, tag.getInteger("Age"))); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.zoglin.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.zoglin.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.zoglin.death"; }
}
