package ganymedes01.etfuturum.entities;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
public class EntityParched extends EntitySkeleton {
	private boolean checkingSun;
	public EntityParched(World world) { super(world); setCurrentItemOrArmor(0, new ItemStack(Items.bow)); tasks.taskEntries.removeIf(object -> { EntityAITasks.EntityAITaskEntry entry = (EntityAITasks.EntityAITaskEntry) object; return entry.action instanceof EntityAIRestrictSun || entry.action instanceof EntityAIFleeSun; }); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16); }
	@Override public boolean isPotionApplicable(net.minecraft.potion.PotionEffect effect) { return effect.getPotionID() != net.minecraft.potion.Potion.weakness.id && super.isPotionApplicable(effect); }
	@Override public void attackEntityWithRangedAttack(EntityLivingBase target, float distance) { if (!worldObj.isRemote) { worldObj.spawnEntityInWorld(new EntityStatusArrow(worldObj, this, target, net.minecraft.potion.Potion.weakness.id, 600)); playSound("random.bow", 1, 1); } }
	@Override public void onLivingUpdate() { checkingSun = true; try { super.onLivingUpdate(); } finally { checkingSun = false; } }
	@Override public float getBrightness(float partial) { return checkingSun ? 0 : super.getBrightness(partial); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.parched.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.parched.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.parched.death"; }

	@Override public void setCombatTask() { super.setCombatTask(); if (getHeldItem() != null && getHeldItem().getItem() == Items.bow) { for (Object object : new java.util.ArrayList<>(tasks.taskEntries)) { net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry entry = (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry) object; if (entry.action instanceof net.minecraft.entity.ai.EntityAIArrowAttack) tasks.removeTask(entry.action); } tasks.addTask(4, new net.minecraft.entity.ai.EntityAIArrowAttack(this, 1, worldObj.difficultySetting == net.minecraft.world.EnumDifficulty.HARD ? 50 : 70, 15)); } }
}
