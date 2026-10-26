package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

public final class BrushableTile extends TileEntity implements ITickable {
    public int progress;public EnumFacing face=EnumFacing.UP;private int idle,cooldown;private String lootTable="";private long seed;private ItemStack loot=ItemStack.EMPTY;
    public void configure(String table,long seed){lootTable=table;this.seed=seed;progress=0;loot=ItemStack.EMPTY;markDirty();}
    public ItemStack revealedItem(){return progress==0?ItemStack.EMPTY:loot;}
    public boolean brush(EnumFacing side){if(world==null||world.isRemote||cooldown>0||!(world.getBlockState(pos).getBlock() instanceof BrushableBlock))return false;if(progress==0&&loot.isEmpty()&&!lootTable.isEmpty()){List<ItemStack> items=ModernLoot.roll(lootTable,new Random(seed),(WorldServer)world);loot=items.isEmpty()?ItemStack.EMPTY:items.get(0);lootTable="";}face=side;idle=0;cooldown=10;progress++;world.playSound(null,pos,SoundEvents.BLOCK_SAND_HIT,SoundCategory.BLOCKS,.7F,1);if(progress<10){sync();return false;}
        if(!loot.isEmpty()){EntityItem item=new EntityItem(world,pos.getX()+.5+side.getXOffset()*.7,pos.getY()+.5+side.getYOffset()*.7,pos.getZ()+.5+side.getZOffset()*.7,loot.copy());item.motionX=item.motionY=item.motionZ=0;if(!world.spawnEntity(item)){progress=9;sync();return false;}}
        loot=ItemStack.EMPTY;world.setBlockState(pos,((BrushableBlock)world.getBlockState(pos).getBlock()).base.getDefaultState(),3);return true;
    }
    @Override public void update(){if(world.isRemote)return;if(cooldown>0)cooldown--;if(progress>0&&++idle>=40&&idle%4==0){progress=Math.max(0,progress-2);sync();}}
    private void sync(){markDirty();IBlockState state=world.getBlockState(pos);if(state.getBlock() instanceof BrushableBlock)world.setBlockState(pos,state.withProperty(BrushableBlock.DUSTED,Math.min(3,(progress+2)/3)),2);world.notifyBlockUpdate(pos,state,world.getBlockState(pos),3);}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setInteger("BrushProgress",progress);tag.setInteger("BrushFace",face.getIndex());tag.setInteger("BrushIdle",idle);tag.setInteger("BrushCooldown",cooldown);tag.setString("LootTable",lootTable);tag.setLong("LootTableSeed",seed);if(!loot.isEmpty())tag.setTag("Item",loot.writeToNBT(new NBTTagCompound()));return tag;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);progress=Math.max(0,Math.min(9,tag.getInteger("BrushProgress")));face=EnumFacing.byIndex(tag.getInteger("BrushFace"));idle=tag.getInteger("BrushIdle");cooldown=Math.max(0,Math.min(10,tag.getInteger("BrushCooldown")));lootTable=tag.getString("LootTable");seed=tag.getLong("LootTableSeed");loot=new ItemStack(tag.getCompoundTag("Item"));}
    @Override public NBTTagCompound getUpdateTag(){NBTTagCompound tag=super.writeToNBT(new NBTTagCompound());tag.setInteger("BrushProgress",progress);tag.setInteger("BrushFace",face.getIndex());if(!revealedItem().isEmpty())tag.setTag("Item",loot.writeToNBT(new NBTTagCompound()));return tag;}
    @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,1,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager network,SPacketUpdateTileEntity packet){NBTTagCompound tag=packet.getNbtCompound();progress=tag.getInteger("BrushProgress");face=EnumFacing.byIndex(tag.getInteger("BrushFace"));loot=new ItemStack(tag.getCompoundTag("Item"));world.markBlockRangeForRenderUpdate(pos,pos);}
}
