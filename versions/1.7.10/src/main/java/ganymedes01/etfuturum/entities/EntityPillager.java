package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.projectile.EntityArrow;
import ganymedes01.etfuturum.items.ItemCrossbow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityPillager extends EntityIllager implements IRangedAttackMob {
	public EntityPillager(World world) { super(world); setCurrentItemOrArmor(0, ModItems.CROSSBOW.newItemStack()); tasks.addTask(2, new CrossbowAttack()); }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(22, -1); }
	/** Nonnegative while loading; the held stack owns the persistent charged state. */
	public int getCrossbowChargeTicks() { return dataWatcher.getWatchableObjectInt(22); }
	private void chargeTicks(int ticks) { dataWatcher.updateObject(22, ticks); }
	private void charged(boolean value) { if (!getHeldItem().hasTagCompound()) getHeldItem().setTagCompound(new NBTTagCompound()); getHeldItem().getTagCompound().setBoolean("Charged", value); }
	private class CrossbowAttack extends EntityAIBase {
		private int visibleTicks, aimTicks;
		CrossbowAttack() { setMutexBits(3); }
		@Override public boolean shouldExecute() { return getAttackTarget() != null && getAttackTarget().isEntityAlive() && getHeldItem() != null && getHeldItem().getItem() instanceof ItemCrossbow; }
		@Override public void resetTask() { visibleTicks = aimTicks = 0; chargeTicks(-1); getNavigator().clearPathEntity(); }
		@Override public void updateTask() {
			EntityLivingBase target = getAttackTarget();
			boolean visible = canEntityBeSeen(target); visibleTicks = visible ? visibleTicks + 1 : 0;
			if (getDistanceSqToEntity(target) <= 64 && visibleTicks >= 5) getNavigator().clearPathEntity(); else getNavigator().tryMoveToEntityLiving(target, 1);
			getLookHelper().setLookPositionWithEntity(target, 30, 30);
			if (!visible) { if (getCrossbowChargeTicks() >= 0) chargeTicks(-1); aimTicks = 0; return; }
			if (ItemCrossbow.charged(getHeldItem())) {
				if (++aimTicks >= 25 && getDistanceSqToEntity(target) <= 64) { attackEntityWithRangedAttack(target, 1); charged(false); aimTicks = 0; }
			} else if (getCrossbowChargeTicks() >= 0) {
				int ticks = getCrossbowChargeTicks() + 1;
				if (ticks >= ItemCrossbow.chargeTime(getHeldItem())) { charged(true); chargeTicks(-1); playSound(ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":item.crossbow.loading_end", 1, 1); }
				else chargeTicks(ticks);
			} else if (visibleTicks >= 5 && getDistanceSqToEntity(target) <= 64) { chargeTicks(0); playSound(ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":item.crossbow.loading_start", 1, 1); }
		}
	}
	@Override public void attackEntityWithRangedAttack(EntityLivingBase target, float distance) { if (!worldObj.isRemote && canEntityBeSeen(target)) { EntityArrow arrow = new EntityArrow(worldObj, this, target, 1.6F, 14 - worldObj.difficultySetting.getDifficultyId() * 4); arrow.setDamage(3); worldObj.spawnEntityInWorld(arrow); playSound("random.bow", 1, .8F); } }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.pillager.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.pillager.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.pillager.death"; }
}
