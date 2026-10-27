package ganymedes01.etfuturum.entities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
/** One passenger and a native 27-slot container; inventory ownership follows dimension transfer. */
public class EntityNewBoatWithChest extends EntityNewBoat implements IInventory {
 private NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);private boolean transferring;
 public EntityNewBoatWithChest(World world){super(world);}
 @Override public boolean processInitialInteract(EntityPlayer player,EnumHand hand){if(player.isSneaking()){if(!world.isRemote&&isUsableByPlayer(player))player.displayGUIChest(this);return true;}return super.processInitialInteract(player,hand);}
 @Override public void updatePassenger(Entity passenger){super.updatePassenger(passenger);if(isPassenger(passenger)){net.minecraft.util.math.Vec3d offset=new net.minecraft.util.math.Vec3d(.2,0,0).rotateYaw(-rotationYaw*(float)Math.PI/180-(float)Math.PI/2);passenger.setPosition(passenger.posX+offset.x,passenger.posY,passenger.posZ+offset.z);}}
 @Override public int getSizeInventory(){return 27;}
 @Override public boolean isEmpty(){for(ItemStack item:items)if(!item.isEmpty())return false;return true;}
 @Override public ItemStack getStackInSlot(int slot){return items.get(slot);}
 @Override public ItemStack decrStackSize(int slot,int count){return ItemStackHelper.getAndSplit(items,slot,count);}
 @Override public ItemStack removeStackFromSlot(int slot){return ItemStackHelper.getAndRemove(items,slot);}
 @Override public void setInventorySlotContents(int slot,ItemStack stack){items.set(slot,stack);if(stack.getCount()>getInventoryStackLimit())stack.setCount(getInventoryStackLimit());}
 @Override public int getInventoryStackLimit(){return 64;}
 @Override public void markDirty(){}
 @Override public boolean isUsableByPlayer(EntityPlayer player){return !isDead&&player.world==world&&player.getDistanceSq(this)<=64;}
 @Override public void openInventory(EntityPlayer player){}
 @Override public void closeInventory(EntityPlayer player){}
 @Override public boolean isItemValidForSlot(int slot,ItemStack stack){return true;}
 @Override public int getField(int id){return 0;}
 @Override public void setField(int id,int value){}
 @Override public int getFieldCount(){return 0;}
 @Override public void clear(){items.clear();}
 @Override public String getName(){return hasCustomName()?getCustomNameTag():"container.etfuturum.chest_boat";}
 @Override public ITextComponent getDisplayName(){return hasCustomName()?new TextComponentString(getName()):new TextComponentTranslation(getName());}
 @Override protected void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);ItemStackHelper.saveAllItems(tag,items);}
 @Override protected void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);items=NonNullList.withSize(27,ItemStack.EMPTY);ItemStackHelper.loadAllItems(tag,items);}
 @Override public void setDead(){if(!world.isRemote&&!isDead&&!transferring&&world.getGameRules().getBoolean("doEntityDrops"))for(ItemStack item:items)if(!item.isEmpty())InventoryHelper.spawnItemStack(world,posX,posY,posZ,item);super.setDead();}
 @Override public Entity changeDimension(int dimension,ITeleporter teleporter){boolean before=transferring;transferring=true;try{return super.changeDimension(dimension,teleporter);}finally{transferring=before;}}
 @Override public boolean attackEntityFrom(DamageSource source,float amount){boolean alive=!isDead;boolean result=super.attackEntityFrom(source,amount);if(alive&&isDead&&!world.isRemote&&world.getGameRules().getBoolean("doEntityDrops")&&!(source.getTrueSource() instanceof EntityPlayer&&((EntityPlayer)source.getTrueSource()).capabilities.isCreativeMode))dropItem(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.CHEST),1);return result;}
 public float getChestHeight(){return isRaft()?.15625F:-.2F;}public float getChestXOffset(){return isRaft()?-.46875F:-.5F;}public float getChestZOffset(){return isRaft()?-1.15F:-1.1F;}
}
