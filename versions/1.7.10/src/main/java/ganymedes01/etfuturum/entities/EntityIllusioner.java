package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.world.World;

/** Command/egg acquisition, as in the released vanilla command-only entity. */
public class EntityIllusioner extends EntityIllager implements IRangedAttackMob {
	public EntityIllusioner(World world) { super(world); setCurrentItemOrArmor(0, new ItemStack(Items.bow)); tasks.addTask(2, new EntityAIArrowAttack(this, .5, 20, 15)); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(32); }
	@Override public void attackEntityWithRangedAttack(EntityLivingBase target, float distance) { if (!worldObj.isRemote) { EntityArrow arrow = new EntityArrow(worldObj, this, target, 1.6F, 14 - worldObj.difficultySetting.getDifficultyId() * 4); arrow.setDamage(2); worldObj.spawnEntityInWorld(arrow); } }
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote && getAttackTarget() != null && ticksExisted % 1200 == 0) { addPotionEffect(new PotionEffect(Potion.invisibility.id, 1200, 0)); if (worldObj.difficultySetting == net.minecraft.world.EnumDifficulty.HARD) getAttackTarget().addPotionEffect(new PotionEffect(Potion.blindness.id, 400, 0)); } }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.illusioner.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.illusioner.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.illusioner.death"; }
}
