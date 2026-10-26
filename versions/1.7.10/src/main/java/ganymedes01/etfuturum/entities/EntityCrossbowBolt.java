package ganymedes01.etfuturum.entities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
/** Keeps native arrow collision/damage and advances a surviving piercing bolt past the struck body. */
public class EntityCrossbowBolt extends EntityArrow {
	private int pierces;
	public EntityCrossbowBolt(World world) { super(world); }
	public EntityCrossbowBolt(World world, EntityPlayer shooter, int piercing, float angle) { super(world, shooter, 1.6F); pierces = Math.min(4, piercing); double a = angle * Math.PI / 180, x = motionX; motionX = x * Math.cos(a) - motionZ * Math.sin(a); motionZ = x * Math.sin(a) + motionZ * Math.cos(a); setDamage(3); }
	@Override public void onUpdate() { double x = posX, y = posY, z = posZ, vx = motionX, vy = motionY, vz = motionZ; super.onUpdate(); if (!worldObj.isRemote && isDead && pierces > 0 && ticksExisted < 1200) { --pierces; isDead = false; setPosition(x + vx, y + vy, z + vz); motionX = vx * .99; motionY = vy * .99 - .05; motionZ = vz * .99; } }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Pierces", pierces); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); pierces = net.minecraft.util.MathHelper.clamp_int(tag.getInteger("Pierces"), 0, 4); }
}
