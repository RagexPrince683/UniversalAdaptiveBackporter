package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.UUID;

public class EntityVex extends EntityMob {
	private UUID owner;
	private int lifetime = -1, attackCooldown;
	private double homeX, homeY, homeZ;
	public EntityVex(World world) { super(world); setSize(.4F, .8F); isImmuneToFire = true; noClip = true; setCurrentItemOrArmor(0, new ItemStack(Items.iron_sword)); equipmentDropChances[0] = 0; targetTasks.addTask(1, new EntityAIHurtByTarget(this, false)); targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, net.minecraft.entity.player.EntityPlayer.class, 20, true)); }
	public void setOwner(EntityLivingBase owner) { this.owner = owner.getUniqueID(); homeX = owner.posX; homeY = owner.posY + 2; homeZ = owner.posZ; }
	public void setLifetime(int lifetime) { this.lifetime = lifetime; }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(14); getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4); }
	@Override protected void fall(float distance) { }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (owner == null && ticksExisted == 1) { homeX = posX; homeY = posY + 1; homeZ = posZ; }
		if (lifetime > 0) --lifetime; else if (lifetime == 0 && ticksExisted % 20 == 0) attackEntityFrom(DamageSource.starve, 1);
		if (attackCooldown > 0) --attackCooldown;
		if (owner != null && ticksExisted % 40 == 0) for (Object object : worldObj.getEntitiesWithinAABB(EntityEvoker.class, boundingBox.expand(32, 16, 32))) { EntityEvoker caster = (EntityEvoker) object; if (owner.equals(caster.getUniqueID())) { setAttackTarget(caster.getAttackTarget()); homeX = caster.posX; homeY = caster.posY + 2; homeZ = caster.posZ; break; } }
	}
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		if (worldObj.isRemote) return;
		EntityLivingBase target = getAttackTarget(); if (target != null && (!target.isEntityAlive() || getDistanceSqToEntity(target) > 1024)) { setAttackTarget(null); target = null; }
		double x = target == null ? homeX + Math.sin(ticksExisted * .03) * 4 : target.posX, y = target == null ? homeY : target.posY + target.height / 2, z = target == null ? homeZ + Math.cos(ticksExisted * .03) * 4 : target.posZ;
		if (!worldObj.blockExists(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z))) return;
		double dx = x - posX, dy = y - posY, dz = z - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length > .2) { double speed = target == null ? .2 : .45; motionX += (dx / length * speed - motionX) * .15; motionY += (dy / length * speed - motionY) * .15; motionZ += (dz / length * speed - motionZ) * .15; rotationYaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90; renderYawOffset = rotationYaw; }
		moveEntity(motionX, motionY, motionZ);
		if (target != null && attackCooldown == 0 && boundingBox.expand(.2, .2, .2).intersectsWith(target.boundingBox)) { attackEntityAsMob(target); attackCooldown = 20; }
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("LifeTicks", lifetime); if (owner != null) tag.setString("Owner", owner.toString()); tag.setDouble("BoundX", homeX); tag.setDouble("BoundY", homeY); tag.setDouble("BoundZ", homeZ); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); lifetime = tag.hasKey("LifeTicks") ? MathHelper.clamp_int(tag.getInteger("LifeTicks"), -1, 2400) : -1; try { owner = UUID.fromString(tag.getString("Owner")); } catch (IllegalArgumentException ignored) { owner = null; } homeX = tag.getDouble("BoundX"); homeY = tag.getDouble("BoundY"); homeZ = tag.getDouble("BoundZ"); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.vex.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.vex.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.vex.death"; }
}
