package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityPanda extends EntityAnimal {
	private int eatTicks;
	public EntityPanda(World world) {
		super(world); setSize(1.3F, 1.25F); tasks.addTask(0, new EntityAISwimming(this)); tasks.addTask(1, new EntityAIPanic(this, 1.2)); tasks.addTask(2, new EntityAIMate(this, 1)); tasks.addTask(3, new EntityAITempt(this, 1, ModBlocks.BAMBOO.getItem(), false)); tasks.addTask(4, new EntityAIFollowParent(this, 1)); tasks.addTask(5, new EntityAIAttackOnCollide(this, 1.2, true)); tasks.addTask(6, new EntityAIWander(this, .6)); tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6)); targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
	}
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); dataWatcher.addObject(21, (byte) 0); dataWatcher.addObject(22, (byte) 0); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.15); }
	public int getGene() { int main = dataWatcher.getWatchableObjectByte(20); return main == 4 || main == 5 ? main == dataWatcher.getWatchableObjectByte(21) ? main : 0 : main; }
	private int randomGene() { int roll = rand.nextInt(16); return roll < 5 ? 0 : roll < 7 ? 1 : roll < 9 ? 2 : roll < 11 ? 3 : roll == 11 ? 4 : roll < 15 ? 5 : 6; }
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { super.onSpawnWithEgg(data); dataWatcher.updateObject(20, (byte) randomGene()); dataWatcher.updateObject(21, (byte) randomGene()); genesChanged(); return data; }
	private void genesChanged() { getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(getGene() == 5 ? 10 : 20); setHealth(Math.min(getHealth(), getMaxHealth())); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(getGene() == 1 ? .07 : .15); }
	@Override public EntityAgeable createChild(EntityAgeable other) { EntityPanda child = new EntityPanda(worldObj), mate = (EntityPanda) other; child.dataWatcher.updateObject(20, rand.nextBoolean() ? dataWatcher.getWatchableObjectByte(20) : dataWatcher.getWatchableObjectByte(21)); child.dataWatcher.updateObject(21, rand.nextBoolean() ? mate.dataWatcher.getWatchableObjectByte(20) : mate.dataWatcher.getWatchableObjectByte(21)); if (rand.nextInt(32) == 0) child.dataWatcher.updateObject(20, (byte) randomGene()); child.genesChanged(); return child; }
	@Override public boolean isBreedingItem(ItemStack stack) { return stack != null && stack.getItem() == ModBlocks.BAMBOO.getItem(); }
	private boolean hasBamboo() { int bx = MathHelper.floor_double(posX), by = MathHelper.floor_double(posY), bz = MathHelper.floor_double(posZ), count = 0; for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) for (int dy = 0; dy <= 2; dy++) if (worldObj.blockExists(bx + dx, by + dy, bz + dz) && worldObj.getBlock(bx + dx, by + dy, bz + dz) == ModBlocks.BAMBOO.get() && ++count >= 8) return true; return false; }
	@Override public boolean interact(EntityPlayer player) { ItemStack held = player.getHeldItem(); if (held != null && (isBreedingItem(held) || held.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.cake))) { if (!worldObj.isRemote) { if (isBreedingItem(held) && !isChild() && getGrowingAge() == 0 && hasBamboo()) return super.interact(player); if (eatTicks == 0) { eatTicks = 80; dataWatcher.updateObject(22, (byte) 1); if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); } } return true; } return super.interact(player); }
	@Override public boolean attackEntityAsMob(Entity target) { boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), 6); if (hit && getGene() != 6) setAttackTarget(null); return hit; }
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (worldObj.isRemote) return; if (eatTicks > 0) { getNavigator().clearPathEntity(); if (--eatTicks == 0) { heal(1); dataWatcher.updateObject(22, (byte) 0); } } if (getGene() == 2 && worldObj.isThundering()) { getNavigator().clearPathEntity(); setAttackTarget(null); } if (isChild() && rand.nextInt(getGene() == 5 ? 500 : 6000) == 0) { worldObj.setEntityState(this, (byte) 8); if (rand.nextInt(700) == 0 && worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot")) dropItem(Items.slime_ball, 1); } if (getGene() == 3 && onGround && rand.nextInt(600) == 0) { jump(); motionX += .2 * Math.cos(rotationYaw); motionZ += .2 * Math.sin(rotationYaw); } }
	public boolean isEating() { return dataWatcher.getWatchableObjectByte(22) != 0; }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("MainGene", dataWatcher.getWatchableObjectByte(20)); tag.setInteger("HiddenGene", dataWatcher.getWatchableObjectByte(21)); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataWatcher.updateObject(20, (byte) MathHelper.clamp_int(tag.getInteger("MainGene"), 0, 6)); dataWatcher.updateObject(21, (byte) MathHelper.clamp_int(tag.getInteger("HiddenGene"), 0, 6)); genesChanged(); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.panda.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.panda.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.panda.death"; }
}
