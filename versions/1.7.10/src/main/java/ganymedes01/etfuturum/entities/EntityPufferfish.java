package ganymedes01.etfuturum.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityPufferfish extends EntityFish {
	private int inflationTicks, deflationTicks;
	public EntityPufferfish(World world) { super(world); setSize(.35F, .35F); }
	@Override public int fishType() { return 3; }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(17, (byte) 0); }
	public int getPuffState() { return dataWatcher.getWatchableObjectByte(17); }
	private void puff(int state) { dataWatcher.updateObject(17, (byte) state); }
	private boolean scares(EntityLivingBase entity) { return !(entity instanceof EntityFish) && (!(entity instanceof EntityPlayer player) || !player.capabilities.disableDamage); }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate();
		int state = getPuffState(); float size = state == 2 ? .7F : state == 1 ? .5F : .35F;
		if (width != size) {
			// Inflation must not push the fish sideways or grow its collision box into a wall.
			double half = size / 2D;
			net.minecraft.util.AxisAlignedBB box = net.minecraft.util.AxisAlignedBB.getBoundingBox(
					posX - half, boundingBox.minY, posZ - half, posX + half, boundingBox.minY + size, posZ + half);
			if (size < width || worldObj.getCollidingBoundingBoxes(this, box).isEmpty()) {
				double x = posX, y = posY, z = posZ;
				setSize(size, size);
				setPosition(x, y, z);
			}
		}
		if (worldObj.isRemote || !isEntityAlive()) return;
		if (ticksExisted % 10 == 0) {
			boolean danger = false;
			for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(2, 2, 2))) if (object != this && scares((EntityLivingBase) object)) { danger = true; break; }
			if (danger) { inflationTicks += 10; deflationTicks = 0; puff(inflationTicks > 40 ? 2 : 1); }
			else { inflationTicks = 0; deflationTicks += 10; if (deflationTicks > 100) puff(0); else if (deflationTicks > 60 && state == 2) puff(1); }
		}
		if (getPuffState() > 0 && ticksExisted % 10 == 0) for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(.3, .3, .3))) {
			EntityLivingBase entity = (EntityLivingBase) object;
			if (entity != this && scares(entity) && entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1 + getPuffState())) entity.addPotionEffect(new PotionEffect(Potion.poison.id, 60 * getPuffState(), 0));
		}
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("PuffState", getPuffState()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); puff(Math.max(0, Math.min(2, tag.getInteger("PuffState")))); }
}
