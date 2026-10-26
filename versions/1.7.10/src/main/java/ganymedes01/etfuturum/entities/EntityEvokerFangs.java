package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import java.util.UUID;

public class EntityEvokerFangs extends Entity {
	private int warmup, life = 22, animationTicks;
	public float animationProgress(float partial) { return Math.min(1, (animationTicks + partial) / 22F); }
	private UUID owner;
	public EntityEvokerFangs(World world) { super(world); setSize(.5F, .8F); }
	public EntityEvokerFangs(World world, EntityLivingBase caster, double x, double y, double z, int delay) { this(world); owner = caster.getUniqueID(); setPosition(x, y, z); warmup = delay; }
	@Override protected void entityInit() { dataWatcher.addObject(16, (byte) 0); }
	@Override public void onUpdate() {
		super.onUpdate(); if (worldObj.isRemote) { if (isActive()) ++animationTicks; return; }
		if (warmup-- > 0) return;
		dataWatcher.updateObject(16, (byte) 1);
		if (life-- == 14) for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(.2, 0, .2))) {
			EntityLivingBase target = (EntityLivingBase) object;
			if (!(target instanceof EntityIllager) && !target.getUniqueID().equals(owner)) target.attackEntityFrom(DamageSource.magic, 6);
		}
		if (life <= 0) setDead();
	}
	public boolean isActive() { return dataWatcher.getWatchableObjectByte(16) != 0; }
	@Override protected void writeEntityToNBT(NBTTagCompound tag) { tag.setInteger("Warmup", warmup); tag.setInteger("Life", life); if (owner != null) tag.setString("Owner", owner.toString()); }
	@Override protected void readEntityFromNBT(NBTTagCompound tag) { warmup = Math.max(-1, Math.min(100, tag.getInteger("Warmup"))); life = Math.max(0, Math.min(22, tag.getInteger("Life"))); try { owner = UUID.fromString(tag.getString("Owner")); } catch (IllegalArgumentException ignored) { owner = null; } }
}
