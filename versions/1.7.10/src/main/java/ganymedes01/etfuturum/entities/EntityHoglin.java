package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityHoglin extends EntityAnimal implements IMob {
	private int unsafeTicks;
	private boolean repelled;

	public EntityHoglin(World world) {
		super(world);
		setSize(1.4F, 1.4F);
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, new EntityAIAttackOnCollide(this, 1, true));
		tasks.addTask(2, new EntityAIMate(this, 1));
		tasks.addTask(3, new EntityAITempt(this, 1, ModBlocks.NETHER_FUNGUS.getItem(), false));
		tasks.addTask(4, new EntityAIFollowParent(this, 1));
		tasks.addTask(5, new EntityAIWander(this, 0.7));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 20, true) {
			@Override public boolean shouldExecute() {
				return !isChild() && !isInLove() && !repelled && super.shouldExecute();
			}
		});
	}
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3);
		getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.6);
	}
	@Override public EntityAgeable createChild(EntityAgeable other) { return new EntityHoglin(worldObj); }
	@Override public boolean isBreedingItem(ItemStack stack) {
		return stack != null && ModBlocks.NETHER_FUNGUS.isEnabled() && stack.getItem() == ModBlocks.NETHER_FUNGUS.getItem() && stack.getItemDamage() == 0;
	}
	@Override public boolean attackEntityAsMob(Entity target) {
		boolean hit = target.attackEntityFrom(net.minecraft.util.DamageSource.causeMobDamage(this), isChild() ? 0.5F : 3 + rand.nextInt(6));
		if (hit && !isChild()) target.addVelocity(-MathHelper.sin(rotationYaw * (float)Math.PI / 180) * 0.5, 0.25, MathHelper.cos(rotationYaw * (float)Math.PI / 180) * 0.5);
		return hit;
	}
	@Override public void onLivingUpdate() {
		super.onLivingUpdate();
		if (worldObj.isRemote) return;
		if (ticksExisted % 40 == 0) {
			repelled = false;
			int bx = MathHelper.floor_double(posX), by = MathHelper.floor_double(posY), bz = MathHelper.floor_double(posZ);
			search: for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) for (int dy = -1; dy <= 1; dy++) {
				int x = bx + dx, y = by + dy, z = bz + dz;
				if (!worldObj.blockExists(x, y, z)) continue;
				if (worldObj.getBlock(x, y, z) == ModBlocks.NETHER_FUNGUS.get() && worldObj.getBlockMetadata(x, y, z) == 1) {
					repelled = true; setAttackTarget(null);
					getNavigator().tryMoveToXYZ(posX + (dx <= 0 ? 4 : -4), posY, posZ + (dz <= 0 ? 4 : -4), 1.3);
					break search;
				}
			}
		}
		if (!isChild() && !repelled && ticksExisted % 100 == 0) { int adults = 0; EntityPiglin closest = null; for (Object object : worldObj.getEntitiesWithinAABB(EntityPiglin.class, boundingBox.expand(12, 6, 12))) { EntityPiglin piglin = (EntityPiglin) object; if (!piglin.isChild()) { adults++; closest = piglin; } } int herd = worldObj.getEntitiesWithinAABB(EntityHoglin.class, boundingBox.expand(12, 6, 12)).size(); if (closest != null && adults > herd) { setAttackTarget(null); getNavigator().tryMoveToXYZ(posX + (posX - closest.posX), posY, posZ + (posZ - closest.posZ), 1.3); } }
		if (repelled) setAttackTarget(null);
		unsafeTicks = worldObj.provider.isHellWorld ? 0 : unsafeTicks + 1;
		if (unsafeTicks >= 300) {
			EntityZoglin zoglin = new EntityZoglin(worldObj);
			zoglin.copyLocationAndAnglesFrom(this);
			zoglin.setGrowingAge(getGrowingAge());
			if (hasCustomNameTag()) zoglin.setCustomNameTag(getCustomNameTag());
			if (worldObj.spawnEntityInWorld(zoglin)) setDead();
		}
	}
	@Override public boolean getCanSpawnHere() {
		return worldObj.provider.isHellWorld && worldObj.difficultySetting != EnumDifficulty.PEACEFUL
			&& worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
			&& !worldObj.isAnyLiquid(boundingBox);
	}
	@Override protected void dropFewItems(boolean player, int looting) {
		if (!isChild()) {
			dropItem(isBurning() ? Items.cooked_porkchop : Items.porkchop, 2 + rand.nextInt(3) + rand.nextInt(looting + 1));
			int leather = rand.nextInt(2) + rand.nextInt(looting + 1);
			if (leather > 0) dropItem(Items.leather, leather);
		}
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("UnsafeTicks", unsafeTicks); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); unsafeTicks = Math.max(0, Math.min(300, tag.getInteger("UnsafeTicks"))); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.hoglin.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.hoglin.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.hoglin.death"; }
}
