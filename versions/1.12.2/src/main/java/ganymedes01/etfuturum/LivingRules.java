package ganymedes01.etfuturum;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Maintained entity rules layered on native movement and damage ownership. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class LivingRules {
 private static final java.lang.reflect.Field JUMP=net.minecraftforge.fml.relauncher.ReflectionHelper.findField(EntityLivingBase.class,"isJumping","field_70703_bu");
 public static boolean jumping(EntityLivingBase entity){try{return JUMP.getBoolean(entity);}catch(IllegalAccessException failure){throw new IllegalStateException("Cannot read native climbing input",failure);}}
 private LivingRules(){}
 @SubscribeEvent public static void update(LivingEvent.LivingUpdateEvent event){EntityLivingBase entity=event.getEntityLiving();if(Features.enabled("mixins.stepHeightFix")&&entity.stepHeight==.5F)entity.stepHeight=.6F;
  if(!entity.world.isRemote&&entity.getClass()==EntitySquid.class&&Features.enabled("entities.enableSquidInk")&&"Nelly".equals(entity.getCustomNameTag())){Vec3d look=entity.getLookVec().normalize();entity.fallDistance=0;entity.addVelocity(look.x*.08,.08,look.z*.08);entity.motionY=.08;entity.velocityChanged=true;}
 }
 @SubscribeEvent public static void ink(LivingHurtEvent event){EntityLivingBase entity=event.getEntityLiving();if(entity.getClass()!=EntitySquid.class||!(entity.world instanceof WorldServer)||!entity.isInWater()||event.getAmount()<=0)return;boolean cloud=Features.enabled("entities.enableSquidInk"),blind=Features.enabled("tweaks.squidsBlindPlayers");if((!cloud&&!blind)||entity.world.rand.nextDouble()>=.15)return;AxisAlignedBB box=entity.getEntityBoundingBox();double x=(box.minX+box.maxX)/2,y=(box.minY+box.maxY)/2,z=(box.minZ+box.maxZ)/2;List<EntityLivingBase> targets=entity.world.getEntitiesWithinAABB(EntityLivingBase.class,new AxisAlignedBB(x-1.5,y-1.5,z-1.5,x+1.5,y+1.5,z+1.5),target->target!=entity&&target.isEntityAlive());if(targets.isEmpty())return;EntityLivingBase target=targets.get(entity.world.rand.nextInt(targets.size()));
  if(cloud)((WorldServer)entity.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE,x,y,z,5,0,0,0,.08);
  if(Features.enabled("mixins.newMobSounds"))entity.world.playSound(null,x,y,z,ModernSounds.get("entity.squid.squirt"),SoundCategory.NEUTRAL,.4F,(entity.world.rand.nextFloat()-entity.world.rand.nextFloat())*.2F+(target.isChild()?1.5F:1));
  if(blind&&target.isInWater()){PotionEffect current=target.getActivePotionEffect(MobEffects.BLINDNESS);if(current==null||current.getAmplifier()==0){long duration=20+entity.world.rand.nextInt(300)+(current==null?0L:current.getDuration());target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS,(int)Math.min(Integer.MAX_VALUE,duration)));}}
 }
}
