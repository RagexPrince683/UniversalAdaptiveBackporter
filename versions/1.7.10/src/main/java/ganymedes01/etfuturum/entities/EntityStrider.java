package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityStrider extends EntityAnimal {
	private int boostTicks;
	public boolean boost() { if (boostTicks > 0) return false; boostTicks = 140; return true; }
	@Override public boolean isCreatureType(net.minecraft.entity.EnumCreatureType type, boolean forSpawnCount) { return type == ganymedes01.etfuturum.world.nether.NetherSpawning.STRIDERS; }
	public EntityStrider(World world) {
		super(world);
		setSize(0.9F, 1.7F);
		isImmuneToFire = true;
		getNavigator().setAvoidsWater(true);
		tasks.addTask(1, new EntityAIMate(this, 1));
		tasks.addTask(2, new EntityAITempt(this, 1.2, ModBlocks.NETHER_FUNGUS.getItem(), false));
		tasks.addTask(3, new EntityAIFollowParent(this, 1));
		tasks.addTask(4, new EntityAIWander(this, 0.7));
		tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8));
		tasks.addTask(6, new EntityAILookIdle(this));
	}
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.175);
	}
	public boolean isSaddled() { return (dataWatcher.getWatchableObjectByte(20) & 1) != 0; }
	public boolean isCold() { return (dataWatcher.getWatchableObjectByte(20) & 2) != 0; }
	@Override public EntityAgeable createChild(EntityAgeable other) { return new EntityStrider(worldObj); }
	@Override public boolean isBreedingItem(ItemStack stack) {
		return stack != null && ModBlocks.NETHER_FUNGUS.isEnabled() && stack.getItem() == ModBlocks.NETHER_FUNGUS.getItem() && stack.getItemDamage() == 1;
	}
	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (isBreedingItem(held)) return super.interact(player);
		if (!isChild() && !isSaddled() && held != null && held.getItem() == Items.saddle) {
			if (!worldObj.isRemote) {
				dataWatcher.updateObject(20, (byte) (dataWatcher.getWatchableObjectByte(20) | 1));
				if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}
			return true;
		}
		if (isSaddled() && riddenByEntity == null && !player.isSneaking()) {
			if (!worldObj.isRemote) player.mountEntity(this);
			return true;
		}
		return super.interact(player);
	}
	@Override protected void fall(float distance) { }
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		boolean lava = worldObj.getBlock(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY - 0.1), MathHelper.floor_double(posZ)).getMaterial() == Material.lava;
		if (riddenByEntity instanceof EntityPlayer && isSaddled()) {
			EntityPlayer rider = (EntityPlayer) riddenByEntity;
			if (Float.isFinite(rider.rotationYaw)) rotationYaw = rider.rotationYaw;
			renderYawOffset = rotationYaw;
			if (Float.isFinite(rider.rotationPitch)) rotationPitch = MathHelper.clamp_float(rider.rotationPitch, -90, 90) * 0.5F;
			strafe = Float.isFinite(rider.moveStrafing) ? MathHelper.clamp_float(rider.moveStrafing, -1, 1) * 0.5F : 0;
			forward = Float.isFinite(rider.moveForward) ? MathHelper.clamp_float(rider.moveForward, -1, 1) : 0;
			ItemStack held = rider.getHeldItem();
			if (held == null || held.getItem() != ganymedes01.etfuturum.ModItems.WARPED_FUNGUS_ON_A_STICK.get()) strafe = forward = 0;
			// Vanilla C0C rider input is processed by the server; no client positions are accepted here.
			if (!worldObj.isRemote) {
				setAIMoveSpeed((lava ? 0.35F : 0.175F) * (boostTicks > 0 ? 1.5F : 1));
				super.moveEntityWithHeading(strafe, forward);
			} else limbSwingAmount = 0;
		} else super.moveEntityWithHeading(strafe, forward);
		if (lava) {
			fallDistance = 0;
			int y = MathHelper.floor_double(boundingBox.minY);
			boolean submerged = worldObj.getBlock(MathHelper.floor_double(posX), y, MathHelper.floor_double(posZ)).getMaterial() == Material.lava;
			motionY = submerged ? 0.08 : Math.max(0, motionY);
			onGround = !submerged;
		}
	}
	@Override public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!worldObj.isRemote && (isWet() || isInWater()) && ticksExisted % 10 == 0) attackEntityFrom(net.minecraft.util.DamageSource.drown, 1);
		if (!worldObj.isRemote && boostTicks > 0) boostTicks--;
		if (!worldObj.isRemote && ticksExisted % 5 == 0) {
			boolean warm = worldObj.getBlock(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY - 0.1), MathHelper.floor_double(posZ)).getMaterial() == Material.lava;
			byte flags = (byte) ((dataWatcher.getWatchableObjectByte(20) & 1) | (warm ? 0 : 2));
			if (flags != dataWatcher.getWatchableObjectByte(20)) dataWatcher.updateObject(20, flags);
		}
	}
	@Override public boolean getCanSpawnHere() {
		return worldObj.provider.isHellWorld && worldObj.checkNoEntityCollision(boundingBox)
			&& worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
			&& worldObj.getBlock(MathHelper.floor_double(posX), MathHelper.floor_double(posY - 1), MathHelper.floor_double(posZ)).getMaterial() == Material.lava;
	}
	@Override protected void dropFewItems(boolean player, int looting) {
		if (!isChild()) { int count = 2 + rand.nextInt(4) + rand.nextInt(looting + 1); dropItem(Items.string, count); }
		if (isSaddled()) dropItem(Items.saddle, 1);
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("Saddle", isSaddled()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataWatcher.updateObject(20, (byte) (tag.getBoolean("Saddle") ? 1 : 0)); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.strider.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.strider.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.strider.death"; }
}
