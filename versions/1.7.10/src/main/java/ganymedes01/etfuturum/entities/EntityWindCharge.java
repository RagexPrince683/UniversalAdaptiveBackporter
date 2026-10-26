package ganymedes01.etfuturum.entities;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.*;
import net.minecraft.world.World;
public class EntityWindCharge extends EntityThrowable {
	public EntityWindCharge(World world) { super(world); }
	public EntityWindCharge(World world, EntityLivingBase shooter) { super(world, shooter); }
	@Override protected float getGravityVelocity() { return 0; }
	@Override protected float func_70182_d() { return 1.5F; }
	@Override public void onUpdate() { super.onUpdate(); if (!worldObj.isRemote && ticksExisted > 100) setDead(); }
	@Override protected void onImpact(MovingObjectPosition hit) {
		if (worldObj.isRemote) return;
		if (hit.entityHit != null) hit.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 1);
		for (Object object : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(4, 4, 4))) {
			EntityLivingBase entity = (EntityLivingBase) object; if (entity instanceof EntityBreeze) continue;
			double dx = entity.posX - posX, dy = entity.posY + entity.height * .5 - posY, dz = entity.posZ - posZ, distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (distance >= 4) continue; Vec3 from = Vec3.createVectorHelper(posX, posY, posZ), to = Vec3.createVectorHelper(entity.posX, entity.posY + entity.height * .5, entity.posZ);
			if (worldObj.rayTraceBlocks(from, to) != null) continue;
			double strength = (1 - distance / 4) * (getThrower() instanceof EntityBreeze ? .8 : 1.2), divisor = Math.max(.2, distance);
			entity.addVelocity(dx / divisor * strength, Math.max(.2, dy / divisor * strength), dz / divisor * strength); entity.fallDistance = 0; entity.velocityChanged = true; entity.getEntityData().setDouble("EFRWindLaunchY", entity.posY); entity.getEntityData().setLong("EFRWindLaunchUntil", worldObj.getTotalWorldTime() + 200);
		}
		if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && (getThrower() instanceof net.minecraft.entity.player.EntityPlayer || worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))) {
			int x = hit.blockX, y = hit.blockY, z = hit.blockZ, meta = worldObj.getBlockMetadata(x, y, z); Block block = worldObj.getBlock(x, y, z);
			if (block instanceof BlockTrapDoor) worldObj.setBlockMetadataWithNotify(x, y, z, meta ^ 4, 3);
			else if (block instanceof BlockLever) { worldObj.setBlockMetadataWithNotify(x, y, z, meta ^ 8, 3); worldObj.notifyBlocksOfNeighborChange(x, y, z, block); }
			else if (block instanceof BlockButton) { worldObj.setBlockMetadataWithNotify(x, y, z, meta | 8, 3); worldObj.notifyBlocksOfNeighborChange(x, y, z, block); worldObj.scheduleBlockUpdate(x, y, z, block, 20); }
			else if (block instanceof BlockDoor door) door.func_150014_a(worldObj, x, y, z, (meta & 4) == 0);
		}
		worldObj.playAuxSFX(2004, (int) posX, (int) posY, (int) posZ, 0); setDead();
	}
}
