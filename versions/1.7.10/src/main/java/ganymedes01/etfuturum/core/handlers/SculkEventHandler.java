package ganymedes01.etfuturum.core.handlers;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.gameevent.TickEvent;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigExperiments;
import ganymedes01.etfuturum.tileentities.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.*;
/** Native event adaptation and loaded sensor admission; no replacement game loop. */
public final class SculkEventHandler {
 public static final SculkEventHandler INSTANCE=new SculkEventHandler();private SculkEventHandler(){}
 public static void initialize(){MinecraftForge.EVENT_BUS.register(INSTANCE);FMLCommonHandler.instance().bus().register(INSTANCE);}
 public static void emit(World world,double x,double y,double z,int frequency,EntityPlayer player){if(!world.isRemote&&ConfigExperiments.enableSculk)TileEntitySculkSensor.emit(world,x,y,z,frequency,player);}
 public static int absorb(EntityLivingBase entity,int amount){if(entity instanceof EntityPlayer||entity.worldObj.isRemote||!ConfigExperiments.enableSculk||amount<=0)return amount;TileEntitySculkCatalyst tile=TileEntitySculkCatalyst.nearest(entity.worldObj,entity.posX,entity.posY,entity.posZ);return tile==null?amount:amount-tile.accept(amount);}
 @SubscribeEvent public void step(TickEvent.PlayerTickEvent event){EntityPlayer player=event.player;if(event.phase!=TickEvent.Phase.END||player.worldObj.isRemote||player.ticksExisted%10!=0||player.capabilities.disableDamage||player.isSneaking()||player.getDistanceSq(player.prevPosX,player.prevPosY,player.prevPosZ)<.0001)return;int x=MathHelper.floor_double(player.posX),y=MathHelper.floor_double(player.boundingBox.minY-.01),z=MathHelper.floor_double(player.posZ);if(!player.worldObj.blockExists(x,y,z)||player.worldObj.getBlock(x,y,z)==Blocks.wool||player.worldObj.getBlock(x,y,z)==Blocks.carpet)return;emit(player.worldObj,player.posX,player.posY,player.posZ,1,player);}
 @SubscribeEvent(priority=EventPriority.LOWEST) public void place(BlockEvent.PlaceEvent event){emit(event.world,event.x+.5,event.y+.5,event.z+.5,13,event.player);}
 @SubscribeEvent(priority=EventPriority.LOWEST) public void broken(BlockEvent.BreakEvent event){emit(event.world,event.x+.5,event.y+.5,event.z+.5,12,event.getPlayer());}
 @SubscribeEvent(priority=EventPriority.LOWEST) public void interact(PlayerInteractEvent event){if(event.action!=PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)return;net.minecraft.block.Block block=event.world.getBlock(event.x,event.y,event.z);if(!(event.world.getTileEntity(event.x,event.y,event.z) instanceof net.minecraft.inventory.IInventory)&&!(block instanceof net.minecraft.block.BlockDoor)&&!(block instanceof net.minecraft.block.BlockTrapDoor)&&!(block instanceof net.minecraft.block.BlockFenceGate)&&!(block instanceof net.minecraft.block.BlockLever)&&!(block instanceof net.minecraft.block.BlockButton))return;emit(event.world,event.x+.5,event.y+.5,event.z+.5,10,event.entityPlayer);}
 @SubscribeEvent(priority=EventPriority.LOWEST) public void hurt(LivingHurtEvent event){EntityLivingBase entity=event.entityLiving;emit(entity.worldObj,entity.posX,entity.posY,entity.posZ,7,entity instanceof EntityPlayer?(EntityPlayer)entity:null);}
 @SubscribeEvent(priority=EventPriority.LOWEST) public void death(LivingDeathEvent event){EntityLivingBase entity=event.entityLiving;emit(entity.worldObj,entity.posX,entity.posY,entity.posZ,15,entity instanceof EntityPlayer?(EntityPlayer)entity:null);}
 @SubscribeEvent public void finish(net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish event){net.minecraft.item.EnumAction action=event.item.getItemUseAction();if(action==net.minecraft.item.EnumAction.eat||action==net.minecraft.item.EnumAction.drink)emit(event.entityPlayer.worldObj,event.entityPlayer.posX,event.entityPlayer.posY,event.entityPlayer.posZ,8,event.entityPlayer);}
 @SubscribeEvent public void projectile(net.minecraftforge.event.entity.EntityJoinWorldEvent event){Entity entity=event.entity;EntityLivingBase owner=entity instanceof net.minecraft.entity.projectile.EntityArrow?((net.minecraft.entity.projectile.EntityArrow)entity).shootingEntity instanceof EntityLivingBase?(EntityLivingBase)((net.minecraft.entity.projectile.EntityArrow)entity).shootingEntity:null:entity instanceof net.minecraft.entity.projectile.EntityThrowable?((net.minecraft.entity.projectile.EntityThrowable)entity).getThrower():null;if(entity instanceof net.minecraft.entity.projectile.EntityArrow||entity instanceof net.minecraft.entity.projectile.EntityThrowable)emit(entity.worldObj,entity.posX,entity.posY,entity.posZ,3,owner instanceof EntityPlayer?(EntityPlayer)owner:null);}
 @SubscribeEvent public void unload(WorldEvent.Unload event){TileEntitySculkSensor.clear(event.world);TileEntitySculkCatalyst.clear(event.world);}
}
