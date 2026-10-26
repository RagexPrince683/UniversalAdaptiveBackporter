package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityTadpole extends EntityWaterMob {
	private int age;
	private boolean fromBucket;
	public EntityTadpole(World world) { super(world); setSize(0.3F, 0.3F); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(6); }
	@Override protected boolean canDespawn() { return !fromBucket; }
	public void setFromBucket(int age) { fromBucket = true; this.age = Math.max(0, Math.min(24000, age)); }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate();
		if (worldObj.isRemote) return;
		if (isInWater()) {
			if (ticksExisted % 30 == 0 || isCollidedHorizontally) { motionX = (rand.nextDouble() - 0.5) * 0.12; motionZ = (rand.nextDouble() - 0.5) * 0.12; motionY = (rand.nextDouble() - 0.5) * 0.05; }
			if (++age >= 24000) {
				EntityFrog frog = new EntityFrog(worldObj); frog.copyLocationAndAnglesFrom(this); frog.chooseVariant(); frog.func_110163_bv();
				if (hasCustomNameTag()) frog.setCustomNameTag(getCustomNameTag()); if (worldObj.spawnEntityInWorld(frog)) setDead();
			}
		}
	}
	@Override public void moveEntityWithHeading(float strafe, float forward) { moveEntity(motionX, motionY, motionZ); }
	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (held != null && held.getItem() == Items.slime_ball) { if (!worldObj.isRemote) { age = Math.min(24000, age + Math.max(20, (24000 - age) / 10)); if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); worldObj.setEntityState(this, (byte) 7); } return true; }
		if (held != null && held.getItem() == Items.water_bucket && !isDead) {
			if (!worldObj.isRemote) {
				ItemStack bucket = ModItems.TADPOLE_BUCKET.newItemStack(); NBTTagCompound nbt = new NBTTagCompound(); nbt.setInteger("Age", age); bucket.setTagCompound(nbt); if (hasCustomNameTag()) bucket.setStackDisplayName(getCustomNameTag());
				if (!player.capabilities.isCreativeMode && held.stackSize == 1) player.inventory.setInventorySlotContents(player.inventory.currentItem, bucket);
				else { if (!player.capabilities.isCreativeMode) held.stackSize--; if (!player.inventory.addItemStackToInventory(bucket)) player.dropPlayerItemWithRandomChoice(bucket, false); }
				setDead();
			}
			return true;
		}
		return super.interact(player);
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Age", age); tag.setBoolean("FromBucket", fromBucket); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); age = Math.max(0, Math.min(24000, tag.getInteger("Age"))); fromBucket = tag.getBoolean("FromBucket"); }
}
