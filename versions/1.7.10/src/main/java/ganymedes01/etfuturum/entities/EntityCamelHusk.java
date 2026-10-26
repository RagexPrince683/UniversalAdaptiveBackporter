package ganymedes01.etfuturum.entities;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
public class EntityCamelHusk extends EntityCamel implements net.minecraft.entity.monster.IMob {
	private boolean jockeyInitialized;
	@Override public boolean isCreatureType(EnumCreatureType type, boolean count) { return type == EnumCreatureType.monster; }
	@Override public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("JockeyInitialized", jockeyInitialized); }
	@Override public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag) { super.readEntityFromNBT(tag); jockeyInitialized = tag.getBoolean("JockeyInitialized"); }
	public EntityCamelHusk(World world) { super(world); }
	@Override protected void dropFewItems(boolean player, int looting) { super.dropFewItems(player, looting); dropItem(net.minecraft.init.Items.rotten_flesh, 2 + rand.nextInt(2) + rand.nextInt(looting + 1)); }
	@Override public EnumCreatureAttribute getCreatureAttribute() { return EnumCreatureAttribute.UNDEAD; }
	@Override public boolean isBreedingItem(ItemStack stack) { return false; }
	@Override public EntityAgeable createChild(EntityAgeable mate) { return null; }
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote && !jockeyInitialized) { jockeyInitialized = true; if (riddenByEntity == null) { EntityHusk husk = new EntityHusk(worldObj); husk.copyLocationAndAnglesFrom(this); husk.setCurrentItemOrArmor(0, ganymedes01.etfuturum.ModItems.IRON_SPEAR.newItemStack()); if (worldObj.spawnEntityInWorld(husk)) husk.mountEntity(this); EntityParched parched = new EntityParched(worldObj); parched.copyLocationAndAnglesFrom(this); EntityMobSeat seat = new EntityMobSeat(worldObj, this, 1); if (worldObj.spawnEntityInWorld(seat) && worldObj.spawnEntityInWorld(parched)) parched.mountEntity(seat); } } if (!worldObj.isRemote && worldObj.difficultySetting == net.minecraft.world.EnumDifficulty.PEACEFUL) setDead(); if (!worldObj.isRemote && riddenByEntity instanceof net.minecraft.entity.monster.EntityMob rider && rider.getAttackTarget() != null && ticksExisted % 10 == 0) getNavigator().tryMoveToEntityLiving(rider.getAttackTarget(), 1.2); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.camel_husk.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.camel_husk.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.camel_husk.death"; }
}
