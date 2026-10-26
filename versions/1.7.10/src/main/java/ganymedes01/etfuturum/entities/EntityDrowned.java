package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.*;

public class EntityDrowned extends EntityZombie implements IRangedAttackMob {
	private int attackTicks;
	private boolean carriesShell;
	private double swimX, swimY, swimZ;
	private int swimTicks;
	public EntityDrowned(World world) {
		super(world); tasks.taskEntries.clear(); targetTasks.taskEntries.clear();
		getNavigator().setAvoidsWater(false); func_146070_a(false);
		tasks.addTask(2, new EntityAIAttackOnCollide(this, 1, false) { @Override public boolean shouldExecute() { return !isInWater() && !hasTrident() && super.shouldExecute(); } });
		tasks.addTask(2, new EntityAIArrowAttack(this, 1, 40, 10) { @Override public boolean shouldExecute() { return hasTrident() && super.shouldExecute(); } });
		tasks.addTask(5, new EntityAIWander(this, 1)); tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 20, true, false, entity -> !worldObj.isDaytime() || entity.isInWater()));
		targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, 20, true));
	}
	public boolean hasTrident() { return getHeldItem() != null && getHeldItem().getItem() == ModItems.TRIDENT.get(); }
	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.drowned." + (isInWater() ? "ambient_water" : "ambient"); }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.drowned.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.drowned.death"; }
	@Override public boolean isCreatureType(EnumCreatureType type, boolean count) { return type == ganymedes01.etfuturum.world.MobSpawning.DROWNED; }
	@Override public boolean canBreatheUnderwater() { return true; }
	@Override public boolean getCanSpawnHere() {
		int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
		net.minecraft.world.biome.BiomeGenBase biome = worldObj.getBiomeGenForCoords(x, z);
		boolean river = net.minecraftforge.common.BiomeDictionary.isBiomeOfType(biome, net.minecraftforge.common.BiomeDictionary.Type.RIVER);
		boolean ocean = net.minecraftforge.common.BiomeDictionary.isBiomeOfType(biome, net.minecraftforge.common.BiomeDictionary.Type.OCEAN);
		// Released Java rules: rivers are less rare, ocean spawns stay five blocks below sea level.
		// Darkness is evaluated here only; conversion, eggs and commands never call this natural predicate.
		return worldObj.provider.dimensionId == 0 && worldObj.difficultySetting != EnumDifficulty.PEACEFUL && (river || ocean) && y < (river ? 63 : 58)
				&& worldObj.blockExists(x, y + 1, z) && worldObj.getBlock(x, y, z).getMaterial() == net.minecraft.block.material.Material.water
				&& worldObj.getBlock(x, y + 1, z).getMaterial() == net.minecraft.block.material.Material.water
				&& worldObj.getSavedLightValue(EnumSkyBlock.Block, x, y, z) == 0 && worldObj.getBlockLightValue(x, y, z) <= 7
				&& (!worldObj.isDaytime() || y < 58 || worldObj.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) == 0)
				&& rand.nextInt(river ? 15 : 40) == 0 && worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
	}
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { super.onSpawnWithEgg(data); setVillager(false); if (rand.nextFloat() < .0625F) { setCurrentItemOrArmor(0, ModItems.TRIDENT.newItemStack()); equipmentDropChances[0] = .085F; } else setCurrentItemOrArmor(0, null); carriesShell = rand.nextFloat() < .03F; return data; }
	@Override public void attackEntityWithRangedAttack(EntityLivingBase target, float distance) {
		if (worldObj.isRemote || !hasTrident() || !canEntityBeSeen(target)) return;
		EntityTrident trident = new EntityTrident(worldObj, this, null);
		double dx = target.posX - posX, dz = target.posZ - posZ, dy = target.posY + target.getEyeHeight() - trident.posY;
		trident.setThrowableHeading(dx, dy + Math.sqrt(dx * dx + dz * dz) * .2, dz, 1.6F, 14 - worldObj.difficultySetting.getDifficultyId() * 4);
		worldObj.spawnEntityInWorld(trident);
	}
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote && attackTicks > 0) --attackTicks; }
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		if (!isInWater()) { super.moveEntityWithHeading(strafe, forward); return; }
		if (worldObj.isRemote) return;
		EntityLivingBase target = getAttackTarget();
		if (target != null && (!target.isEntityAlive() || getDistanceSqToEntity(target) > 256 || worldObj.isDaytime() && !target.isInWater())) { setAttackTarget(null); target = null; }
		if (target != null) {
			swimX = target.posX; swimY = target.posY + target.height * .5; swimZ = target.posZ;
			if (!hasTrident() && attackTicks == 0 && getDistanceSqToEntity(target) < 3 && canEntityBeSeen(target)) { attackEntityAsMob(target); attackTicks = 20; }
		} else if (--swimTicks <= 0 || isCollidedHorizontally) { swimTicks = 40; swimX = posX + (rand.nextDouble() - .5) * 8; swimY = posY + (rand.nextDouble() - .5) * 4; swimZ = posZ + (rand.nextDouble() - .5) * 8; }
		double dx = swimX - posX, dy = swimY - posY, dz = swimZ - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length > .5 && worldObj.blockExists(MathHelper.floor_double(swimX), MathHelper.floor_double(swimY), MathHelper.floor_double(swimZ)) && worldObj.func_147447_a(Vec3.createVectorHelper(posX, posY + 1, posZ), Vec3.createVectorHelper(swimX, swimY, swimZ), false, true, false) == null) { motionX += dx / length * .015; motionY += dy / length * .015; motionZ += dz / length * .015; rotationYaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90; renderYawOffset = rotationYaw; }
		else if (isCollidedHorizontally) motionY += .04;
		moveEntity(motionX, motionY, motionZ); motionX *= .9; motionY *= .9; motionZ *= .9;
	}
	@Override protected void dropFewItems(boolean player, int looting) { super.dropFewItems(player, looting); if (carriesShell) ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, ModItems.NAUTILUS_SHELL.newItemStack(), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("HasNautilusShell", carriesShell); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); carriesShell = tag.getBoolean("HasNautilusShell"); }
	@Override protected void dropRareDrop(int bonus) { if (ModItems.COPPER_INGOT.isEnabled()) ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, ModItems.COPPER_INGOT.newItemStack(), 0); }
}
