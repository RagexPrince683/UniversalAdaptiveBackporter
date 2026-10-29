package ganymedes01.etfuturum.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.*;

public final class BlockBell extends BlockStructureDecoration {
    public BlockBell(){super("bell","bell_floor","bell_ceiling","bell_wall");setHardness(5);setResistance(5);setHarvestLevel("pickaxe",0);setBlockBounds(.125F,0,.125F,.875F,1,.875F);}
    @Override public boolean hasTileEntity(int meta){return true;}@Override public TileEntity createTileEntity(World world,int meta){return new BellTile();}
    @Override public boolean onBlockEventReceived(World world,int x,int y,int z,int id,int data){TileEntity tile=world.getTileEntity(x,y,z);return tile!=null&&tile.receiveClientEvent(id,data);}
    @Override public boolean canPlaceBlockOnSide(World world,int x,int y,int z,int side){net.minecraftforge.common.util.ForgeDirection face=net.minecraftforge.common.util.ForgeDirection.getOrientation(side);return world.isSideSolid(x-face.offsetX,y-face.offsetY,z-face.offsetZ,face);}
    @Override public int onBlockPlaced(World world,int x,int y,int z,int side,float hx,float hy,float hz,int meta){return (side==1?0:side==0?4:8)|(side==2?0:side==5?1:side==3?2:3);}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){if(!player.canPlayerEdit(x,y,z,side,player.getHeldItem()))return false;ring(world,x,y,z);return true;}
    @Override public void onBlockClicked(World world,int x,int y,int z,EntityPlayer player){ring(world,x,y,z);}
    @Override public void onEntityCollidedWithBlock(World world,int x,int y,int z,Entity entity){if(entity instanceof EntityArrow||entity instanceof EntityThrowable)ring(world,x,y,z);}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block block){if(world.isRemote)return;TileEntity tile=world.getTileEntity(x,y,z);if(!(tile instanceof BellTile))return;BellTile bell=(BellTile)tile;int meta=world.getBlockMetadata(x,y,z),anchor=(meta&12)==0?1:(meta&12)==4?0:BlockBookStorage.face(meta);if(!canPlaceBlockOnSide(world,x,y,z,anchor)){dropBlockAsItem(world,x,y,z,meta,0);world.setBlockToAir(x,y,z);return;}boolean powered=world.isBlockIndirectlyGettingPowered(x,y,z);if(powered&&!bell.powered)ring(world,x,y,z);if(powered!=bell.powered){bell.powered=powered;bell.markDirty();}}
    @Override public void onBlockAdded(World world,int x,int y,int z){onNeighborBlockChange(world,x,y,z,this);}
    public static void ring(World world,int x,int y,int z){if(world.isRemote)return;TileEntity tile=world.getTileEntity(x,y,z);if(!(tile instanceof BellTile)||world.getTotalWorldTime()-((BellTile)tile).lastRing<2)return;((BellTile)tile).lastRing=world.getTotalWorldTime();world.addBlockEvent(x,y,z,world.getBlock(x,y,z),1,0);world.playSoundEffect(x+.5,y+.5,z+.5,"minecraft_1.21.11:block.bell.use",2,1);
        for(Object object:world.getEntitiesWithinAABB(EntityVillager.class,AxisAlignedBB.getBoundingBox(x-32,y-32,z-32,x+33,y+33,z+33))){EntityVillager villager=(EntityVillager)object;villager.getEntityData().setLong("UabBellHeardUntil",world.getTotalWorldTime()+300);if(villager.hasHome()){ChunkCoordinates home=villager.getHomePosition();villager.getNavigator().tryMoveToXYZ(home.posX+.5,home.posY,home.posZ+.5,1);}}
        if(world instanceof WorldServer)for(Object object:world.getEntitiesWithinAABB(EntityLivingBase.class,AxisAlignedBB.getBoundingBox(x-32,y-32,z-32,x+33,y+33,z+33))){EntityLivingBase entity=(EntityLivingBase)object;if(entity instanceof ganymedes01.etfuturum.entities.EntityIllager||entity.getClass().getSimpleName().equals("EntityRavager"))((WorldServer)world).func_147487_a("witchMagic",entity.posX,entity.posY+1,entity.posZ,20,.3,.7,.3,.01);}

    }
    public static final class BellTile extends TileEntity {public boolean powered;public long lastRing=Long.MIN_VALUE/2;public int ringing;
        @Override public void updateEntity(){if(ringing>0)ringing--;}
        @Override public boolean receiveClientEvent(int id,int data){if(id==1){ringing=50;return true;}return super.receiveClientEvent(id,data);}
        @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setBoolean("Powered",powered);}@Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);powered=tag.getBoolean("Powered");}
    }
}
