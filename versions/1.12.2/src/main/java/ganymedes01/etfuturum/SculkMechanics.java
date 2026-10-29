package ganymedes01.etfuturum;

import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Native events feed the loaded sensor index; catalyst XP remains owned by Forge. */
public final class SculkMechanics {
    private SculkMechanics(){}
    public static void initialize(){net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new SculkMechanics());}
    public static void emit(World world,double x,double y,double z,int frequency,EntityPlayer player){if(world.isRemote||!Features.enabled("experiments.enableSculk"))return;SculkSensorTile.emit(world,x,y,z,frequency,player);}
    @SubscribeEvent public void step(TickEvent.PlayerTickEvent event){EntityPlayer player=event.player;if(event.phase!=TickEvent.Phase.END||player.world.isRemote||player.ticksExisted%10!=0||player.capabilities.disableDamage||player.isSneaking()||player.getDistanceSq(player.prevPosX,player.prevPosY,player.prevPosZ)<.0001)return;BlockPos foot=new BlockPos(player.posX,player.getEntityBoundingBox().minY-.01,player.posZ);if(!player.world.isBlockLoaded(foot)||player.world.getBlockState(foot).getBlock()==Blocks.WOOL||player.world.getBlockState(foot).getBlock()==Blocks.CARPET)return;emit(player.world,player.posX,player.posY,player.posZ,1,player);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void place(BlockEvent.PlaceEvent event){emit(event.getWorld(),event.getPos().getX()+.5,event.getPos().getY()+.5,event.getPos().getZ()+.5,13,event.getPlayer());}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void broken(BlockEvent.BreakEvent event){emit(event.getWorld(),event.getPos().getX()+.5,event.getPos().getY()+.5,event.getPos().getZ()+.5,12,event.getPlayer());}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void interact(PlayerInteractEvent.RightClickBlock event){net.minecraft.block.Block block=event.getWorld().getBlockState(event.getPos()).getBlock();if(!(event.getWorld().getTileEntity(event.getPos()) instanceof net.minecraft.inventory.IInventory)&&!(block instanceof net.minecraft.block.BlockDoor)&&!(block instanceof net.minecraft.block.BlockTrapDoor)&&!(block instanceof net.minecraft.block.BlockFenceGate)&&!(block instanceof net.minecraft.block.BlockLever)&&!(block instanceof net.minecraft.block.BlockButton))return;emit(event.getWorld(),event.getPos().getX()+.5,event.getPos().getY()+.5,event.getPos().getZ()+.5,10,event.getEntityPlayer());}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void hurt(LivingHurtEvent event){EntityLivingBase entity=event.getEntityLiving();emit(entity.world,entity.posX,entity.posY,entity.posZ,7,entity instanceof EntityPlayer?(EntityPlayer)entity:null);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void death(LivingDeathEvent event){EntityLivingBase entity=event.getEntityLiving();emit(entity.world,entity.posX,entity.posY,entity.posZ,15,entity instanceof EntityPlayer?(EntityPlayer)entity:null);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void experience(LivingExperienceDropEvent event){EntityLivingBase entity=event.getEntityLiving();if(entity instanceof EntityPlayer||entity.world.isRemote||event.getDroppedExperience()<=0||!Content.available("sculk_catalyst"))return;SculkCatalystTile nearest=SculkCatalystTile.nearest(entity.world,entity.posX,entity.posY,entity.posZ);if(nearest!=null)event.setDroppedExperience(event.getDroppedExperience()-nearest.accept(event.getDroppedExperience()));}
    @SubscribeEvent public void finish(LivingEntityUseItemEvent.Finish event){EntityLivingBase entity=event.getEntityLiving();net.minecraft.item.EnumAction action=event.getItem().getItemUseAction();if(action==net.minecraft.item.EnumAction.EAT||action==net.minecraft.item.EnumAction.DRINK)emit(entity.world,entity.posX,entity.posY,entity.posZ,8,entity instanceof EntityPlayer?(EntityPlayer)entity:null);}
    @SubscribeEvent public void projectile(net.minecraftforge.event.entity.EntityJoinWorldEvent event){Entity entity=event.getEntity();EntityLivingBase owner=entity instanceof net.minecraft.entity.projectile.EntityArrow?((net.minecraft.entity.projectile.EntityArrow)entity).shootingEntity instanceof EntityLivingBase?(EntityLivingBase)((net.minecraft.entity.projectile.EntityArrow)entity).shootingEntity:null:entity instanceof net.minecraft.entity.projectile.EntityThrowable?((net.minecraft.entity.projectile.EntityThrowable)entity).getThrower():null;if(entity instanceof net.minecraft.entity.projectile.EntityArrow||entity instanceof net.minecraft.entity.projectile.EntityThrowable)emit(entity.world,entity.posX,entity.posY,entity.posZ,3,owner instanceof EntityPlayer?(EntityPlayer)owner:null);}
    @SubscribeEvent public void unload(WorldEvent.Unload event){SculkSensorTile.clear(event.getWorld());SculkCatalystTile.clear(event.getWorld());}
}
