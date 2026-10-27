package ganymedes01.etfuturum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
public final class CopperChestTile extends TileEntityLockableLoot implements ITickable {
    private NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);
    private int users,ticks;
    public float lidAngle,prevLidAngle;
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override public int getSizeInventory(){return 27;}
    @Override public boolean isEmpty(){for(ItemStack stack:items)if(!stack.isEmpty())return false;return true;}
    @Override public int getInventoryStackLimit(){return 64;}
    @Override public String getName(){return hasCustomName()?customName:"tile.etfuturum.copper_chest.name";}
    @Override public String getGuiID(){return "minecraft:chest";}
    @Override public Container createContainer(InventoryPlayer inventory,EntityPlayer player){fillWithLoot(player);return new ContainerChest(inventory,this,player);}
    @Override public void openInventory(EntityPlayer player){if(!player.isSpectator()&&!world.isRemote){users++;notifyUsers();}}
    @Override public void closeInventory(EntityPlayer player){if(!player.isSpectator()&&!world.isRemote){users=Math.max(0,users-1);notifyUsers();}}
    private void notifyUsers(){world.addBlockEvent(pos,getBlockType(),1,users);world.notifyNeighborsOfStateChange(pos,getBlockType(),false);}
    @Override public boolean receiveClientEvent(int id,int value){if(id==1){users=Math.max(0,value);return true;}return super.receiveClientEvent(id,value);}
    @Override public void update(){prevLidAngle=lidAngle;if(!world.isRemote&&users>0&&lidAngle==0)sound("open");lidAngle=MathHelper.clamp(lidAngle+(users>0?.1F:-.1F),0,1);if(!world.isRemote&&users==0&&prevLidAngle>=.5F&&lidAngle<.5F)sound("close");if(!world.isRemote&&++ticks%200==0&&users>0){users=0;for(EntityPlayer player:world.getEntitiesWithinAABB(EntityPlayer.class,new AxisAlignedBB(pos).grow(5)))if(player.openContainer instanceof ContainerChest&&((ContainerChest)player.openContainer).getLowerChestInventory()==this)users++;notifyUsers();}}
    private void sound(String event){world.playSound(null,pos,ModernSounds.get("block.copper_chest."+event),SoundCategory.BLOCKS,.5F,world.rand.nextFloat()*.1F+.9F);}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);items=NonNullList.withSize(27,ItemStack.EMPTY);if(!checkLootAndRead(tag))ItemStackHelper.loadAllItems(tag,items);if(tag.hasKey("CustomName",8))customName=tag.getString("CustomName");}
    @Override public NBTTagCompound getUpdateTag(){return super.writeToNBT(new NBTTagCompound());}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);if(!checkLootAndWrite(tag))ItemStackHelper.saveAllItems(tag,items);if(hasCustomName())tag.setString("CustomName",customName);return tag;}
}
