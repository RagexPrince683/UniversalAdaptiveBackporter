package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.Content;
import ganymedes01.etfuturum.ModEnchantments;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.datasync.*;
import net.minecraft.world.World;

/** The thrown stack is authoritative and is transferred exactly once on impact. */
public class EntityTrident extends EntityThrowable {
	private static final DataParameter<Byte> RETURNING = EntityDataManager.createKey(EntityTrident.class, DataSerializers.BYTE);
	private ItemStack stack;
	private boolean returning;
	@Override protected void entityInit() { super.entityInit(); dataManager.register(RETURNING, (byte) 0); }
	private java.util.UUID owner;
	public EntityTrident(World world) { super(world); }
	public EntityTrident(World world, EntityLivingBase shooter, ItemStack stack) { super(world, shooter); owner = shooter.getUniqueID(); this.stack = stack == null ? null : stack.copy(); if (this.stack != null) this.stack.setCount(1); }
	@Override protected float getGravityVelocity() { return isInWater() ? .01F : .05F; }
	@Override protected void onImpact(RayTraceResult hit) {
		if (world.isRemote) return;
		if (returning) return;
		if (hit.entityHit != null) {
			int impaling = ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.impaling, stack);
			boolean aquatic = hit.entityHit instanceof net.minecraft.entity.passive.EntityWaterMob;
			hit.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 8 + (aquatic ? impaling * 2.5F : 0));
			if (world.isThundering() && ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.channeling, stack) > 0 && world.canSeeSky(new BlockPos(hit.entityHit))) world.addWeatherEffect(new net.minecraft.entity.effect.EntityLightningBolt(world, hit.entityHit.posX, hit.entityHit.posY, hit.entityHit.posZ, false));
		}
		if (ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.loyalty, stack) > 0) { returning = true; dataManager.set(RETURNING, (byte) 1); noClip = true; return; }
		if (stack != null) { entityDropItem(stack, .1F); stack = null; }
		setDead();
	}
	@Override public void onUpdate() {
		if (world.isRemote && dataManager.get(RETURNING) != 0) { onEntityUpdate(); return; }
		if (world.isRemote || !returning) { super.onUpdate(); if (!world.isRemote && ticksExisted > 1200) { if (stack != null) { entityDropItem(stack, 0); stack = null; } setDead(); } return; }
		onEntityUpdate(); EntityPlayer player = owner == null ? null : world.getPlayerEntityByUUID(owner);
		if (player == null || !player.isEntityAlive() || player.dimension != dimension) { if (stack != null) { entityDropItem(stack, 0); stack = null; } setDead(); return; }
		double dx = player.posX - posX, dy = player.posY + player.getEyeHeight() - posY, dz = player.posZ - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length < 1.5) { if (stack != null && player.inventory.addItemStackToInventory(stack)) { stack = null; setDead(); } else if (ticksExisted > 400) { if (stack != null) entityDropItem(stack, 0); stack = null; setDead(); } return; }
		double speed = .15 + .15 * ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.loyalty, stack); motionX = dx / length * speed; motionY = dy / length * speed; motionZ = dz / length * speed; setPosition(posX + motionX, posY + motionY, posZ + motionZ);
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); if (stack != null) tag.setTag("Trident", stack.writeToNBT(new NBTTagCompound())); tag.setBoolean("Returning", returning); if (owner != null) tag.setString("OwnerUUID", owner.toString()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); stack = tag.hasKey("Trident", 10) ? new ItemStack(tag.getCompoundTag("Trident")) : null; if (stack != null && stack.getItem() != Content.item("trident")) stack = null; returning = tag.getBoolean("Returning"); noClip = returning; dataManager.set(RETURNING, (byte) (returning ? 1 : 0)); try { owner = java.util.UUID.fromString(tag.getString("OwnerUUID")); } catch (IllegalArgumentException ignored) { owner = null; } }
}
