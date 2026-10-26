package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModernSounds;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

/** Native arrow creation preserves bow enchantments and tipped/spectral ammunition. */
abstract class EntityStatusSkeleton extends EntitySkeleton {
    EntityStatusSkeleton(World world){super(world);setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND,new ItemStack(Items.BOW));}
    protected abstract Potion effect();
    protected abstract int duration();
    protected abstract String kind();
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16);}
    @Override protected EntityArrow getArrow(float distance){EntityArrow arrow=super.getArrow(distance);if(arrow instanceof EntityTippedArrow)((EntityTippedArrow)arrow).addEffect(new PotionEffect(effect(),duration()));return arrow;}
    @Override public void setCombatTask(){super.setCombatTask();if(world!=null&&!world.isRemote&&getHeldItemMainhand().getItem()==Items.BOW){for(EntityAITasks.EntityAITaskEntry entry:new java.util.ArrayList<>(tasks.taskEntries))if(entry.action instanceof EntityAIAttackRangedBow||entry.action instanceof EntityAIAttackRanged)tasks.removeTask(entry.action);tasks.addTask(4,new EntityAIAttackRanged(this,1,world.getDifficulty()==EnumDifficulty.HARD?50:70,15));}}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity."+kind()+".ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity."+kind()+".hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity."+kind()+".death");}
}
