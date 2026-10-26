package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/** The thrown stack is authoritative and is transferred exactly once on impact. */
public class EntityTrident extends EntityThrowable {
	private ItemStack stack;
	private boolean returning;
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(16, (byte) 0); }
	private java.util.UUID owner;
	public EntityTrident(World world) { super(world); }
	public EntityTrident(World world, EntityLivingBase shooter, ItemStack stack) { super(world, shooter); owner = shooter.getUniqueID(); this.stack = stack == null ? null : stack.copy(); if (this.stack != null) this.stack.stackSize = 1; }
	@Override protected float func_70182_d() { return 2.5F; }
	@Override protected float getGravityVelocity() { return isInWater() ? .01F : .05F; }
	@Override protected void onImpact(MovingObjectPosition hit) {
		if (worldObj.isRemote) return;
		if (returning) return;
		if (hit.entityHit != null) {
			int impaling = ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.impaling, stack);
			boolean aquatic = hit.entityHit instanceof net.minecraft.entity.passive.EntityWaterMob || hit.entityHit instanceof EntityAquaticAnimal || hit.entityHit instanceof EntityNautilus || hit.entityHit instanceof EntityGuardian;
			hit.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 8 + (aquatic ? impaling * 2.5F : 0));
			if (worldObj.isThundering() && ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.channeling, stack) > 0 && worldObj.canBlockSeeTheSky(net.minecraft.util.MathHelper.floor_double(hit.entityHit.posX), net.minecraft.util.MathHelper.floor_double(hit.entityHit.posY), net.minecraft.util.MathHelper.floor_double(hit.entityHit.posZ))) worldObj.addWeatherEffect(new net.minecraft.entity.effect.EntityLightningBolt(worldObj, hit.entityHit.posX, hit.entityHit.posY, hit.entityHit.posZ));
		}
		if (ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.loyalty, stack) > 0) { returning = true; dataWatcher.updateObject(16, (byte) 1); noClip = true; return; }
		if (stack != null) { entityDropItem(stack, .1F); stack = null; }
		setDead();
	}
	@Override public void onUpdate() {
		if (worldObj.isRemote && dataWatcher.getWatchableObjectByte(16) != 0) { onEntityUpdate(); return; }
		if (worldObj.isRemote || !returning) { super.onUpdate(); if (!worldObj.isRemote && ticksExisted > 1200) { if (stack != null) { entityDropItem(stack, 0); stack = null; } setDead(); } return; }
		onEntityUpdate(); EntityPlayer player = owner == null ? null : worldObj.func_152378_a(owner);
		if (player == null || !player.isEntityAlive() || player.dimension != dimension) { if (stack != null) { entityDropItem(stack, 0); stack = null; } setDead(); return; }
		double dx = player.posX - posX, dy = player.posY + player.getEyeHeight() - posY, dz = player.posZ - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length < 1.5) { if (stack != null && player.inventory.addItemStackToInventory(stack)) { stack = null; setDead(); } else if (ticksExisted > 400) { if (stack != null) entityDropItem(stack, 0); stack = null; setDead(); } return; }
		double speed = .15 + .15 * ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.loyalty, stack); motionX = dx / length * speed; motionY = dy / length * speed; motionZ = dz / length * speed; setPosition(posX + motionX, posY + motionY, posZ + motionZ);
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); if (stack != null) tag.setTag("Trident", stack.writeToNBT(new NBTTagCompound())); tag.setBoolean("Returning", returning); if (owner != null) tag.setString("OwnerUUID", owner.toString()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); stack = tag.hasKey("Trident", 10) ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Trident")) : null; if (stack != null && stack.getItem() != ModItems.TRIDENT.get()) stack = null; returning = tag.getBoolean("Returning"); noClip = returning; dataWatcher.updateObject(16, (byte) (returning ? 1 : 0)); try { owner = java.util.UUID.fromString(tag.getString("OwnerUUID")); } catch (IllegalArgumentException ignored) { owner = null; } }
}
