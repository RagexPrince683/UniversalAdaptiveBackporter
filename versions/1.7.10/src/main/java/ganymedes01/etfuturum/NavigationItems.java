package ganymedes01.etfuturum;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.configuration.ContentAvailability;
/** Server-owned saved navigation; inventory NBT uses the native slot synchronization. */
public final class NavigationItems {
 public static final String TARGET="UabCompassTarget",DEATH="UabLastDeath";private NavigationItems(){}
public static void initialize(){ModernAdditions.add("lodestone","lodestones",new ganymedes01.etfuturum.blocks.BlockLodestone());ModernAdditions.add("lodestone_compass","lodestones",new ganymedes01.etfuturum.items.ItemLodestoneCompass());ModernAdditions.add("recovery_compass","recovery_compasses",new ganymedes01.etfuturum.items.ItemTargetCompass(true));net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new NavigationItems());}
 public static void recipes(){ModernAdditions.recipe("lodestone",1,"BBB","BIB","BBB",'B',new ItemStack(Blocks.stonebrick,1,3),'I',Items.iron_ingot);if(ModItems.ECHO_SHARD.isEnabled())ModernAdditions.recipe("recovery_compass",1,"EEE","ECE","EEE",'E',ModItems.ECHO_SHARD.get(),'C',Items.compass);}
 public static NBTTagCompound target(int dimension,int x,int y,int z){NBTTagCompound target=new NBTTagCompound();target.setInteger("Dimension",dimension);target.setInteger("X",x);target.setInteger("Y",y);target.setInteger("Z",z);return target;}
 public static boolean valid(NBTTagCompound target){return target.hasKey("Dimension",99)&&target.hasKey("X",99)&&target.hasKey("Y",99)&&target.hasKey("Z",99);}
 public static NBTTagCompound saved(EntityPlayer player){return player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getCompoundTag(DEATH);}
 private static void remember(EntityPlayer player,NBTTagCompound target){NBTTagCompound data=player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);data.setTag(DEATH,target.copy());player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG,data);}
 @SubscribeEvent public void death(LivingDeathEvent event){if(!(event.entityLiving instanceof EntityPlayer)||event.entityLiving.worldObj.isRemote)return;EntityPlayer player=(EntityPlayer)event.entityLiving;remember(player,target(player.worldObj.provider.dimensionId,(int)Math.floor(player.posX),(int)Math.floor(player.posY),(int)Math.floor(player.posZ)));}
 @SubscribeEvent public void clone(PlayerEvent.Clone event){NBTTagCompound target=saved(event.original);if(valid(target))remember(event.entityPlayer,target);}
 public static void update(ItemStack stack,World world,Entity entity,boolean recovery){if(world.isRemote||!(entity instanceof EntityPlayer))return;EntityPlayer player=(EntityPlayer)entity;NBTTagCompound root=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound(),previous=root.getCompoundTag(TARGET);if(recovery){NBTTagCompound next=saved(player);if(valid(next)){if(!previous.equals(next)){root.setTag(TARGET,next.copy());stack.setTagCompound(root);player.inventory.markDirty();}}else if(root.hasKey(TARGET)){root.removeTag(TARGET);stack.setTagCompound(root);player.inventory.markDirty();}}else if(valid(previous)&&player.ticksExisted%20==0&&previous.getInteger("Dimension")==world.provider.dimensionId){int x=previous.getInteger("X"),y=previous.getInteger("Y"),z=previous.getInteger("Z");if(world.blockExists(x,y,z)&&world.getBlock(x,y,z)!=ModernAdditions.block("lodestone")){root.removeTag(TARGET);stack.setTagCompound(root);player.inventory.markDirty();}}}
 public static boolean bind(EntityPlayer player,World world,int x,int y,int z){ItemStack held=player.getHeldItem();if(held==null||!(held.getItem()==Items.compass||held.getItem() instanceof ganymedes01.etfuturum.items.ItemTargetCompass&&!(( ganymedes01.etfuturum.items.ItemTargetCompass)held.getItem()).recovery))return false;if(world.isRemote)return true;ItemStack bound=new ItemStack(ModernAdditions.item("lodestone_compass"));NBTTagCompound tag=held.hasTagCompound()?(NBTTagCompound)held.getTagCompound().copy():new NBTTagCompound();tag.setTag(TARGET,target(player.worldObj.provider.dimensionId,x,y,z));bound.setTagCompound(tag);if(!player.capabilities.isCreativeMode&&held.stackSize==1){player.inventory.setInventorySlotContents(player.inventory.currentItem,bound);}else{if(!player.capabilities.isCreativeMode){held.stackSize--;if(held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}if(!player.inventory.addItemStackToInventory(bound))player.dropPlayerItemWithRandomChoice(bound,false);}player.inventory.markDirty();return true;}
}
