package ganymedes01.etfuturum.entities;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
public class EntityZombieNautilus extends EntityNautilus implements net.minecraft.entity.monster.IMob {
	private boolean jockeyInitialized;
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote && !jockeyInitialized) { jockeyInitialized = true; if (riddenByEntity == null) { EntityDrowned drowned = new EntityDrowned(worldObj); drowned.copyLocationAndAnglesFrom(this); drowned.setCurrentItemOrArmor(0, ganymedes01.etfuturum.ModItems.TRIDENT.newItemStack()); if (worldObj.spawnEntityInWorld(drowned)) drowned.mountEntity(this); } } if (!worldObj.isRemote && worldObj.difficultySetting == net.minecraft.world.EnumDifficulty.PEACEFUL) setDead(); }
	@Override public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("JockeyInitialized", jockeyInitialized); }
	@Override public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag) { super.readEntityFromNBT(tag); jockeyInitialized = tag.getBoolean("JockeyInitialized"); }
	public EntityZombieNautilus(World world) { super(world); }
	@Override public EnumCreatureAttribute getCreatureAttribute() { return EnumCreatureAttribute.UNDEAD; }
	@Override public boolean isBreedingItem(ItemStack stack) { return false; }
	@Override public EntityAgeable createChild(EntityAgeable mate) { return null; }
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { super.onSpawnWithEgg(data); dataWatcher.updateObject(21, worldObj.getBiomeGenForCoords(net.minecraft.util.MathHelper.floor_double(posX), net.minecraft.util.MathHelper.floor_double(posZ)).temperature > .8F ? 1 : 0); return data; }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.zombie_nautilus.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.zombie_nautilus.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.zombie_nautilus.death"; }
}
