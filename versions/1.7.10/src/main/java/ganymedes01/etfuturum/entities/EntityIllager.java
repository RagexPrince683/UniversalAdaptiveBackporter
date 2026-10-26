package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Native village targets, with raid ownership saved independently of entity registration. */
public abstract class EntityIllager extends EntityMob {
	public int raidId;
	public EntityIllager(World world) {
		super(world); setSize(.6F, 1.95F);
		tasks.addTask(0, new EntityAISwimming(this)); tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1)); tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1, false));
		tasks.addTask(7, new EntityAIWander(this, .6)); tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 15)); tasks.addTask(9, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true)); targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 20, true));
		targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, 20, true)); targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, net.minecraft.entity.monster.EntityIronGolem.class, 20, true));
	}
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); dataWatcher.addObject(21, (byte) 0); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(24); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.35); getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32); getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5); }
	public boolean isAttacking() { return dataWatcher.getWatchableObjectByte(21) != 0; }
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote) { dataWatcher.updateObject(21, (byte) (getAttackTarget() != null ? 1 : 0)); if (raidId != 0 && ticksExisted % 20 == 0 && !ganymedes01.etfuturum.world.RaidProgress.get(worldObj).isActive(raidId)) { raidId = 0; detachHome(); } } }
	public boolean isCaptain() { return dataWatcher.getWatchableObjectByte(20) != 0; }
	public void setCaptain(boolean captain) { dataWatcher.updateObject(20, (byte) (captain ? 1 : 0)); }
	@Override protected boolean canDespawn() { return raidId == 0 && super.canDespawn(); }
	@Override public void onDeath(net.minecraft.util.DamageSource source) { if (!worldObj.isRemote && raidId != 0) ganymedes01.etfuturum.world.RaidProgress.get(worldObj).died(this); super.onDeath(source); }
	@Override protected void dropFewItems(boolean player, int looting) { if (player) { int count = rand.nextInt(2 + looting); if (count > 0) dropItem(Items.emerald, count); } if (isCaptain() && raidId == 0) entityDropItem(ModItems.OMINOUS_BOTTLE.newItemStack(), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("EFRRaid", raidId); tag.setBoolean("PatrolLeader", isCaptain()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); raidId = Math.max(0, tag.getInteger("EFRRaid")); setCaptain(tag.getBoolean("PatrolLeader")); }
}
