package ganymedes01.etfuturum.entities;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import java.util.UUID;

/** Native mount identity, with bounded UUID recovery after chunk loading. */
public class EntityMobSeat extends Entity {
	private UUID ownerId;
	private EntityRideableMob owner;
	private int missingTicks;
	public EntityMobSeat(World world) { super(world); setSize(0, 0); noClip = true; setInvisible(true); }
	public EntityMobSeat(World world, EntityRideableMob owner, int slot) { this(world); this.owner = owner; ownerId = owner.getUniqueID(); dataWatcher.updateObject(16, owner.getEntityId()); dataWatcher.updateObject(17, slot); owner.positionPassenger(this, slot); }
	@Override protected void entityInit() { dataWatcher.addObject(16, 0); dataWatcher.addObject(17, 1); }
	public boolean belongsTo(EntityRideableMob mob, int slot) { return ownerId != null && ownerId.equals(mob.getUniqueID()) && dataWatcher.getWatchableObjectInt(17) == slot; }
	@Override public boolean isEntityInvulnerable() { return true; }
	@Override public void onUpdate() {
		super.onUpdate();
		if (owner == null) {
			Entity candidate = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(16));
			if (candidate instanceof EntityRideableMob mount && (worldObj.isRemote || ownerId != null && ownerId.equals(mount.getUniqueID()))) owner = mount;
			if (!worldObj.isRemote && owner == null && ticksExisted % 20 == 0) for (Object object : worldObj.getEntitiesWithinAABB(EntityRideableMob.class, boundingBox.expand(8, 8, 8))) { EntityRideableMob mount = (EntityRideableMob) object; if (ownerId != null && ownerId.equals(mount.getUniqueID())) { owner = mount; dataWatcher.updateObject(16, mount.getEntityId()); break; } }
		}
		if (owner != null && !owner.isDead) { missingTicks = 0; owner.positionPassenger(this, dataWatcher.getWatchableObjectInt(17)); if (!worldObj.isRemote && owner.riddenByEntity == null && riddenByEntity != null) riddenByEntity.mountEntity(owner); }
		else if (!worldObj.isRemote && ++missingTicks > 200) { if (riddenByEntity != null) riddenByEntity.mountEntity(null); setDead(); }
		if (!worldObj.isRemote && riddenByEntity == null && ticksExisted > 200) setDead();
	}
	@Override public void updateRiderPosition() { if (riddenByEntity != null) riddenByEntity.setPosition(posX, posY + riddenByEntity.getYOffset(), posZ); }
	@Override protected void writeEntityToNBT(NBTTagCompound tag) { if (ownerId != null) tag.setString("MountUUID", ownerId.toString()); tag.setInteger("Seat", dataWatcher.getWatchableObjectInt(17)); }
	@Override protected void readEntityFromNBT(NBTTagCompound tag) { try { ownerId = UUID.fromString(tag.getString("MountUUID")); } catch (IllegalArgumentException ignored) { ownerId = null; } dataWatcher.updateObject(17, Math.max(1, Math.min(3, tag.getInteger("Seat")))); }
}
