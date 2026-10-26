package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Keeps native feline taming, owner/sit AI and persistence; adds village ecology and modern variants. */
public class EntityCat extends EntityOcelot {
	private boolean sleptWithOwner;
	public EntityCat(World world) { super(world); }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); dataWatcher.addObject(21, (byte) 14); }
	public int getVariant() { return dataWatcher.getWatchableObjectByte(20) & 15; }
	public int getCollarColor() { return dataWatcher.getWatchableObjectByte(21) & 15; }
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { super.onSpawnWithEgg(data); dataWatcher.updateObject(20, (byte) rand.nextInt(11)); return data; }
	@Override public boolean getCanSpawnHere() { int x = net.minecraft.util.MathHelper.floor_double(posX), y = net.minecraft.util.MathHelper.floor_double(posY), z = net.minecraft.util.MathHelper.floor_double(posZ); return worldObj.provider.dimensionId == 0 && worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty() && !worldObj.isAnyLiquid(boundingBox) && worldObj.getBlock(x, y - 1, z).isNormalCube() && (worldObj.villageCollectionObj.findNearestVillage(x, y, z, 32) != null || net.minecraftforge.common.BiomeDictionary.isBiomeOfType(worldObj.getBiomeGenForCoords(x, z), net.minecraftforge.common.BiomeDictionary.Type.SWAMP)) && worldObj.getEntitiesWithinAABB(EntityCat.class, boundingBox.expand(48, 16, 48)).size() < 5; }
	@Override public boolean isBreedingItem(ItemStack stack) { return stack != null && stack.getItem() == Items.fish && stack.getItemDamage() < 2; }
	@Override public boolean canMateWith(net.minecraft.entity.passive.EntityAnimal other) { return other instanceof EntityCat && super.canMateWith(other); }
	@Override public EntityOcelot createChild(EntityAgeable mate) { EntityCat child = new EntityCat(worldObj); child.dataWatcher.updateObject(20, (byte) (rand.nextBoolean() ? getVariant() : ((EntityCat) mate).getVariant())); if (isTamed()) { child.func_152115_b(func_152113_b()); child.setTamed(true); child.setTameSkin(1); } return child; }
	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (isTamed() && getOwner() == player && held != null && held.getItem() == Items.dye) { if (!worldObj.isRemote) { dataWatcher.updateObject(21, (byte) (15 - held.getItemDamage())); if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); } return true; }
		return super.interact(player);
	}
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote || ticksExisted % 20 != 0 || !isTamed()) return;
		EntityLivingBase owner = getOwner();
		if (!(owner instanceof EntityPlayer player)) return;
		if (player.isPlayerSleeping() && getDistanceSqToEntity(player) < 16) sleptWithOwner = true;
		else if (sleptWithOwner && !player.isPlayerSleeping()) { sleptWithOwner = false; if (rand.nextFloat() < .7F && getDistanceSqToEntity(player) < 16) { net.minecraft.item.Item[] gifts = {Items.string, Items.feather, Items.rotten_flesh, Items.bone, ganymedes01.etfuturum.ModItems.PHANTOM_MEMBRANE.get()}; entityDropItem(new ItemStack(gifts[rand.nextInt(gifts.length)]), 0); } }
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("CatType", getVariant()); tag.setInteger("CollarColor", getCollarColor()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataWatcher.updateObject(20, (byte) net.minecraft.util.MathHelper.clamp_int(tag.getInteger("CatType"), 0, 10)); dataWatcher.updateObject(21, (byte) (tag.hasKey("CollarColor") ? tag.getInteger("CollarColor") & 15 : 14)); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.cat.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.cat.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.cat.death"; }
}
