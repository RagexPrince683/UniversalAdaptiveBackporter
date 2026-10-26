package ganymedes01.etfuturum.entities;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
public class EntityBreeze extends EntityMob {
	private int shotCooldown, jumpCooldown;
	public EntityBreeze(World world) { super(world); setSize(.6F, 1.77F); tasks.addTask(5, new EntityAIWander(this, .8)); targetTasks.addTask(1, new EntityAIHurtByTarget(this, false)); targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 20, true)); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.63); getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(24); }
	@Override protected void fall(float distance) { }
	@Override public boolean attackEntityFrom(DamageSource source, float damage) { if (source.getSourceOfDamage() instanceof EntityArrow arrow) { if (!worldObj.isRemote) { arrow.motionX *= -1; arrow.motionY *= -1; arrow.motionZ *= -1; arrow.shootingEntity = this; arrow.velocityChanged = true; } return false; } return super.attackEntityFrom(source, damage); }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return; if (shotCooldown > 0) --shotCooldown; if (jumpCooldown > 0) --jumpCooldown;
		EntityLivingBase target = getAttackTarget(); if (target == null || !target.isEntityAlive() || getDistanceSqToEntity(target) > 576) return;
		getLookHelper().setLookPositionWithEntity(target, 30, 30);
		if (shotCooldown == 0 && onGround && canEntityBeSeen(target)) { EntityWindCharge charge = new EntityWindCharge(worldObj, this); charge.setThrowableHeading(target.posX - posX, target.posY + target.height / 2 - charge.posY, target.posZ - posZ, 1.2F, 2); worldObj.spawnEntityInWorld(charge); shotCooldown = 60; }
		if (jumpCooldown == 0 && onGround) { double angle = rand.nextDouble() * Math.PI * 2, x = target.posX + Math.cos(angle) * 4, z = target.posZ + Math.sin(angle) * 4; if (worldObj.blockExists(MathHelper.floor_double(x), MathHelper.floor_double(posY), MathHelper.floor_double(z))) { motionX = (x - posX) * .08; motionZ = (z - posZ) * .08; motionY = .8; isAirBorne = true; velocityChanged = true; jumpCooldown = 100; } }
	}
	@Override protected void dropFewItems(boolean player, int looting) { if (player) ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, ganymedes01.etfuturum.ModItems.BREEZE_ROD.newItemStack(1 + rand.nextInt(2) + rand.nextInt(looting + 1)), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("ShotCooldown", shotCooldown); tag.setInteger("JumpCooldown", jumpCooldown); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); shotCooldown = MathHelper.clamp_int(tag.getInteger("ShotCooldown"), 0, 60); jumpCooldown = MathHelper.clamp_int(tag.getInteger("JumpCooldown"), 0, 100); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.breeze.idle_air"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.breeze.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.breeze.death"; }
}
