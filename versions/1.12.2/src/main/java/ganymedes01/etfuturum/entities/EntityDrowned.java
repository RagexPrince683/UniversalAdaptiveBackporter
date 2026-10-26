package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.Content;
import ganymedes01.etfuturum.ModEnchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.network.datasync.*;
import net.minecraft.world.*;

public class EntityDrowned extends EntityZombie implements IRangedAttackMob {
	private int attackTicks;
	private boolean carriesShell;
	private double swimX, swimY, swimZ;
	private int swimTicks;
	public EntityDrowned(World world) {
		super(world); tasks.taskEntries.clear(); targetTasks.taskEntries.clear();
		((net.minecraft.pathfinding.PathNavigateGround) getNavigator()).setCanSwim(true); setBreakDoorsAItask(false);
		tasks.addTask(2, new EntityAIAttackMelee(this, 1, false) { @Override public boolean shouldExecute() { return !isInWater() && !hasTrident() && super.shouldExecute(); } });
		tasks.addTask(2, new EntityAIAttackRanged(this, 1, 40, 10) { @Override public boolean shouldExecute() { return hasTrident() && super.shouldExecute(); } });
		tasks.addTask(5, new EntityAIWander(this, 1)); tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 20, true, false, entity -> entity != null && (!world.isDaytime() || entity.isInWater())));
		targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityVillager>(this, EntityVillager.class, true));
	}
	public boolean hasTrident() { return getHeldItemMainhand().getItem() == Content.item("trident"); }
	@Override public boolean isCreatureType(EnumCreatureType type, boolean count) { return type == ganymedes01.etfuturum.MobRegistry.DROWNED; }
	@Override public boolean canBreatheUnderwater() { return true; }
	@Override public boolean getCanSpawnHere() {
		int x = MathHelper.floor(posX), y = MathHelper.floor(posY), z = MathHelper.floor(posZ);
		net.minecraft.world.biome.Biome biome = world.getBiome(new BlockPos(x, y, z));
		boolean river = net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.RIVER);
		boolean ocean = net.minecraftforge.common.BiomeDictionary.hasType(biome, net.minecraftforge.common.BiomeDictionary.Type.OCEAN);
		// Released Java rules: rivers are less rare, ocean spawns stay five blocks below sea level.
		// Darkness is evaluated here only; conversion, eggs and commands never call this natural predicate.
		return ganymedes01.etfuturum.MobRegistry.enabled(getClass()) && world.provider.getDimension() == 0 && world.getDifficulty() != EnumDifficulty.PEACEFUL && (river || ocean) && y < (river ? 63 : 58)
				&& world.isBlockLoaded(new BlockPos(x, y + 1, z)) && world.getBlockState(new BlockPos(x, y, z)).getMaterial() == net.minecraft.block.material.Material.WATER
				&& world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial() == net.minecraft.block.material.Material.WATER
				&& world.getLightFor(EnumSkyBlock.BLOCK, new BlockPos(x, y, z)) == 0 && world.getLight(new BlockPos(x, y, z)) <= 7
				&& (!world.isDaytime() || y < 58 || world.getLightFor(EnumSkyBlock.SKY, new BlockPos(x, y, z)) == 0)
				&& rand.nextInt(river ? 15 : 40) == 0 && world.checkNoEntityCollision(getEntityBoundingBox()) && world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty();
	}
	@Override public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data) {
        super.onInitialSpawn(difficulty, data);
        if (rand.nextFloat() < .0625F && Content.available("trident")) { setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, Content.stack("trident")); inventoryHandsDropChances[0] = .085F; }
        else setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
        carriesShell = rand.nextFloat() < .03F; return data;
    }
    @Override public void setSwingingArms(boolean swinging) { }
	@Override public void attackEntityWithRangedAttack(EntityLivingBase target, float distance) {
		if (world.isRemote || !hasTrident() || !canEntityBeSeen(target)) return;
		EntityTrident trident = new EntityTrident(world, this, null);
		double dx = target.posX - posX, dz = target.posZ - posZ, dy = target.posY + target.getEyeHeight() - trident.posY;
		trident.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * .2, dz, 1.6F, 14 - world.getDifficulty().getId() * 4);
		world.spawnEntity(trident);
	}
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!world.isRemote && attackTicks > 0) --attackTicks; }
	@Override public void travel(float strafe, float vertical, float forward) {
		if (!isInWater()) { super.travel(strafe, vertical, forward); return; }
		if (world.isRemote) return;
		EntityLivingBase target = getAttackTarget();
		if (target != null && (!target.isEntityAlive() || getDistanceSq(target) > 256 || world.isDaytime() && !target.isInWater())) { setAttackTarget(null); target = null; }
		if (target != null) {
			swimX = target.posX; swimY = target.posY + target.height * .5; swimZ = target.posZ;
			if (!hasTrident() && attackTicks == 0 && getDistanceSq(target) < 3 && canEntityBeSeen(target)) { attackEntityAsMob(target); attackTicks = 20; }
		} else if (--swimTicks <= 0 || collidedHorizontally) { swimTicks = 40; swimX = posX + (rand.nextDouble() - .5) * 8; swimY = posY + (rand.nextDouble() - .5) * 4; swimZ = posZ + (rand.nextDouble() - .5) * 8; }
		double dx = swimX - posX, dy = swimY - posY, dz = swimZ - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length > .5 && world.isBlockLoaded(new BlockPos(swimX, swimY, swimZ)) && world.rayTraceBlocks(new Vec3d(posX, posY + 1, posZ), new Vec3d(swimX, swimY, swimZ), false, true, false) == null) { motionX += dx / length * .015; motionY += dy / length * .015; motionZ += dz / length * .015; rotationYaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90; renderYawOffset = rotationYaw; }
		else if (collidedHorizontally) motionY += .04;
		move(MoverType.SELF, motionX, motionY, motionZ); motionX *= .9; motionY *= .9; motionZ *= .9;
	}
	@Override protected void dropLoot(boolean player, int looting, DamageSource source) { super.dropLoot(player, looting, source); if (carriesShell && Content.available("nautilus_shell")) entityDropItem(Content.stack("nautilus_shell"), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("HasNautilusShell", carriesShell); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); carriesShell = tag.getBoolean("HasNautilusShell"); }
}
