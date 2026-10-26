package ganymedes01.etfuturum.entities;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.world.World;
public class EntityCamel extends EntityRideableMob {
	public EntityCamel(World world) { super(world); setSize(1.7F, 2.375F); stepHeight = 1.5F; tasks.addTask(2, new EntityAITempt(this, 1, Item.getItemFromBlock(Blocks.cactus), false)); }
	@Override public boolean getCanSpawnHere() { int x = net.minecraft.util.MathHelper.floor_double(posX), y = net.minecraft.util.MathHelper.floor_double(posY), z = net.minecraft.util.MathHelper.floor_double(posZ); return worldObj.provider.dimensionId == 0 && y > 60 && worldObj.getBlock(x, y - 1, z).getMaterial().isSolid() && worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty() && !worldObj.isAnyLiquid(boundingBox) && (!(this instanceof EntityCamelHusk) || !worldObj.isDaytime() && worldObj.getBlockLightValue(x, y, z) <= 7 && worldObj.difficultySetting != net.minecraft.world.EnumDifficulty.PEACEFUL); }
	public boolean isSitting() { return flag(2); }
	@Override public int passengerCount() { return 2; }
	@Override public double getMountedYOffset() { return flag(2) ? .9 : 2.2; }
	@Override public EntityAgeable createChild(EntityAgeable mate) { return new EntityCamel(worldObj); }
	@Override public boolean isBreedingItem(ItemStack stack) { return stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.cactus); }
	@Override public boolean interact(EntityPlayer player) { if (player.isSneaking() && player.getHeldItem() == null && riddenByEntity == null) { if (!worldObj.isRemote) flag(2, !flag(2)); return true; } flag(2, false); return super.interact(player); }
	@Override public void moveEntityWithHeading(float strafe, float forward) { if (flag(2) && riddenByEntity == null) { moveEntity(0, motionY, 0); return; } super.moveEntityWithHeading(strafe, forward); }
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote && riddenByEntity == null && getNavigator().noPath() && ticksExisted % 200 == 0 && rand.nextInt(3) == 0) flag(2, !flag(2)); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.camel.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.camel.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.camel.death"; }
}
