package ganymedes01.etfuturum;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

final class SpearItem extends ItemSword {
    final float thrustDamage;
    SpearItem(ToolMaterial material){super(material);thrustDamage=3+material.getAttackDamage();}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.BOW;}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 72000;}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){player.setActiveHand(hand);return new ActionResult<>(EnumActionResult.SUCCESS,player.getHeldItem(hand));}
    @Override public void onUsingTick(ItemStack stack,EntityLivingBase living,int remaining){if(!(living instanceof EntityPlayer)||living.world.isRemote)return;int used=getMaxItemUseDuration(stack)-remaining;if(used<10||used%20!=10)return;EntityPlayer player=(EntityPlayer)living;Vec3d eye=player.getPositionEyes(1),direction=player.getLookVec(),end=eye.add(direction.scale(4.5));RayTraceResult obstruction=player.world.rayTraceBlocks(eye,end,false,true,false);double distance=obstruction==null?4.5:eye.distanceTo(obstruction.hitVec);EntityLivingBase nearest=null;
        for(Entity entity:player.world.getEntitiesWithinAABBExcludingEntity(player,player.getEntityBoundingBox().expand(direction.x*4.5,direction.y*4.5,direction.z*4.5).grow(.5))){if(!(entity instanceof EntityLivingBase)||!entity.canBeCollidedWith()||entity==player.getRidingEntity())continue;RayTraceResult hit=entity.getEntityBoundingBox().grow(.2).calculateIntercept(eye,end);if(hit==null)continue;double length=eye.distanceTo(hit.hitVec);if(length>=2&&length<distance){nearest=(EntityLivingBase)entity;distance=length;}}
        if(nearest!=null){double relative=(player.motionX-nearest.motionX)*direction.x+(player.motionY-nearest.motionY)*direction.y+(player.motionZ-nearest.motionZ)*direction.z;if(player.getRidingEntity()!=null)relative+=player.getRidingEntity().motionX*direction.x+player.getRidingEntity().motionZ*direction.z;if(nearest.attackEntityFrom(DamageSource.causePlayerDamage(player),thrustDamage+(float)MathHelper.clamp(relative*12,0,20))){stack.damageItem(1,player);nearest.addVelocity(direction.x*.3,.1,direction.z*.3);nearest.velocityChanged=true;}}
    }
}
