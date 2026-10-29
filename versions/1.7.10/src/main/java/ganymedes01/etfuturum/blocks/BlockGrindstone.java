package ganymedes01.etfuturum.blocks;
import ganymedes01.etfuturum.EtFuturum;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.*;
import net.minecraftforge.common.util.ForgeDirection;
public final class BlockGrindstone extends BlockStructureDecoration {
    public BlockGrindstone(){super("grindstone","grindstone_floor","grindstone_wall","grindstone_ceiling");setHardness(2);setResistance(6);setHarvestLevel("pickaxe",0);setBlockBounds(.125F,0,.375F,.875F,1,.625F);}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){if(!world.isRemote)player.openGui(EtFuturum.instance,12,world,x,y,z);return true;}
    @Override public int onBlockPlaced(World world,int x,int y,int z,int side,float hx,float hy,float hz,int meta){return (side==0?8:side==1?0:4)|(side==2?0:side==5?1:side==3?2:3);}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase entity,ItemStack stack){int meta=world.getBlockMetadata(x,y,z);if((meta&12)!=4)world.setBlockMetadataWithNotify(x,y,z,(meta&12)|(net.minecraft.util.MathHelper.floor_double(entity.rotationYaw*4/360+.5)&3),3);}
    @Override public boolean canPlaceBlockOnSide(World world,int x,int y,int z,int side){ForgeDirection facing=ForgeDirection.getOrientation(side);return world.isSideSolid(x-facing.offsetX,y-facing.offsetY,z-facing.offsetZ,facing);}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block block){if(world.isRemote)return;int meta=world.getBlockMetadata(x,y,z),side=(meta&12)==0?1:(meta&12)==8?0:BlockBookStorage.face(meta);if(!canPlaceBlockOnSide(world,x,y,z,side)){dropBlockAsItem(world,x,y,z,meta,0);world.setBlockToAir(x,y,z);}}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world,int x,int y,int z){int meta=world.getBlockMetadata(x,y,z);if((meta&12)==4){if((meta&1)==0)setBlockBounds(.125F,.375F,0,.875F,.625F,1);else setBlockBounds(0,.375F,.125F,1,.625F,.875F);}else if((meta&1)==0)setBlockBounds(.125F,0,.375F,.875F,1,.625F);else setBlockBounds(.375F,0,.125F,.625F,1,.875F);}
}
