package ganymedes01.etfuturum.entities;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
public class EntityBogged extends EntitySkeleton {
	
	public EntityBogged(World world) { super(world); setCurrentItemOrArmor(0, new ItemStack(Items.bow)); }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16); }
	public boolean isSheared() { return dataWatcher.getWatchableObjectByte(20) != 0; }
	@Override public void attackEntityWithRangedAttack(EntityLivingBase target, float distance) { if (!worldObj.isRemote) { worldObj.spawnEntityInWorld(new EntityStatusArrow(worldObj, this, target, net.minecraft.potion.Potion.poison.id, 100)); playSound("random.bow", 1, 1); } }
	@Override public boolean interact(EntityPlayer player) { ItemStack held = player.getHeldItem(); if (held != null && held.getItem() == Items.shears && !isSheared()) { if (!worldObj.isRemote) { dataWatcher.updateObject(20, (byte) 1); entityDropItem(new ItemStack(Blocks.red_mushroom), 0); entityDropItem(new ItemStack(Blocks.brown_mushroom), 0); held.damageItem(1, player); } return true; } return super.interact(player); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setBoolean("Sheared", isSheared()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataWatcher.updateObject(20, (byte) (tag.getBoolean("Sheared") ? 1 : 0)); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.bogged.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.bogged.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.bogged.death"; }

	@Override public void setCombatTask() { super.setCombatTask(); if (getHeldItem() != null && getHeldItem().getItem() == Items.bow) { for (Object object : new java.util.ArrayList<>(tasks.taskEntries)) { net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry entry = (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry) object; if (entry.action instanceof net.minecraft.entity.ai.EntityAIArrowAttack) tasks.removeTask(entry.action); } tasks.addTask(4, new net.minecraft.entity.ai.EntityAIArrowAttack(this, 1, worldObj.difficultySetting == net.minecraft.world.EnumDifficulty.HARD ? 50 : 70, 15)); } }
}
