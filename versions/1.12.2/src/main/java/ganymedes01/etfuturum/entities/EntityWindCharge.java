package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.ModernSounds;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
public class EntityWindCharge extends EntityThrowable {
    public EntityWindCharge(World world){super(world);}
    public EntityWindCharge(World world,EntityLivingBase shooter){super(world,shooter);}
    @Override protected float getGravityVelocity(){return 0;}
    @Override public void onUpdate(){super.onUpdate();if(!world.isRemote&&ticksExisted>100)setDead();}
    @Override protected void onImpact(RayTraceResult hit){if(world.isRemote)return;if(hit.entityHit!=null)hit.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this,getThrower()),1);for(EntityLivingBase entity:world.getEntitiesWithinAABB(EntityLivingBase.class,getEntityBoundingBox().grow(4))){if(entity instanceof EntityBreeze)continue;double dx=entity.posX-posX,dy=entity.posY+entity.height*.5-posY,dz=entity.posZ-posZ,distance=Math.sqrt(dx*dx+dy*dy+dz*dz);if(distance>=4||world.rayTraceBlocks(new Vec3d(posX,posY,posZ),new Vec3d(entity.posX,entity.posY+entity.height*.5,entity.posZ))!=null)continue;double strength=(1-distance/4)*(getThrower() instanceof EntityBreeze?.8:1.2),divisor=Math.max(.2,distance);entity.addVelocity(dx/divisor*strength,Math.max(.2,dy/divisor*strength),dz/divisor*strength);entity.fallDistance=0;entity.velocityChanged=true;entity.getEntityData().setDouble("EFRWindLaunchY",entity.posY);entity.getEntityData().setLong("EFRWindLaunchUntil",world.getTotalWorldTime()+200);}
        if(hit.typeOfHit==RayTraceResult.Type.BLOCK&&world.isBlockLoaded(hit.getBlockPos())){BlockPos pos=hit.getBlockPos();IBlockState state=world.getBlockState(pos);Block block=state.getBlock();EntityLivingBase owner=getThrower();boolean permitted=owner instanceof EntityPlayer?world.isBlockModifiable((EntityPlayer)owner,pos)&&((EntityPlayer)owner).canPlayerEdit(pos,hit.sideHit,((EntityPlayer)owner).getHeldItemMainhand()):net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,this);
            if(permitted){if(block instanceof BlockTrapDoor)world.setBlockState(pos,state.cycleProperty(BlockTrapDoor.OPEN),3);else if(block instanceof BlockDoor)((BlockDoor)block).toggleDoor(world,pos,!((BlockDoor)block).isOpen(world,pos));else if(owner instanceof EntityPlayer&&(block instanceof BlockLever||block instanceof BlockButton))block.onBlockActivated(world,pos,state,(EntityPlayer)owner,EnumHand.MAIN_HAND,hit.sideHit,0,0,0);else if(block instanceof BlockButton){world.setBlockState(pos,state.withProperty(BlockButton.POWERED,true),3);world.notifyNeighborsOfStateChange(pos,block,false);world.notifyNeighborsOfStateChange(pos.offset(state.getValue(BlockButton.FACING).getOpposite()),block,false);world.scheduleUpdate(pos,block,block.tickRate(world));}else if(block instanceof BlockLever){world.setBlockState(pos,state.cycleProperty(BlockLever.POWERED),3);world.notifyNeighborsOfStateChange(pos,block,false);world.notifyNeighborsOfStateChange(pos.offset(state.getValue(BlockLever.FACING).getFacing().getOpposite()),block,false);}}}
        world.playSound(null,posX,posY,posZ,ModernSounds.get("entity.wind_charge.wind_burst"),SoundCategory.PLAYERS,1,1);world.playEvent(2004,new BlockPos(this),0);setDead();}
}
