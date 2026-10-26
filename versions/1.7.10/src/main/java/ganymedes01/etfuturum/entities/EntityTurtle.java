package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityTurtle extends EntityAquaticAnimal {
	private int homeX, homeY, homeZ, diggingTicks;
	private boolean hasHome, hadChildAge;
	public EntityTurtle(World world) {
		super(world); setSize(1.2F, .4F);
		getNavigator().setAvoidsWater(false);
		tasks.addTask(1, new EntityAIPanic(this, 1.2)); tasks.addTask(2, new EntityAIMate(this, 1));
		tasks.addTask(3, new EntityAITempt(this, 1.1, ModBlocks.SEAGRASS.getItem(), false));
		tasks.addTask(5, new EntityAIWander(this, .8)); tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6));
	}
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.25); }
	@Override public boolean canBreatheUnderwater() { return true; }
	public boolean hasEgg() { return dataWatcher.getWatchableObjectByte(20) != 0; }
	@Override protected String getLivingSound() { return isInWater() ? null : ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.turtle.ambient_land"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.turtle.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.turtle.death"; }
	public void setHome(int x, int y, int z) { homeX = x; homeY = y; homeZ = z; hasHome = true; }
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { setHome(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)); return super.onSpawnWithEgg(data); }
	@Override public boolean isBreedingItem(ItemStack stack) { return !hasEgg() && stack != null && stack.getItem() == ModBlocks.SEAGRASS.getItem(); }
	@Override public EntityAgeable createChild(EntityAgeable other) { dataWatcher.updateObject(20, (byte) 1); setGrowingAge(6000); other.setGrowingAge(6000); resetInLove(); ((net.minecraft.entity.passive.EntityAnimal) other).resetInLove(); return null; }
	@Override public boolean getCanSpawnHere() { return worldObj.provider.dimensionId == 0 && posY >= 60 && posY <= 67 && worldObj.getBlock(MathHelper.floor_double(posX), MathHelper.floor_double(posY) - 1, MathHelper.floor_double(posZ)) == Blocks.sand && worldObj.getBlockLightValue(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)) > 8 && worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty() && !worldObj.isAnyLiquid(boundingBox); }
	@Override protected double swimAcceleration() { return isChild() ? .006 : .015; }
	@Override protected boolean swimDestination() {
		if (hasEgg() && hasHome) { swimX = homeX + .5; swimY = homeY; swimZ = homeZ + .5; return true; }
		if (isChild()) {
			for (int i = 0; i < 4; i++) {
				int x = MathHelper.floor_double(posX) + rand.nextInt(9) - 4, y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ) + rand.nextInt(9) - 4;
				if (waterAt(x, y, z) && waterAt(x, y - 1, z)) { swimX = x + .5; swimY = y - .5; swimZ = z + .5; return true; }
			}
		}
		return false;
	}
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (!hasHome) setHome(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
		if (hadChildAge && !isChild() && worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot")) ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, ModItems.TURTLE_SCUTE.newItemStack(), .1F);
		hadChildAge = isChild();
		int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
		if (hasEgg() && !isInWater()) {
			if (Math.abs(x - homeX) < 9 && Math.abs(z - homeZ) < 9 && worldObj.getBlock(x, y - 1, z) == Blocks.sand && worldObj.isAirBlock(x, y, z)) {
				getNavigator().clearPathEntity();
				if (++diggingTicks >= 200 && ModBlocks.TURTLE_EGG.isEnabled() && worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing") && worldObj.setBlock(x, y, z, ModBlocks.TURTLE_EGG.get(), rand.nextInt(4) << 2, 3)) { dataWatcher.updateObject(20, (byte) 0); diggingTicks = 0; setGrowingAge(600); }
			} else if (ticksExisted % 40 == 0 && worldObj.blockExists(homeX, homeY, homeZ)) getNavigator().tryMoveToXYZ(homeX + .5, homeY, homeZ + .5, 1);
		} else {
			diggingTicks = 0;
			if (!isInWater() && ticksExisted % 100 == 0 && !hasEgg()) {
				for (int attempt = 0; attempt < 16; attempt++) {
					int wx = x + rand.nextInt(17) - 8, wz = z + rand.nextInt(17) - 8;
					if (waterAt(wx, y, wz)) { getNavigator().tryMoveToXYZ(wx + .5, y, wz + .5, 1); break; }
				}
			}
		}
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("HasEgg", hasEgg()); tag.setInteger("HomePosX", homeX); tag.setInteger("HomePosY", homeY); tag.setInteger("HomePosZ", homeZ); tag.setBoolean("HasHome", hasHome); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataWatcher.updateObject(20, (byte) (tag.getBoolean("HasEgg") ? 1 : 0)); hasHome = tag.getBoolean("HasHome"); homeX = tag.getInteger("HomePosX"); homeY = MathHelper.clamp_int(tag.getInteger("HomePosY"), 1, 255); homeZ = tag.getInteger("HomePosZ"); hadChildAge = isChild(); }
}
