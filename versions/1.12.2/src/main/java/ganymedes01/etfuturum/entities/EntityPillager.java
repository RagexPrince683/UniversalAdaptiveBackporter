package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
public class EntityPillager extends EntityIllager implements IRangedAttackMob {
    private static final DataParameter<Integer> CHARGE=EntityDataManager.createKey(EntityPillager.class,DataSerializers.VARINT);
    public EntityPillager(World world){super(world);setItemStackToSlot(EntityEquipmentSlot.MAINHAND,Content.stack("crossbow"));tasks.addTask(2,new CrossbowAttack());}
    @Override protected void entityInit(){super.entityInit();dataManager.register(CHARGE,-1);}
    public int getCrossbowChargeTicks(){return dataManager.get(CHARGE);}
    private void chargeTicks(int ticks){dataManager.set(CHARGE,ticks);}
    private void charge(){ItemStack held=getHeldItemMainhand();if(!held.hasTagCompound())held.setTagCompound(new NBTTagCompound());held.getTagCompound().setBoolean("Charged",true);held.getTagCompound().setBoolean("CreativeCharge",true);held.getTagCompound().setTag("ChargedProjectile",new ItemStack(Items.ARROW).writeToNBT(new NBTTagCompound()));}
    private final class CrossbowAttack extends EntityAIBase {
        private int visibleTicks,aimTicks;
        CrossbowAttack(){setMutexBits(3);}
        @Override public boolean shouldExecute(){return getAttackTarget()!=null&&getAttackTarget().isEntityAlive()&&getHeldItemMainhand().getItem() instanceof CrossbowItem;}
        @Override public void resetTask(){visibleTicks=aimTicks=0;chargeTicks(-1);getNavigator().clearPath();}
        @Override public void updateTask(){EntityLivingBase target=getAttackTarget();if(target==null)return;boolean visible=canEntityBeSeen(target);visibleTicks=visible?visibleTicks+1:0;if(getDistanceSq(target)<=64&&visibleTicks>=5)getNavigator().clearPath();else getNavigator().tryMoveToEntityLiving(target,1);getLookHelper().setLookPositionWithEntity(target,30,30);if(!visible){chargeTicks(-1);aimTicks=0;return;}ItemStack held=getHeldItemMainhand();if(CrossbowItem.charged(held)){if(++aimTicks>=25&&getDistanceSq(target)<=64){attackEntityWithRangedAttack(target,1);aimTicks=0;}}else if(getCrossbowChargeTicks()>=0){int ticks=getCrossbowChargeTicks()+1;if(ticks>=CrossbowItem.chargeTime(held)){charge();chargeTicks(-1);playSound(ModernSounds.get("item.crossbow.loading_end"),1,1);}else chargeTicks(ticks);}else if(visibleTicks>=5&&getDistanceSq(target)<=64){chargeTicks(0);playSound(ModernSounds.get("item.crossbow.loading_start"),1,1);}}
    }
    @Override public void attackEntityWithRangedAttack(EntityLivingBase target,float distance){if(!world.isRemote&&canEntityBeSeen(target))CrossbowItem.fireAt(world,this,getHeldItemMainhand(),target);}
    @Override public void setSwingingArms(boolean swinging){}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.pillager.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.pillager.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.pillager.death");}
}
