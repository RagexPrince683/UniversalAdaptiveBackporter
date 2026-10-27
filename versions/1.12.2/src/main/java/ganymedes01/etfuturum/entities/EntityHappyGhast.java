package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
public class EntityHappyGhast extends EntityRideableMob {
    public EntityHappyGhast(World world){super(world);setSize(4,4);setScaleForAge(false);setNoGravity(true);tasks.addTask(2,new EntityAITempt(this,1,Items.SNOWBALL,false));}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);}
    @Override public int passengerCount(){return 4;}
    @Override public double getMountedYOffset(){return height;}
    @Override protected boolean flyingMount(){return true;}
    @Override protected boolean saddleItem(ItemStack held){return Content.available("harness")&&held.getItem()==Content.item("harness");}
    @Override public boolean isBreedingItem(ItemStack stack){return false;}
    @Override public EntityAgeable createChild(EntityAgeable mate){return null;}
    @Override protected void dash(){}
    @Override protected double mountedSpeed(){return .2;}
    @Override public boolean canBeCollidedWith(){return !isDead;}
    @Override public AxisAlignedBB getCollisionBoundingBox(){return !isChild()?getEntityBoundingBox():null;}
    @Override public void fall(float distance,float damage){}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(isChild()&&held.getItem()==Items.SNOWBALL){if(!world.isRemote){setGrowingAge(Math.min(0,getGrowingAge()+Math.max(1,-getGrowingAge()/10)));consume(player,held);world.setEntityState(this,(byte)7);playSound(ModernSounds.get("entity.ghastling.ambient"),1,1);}return true;}if(!isChild()&&isSaddled()&&!isBeingRidden()&&held.getItem()==Items.SHEARS){if(!world.isRemote){ItemStack harness=new ItemStack(Content.item("harness"),1,getVariant());if(ItemDelivery.drop(this,harness,0)){flag(1,false);if(!player.capabilities.isCreativeMode)held.damageItem(1,player);}}return true;}return super.processInteract(player,hand);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(!world.isRemote){if(ticksExisted%(isWet()||posY>=128?80:400)==0)heal(1);if(!isBeingRidden()&&ticksExisted%10==0&&!world.getEntitiesWithinAABB(EntityPlayer.class,top()).isEmpty()){motionX=motionY=motionZ=0;rotationYaw=Math.round(rotationYaw/90)*90;}}}
    @Override public void setScaleForAge(boolean child){setScale(child?.2375F:1);}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get(isChild()?"entity.ghastling.ambient":"entity.happy_ghast.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get(isChild()?"entity.ghastling.hurt":"entity.happy_ghast.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get(isChild()?"entity.ghastling.death":"entity.happy_ghast.death");}
}
