package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.ModernAdditions;
import ganymedes01.etfuturum.tileentities.TileEntityWoodSign;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.*;

/** Keep legacy sign text and update packets; ceiling and wall identities carry native metadata. */
public final class BlockHangingSign extends BlockWoodSign {
    public final boolean ceiling;public final String wood;
    public BlockHangingSign(String wood,boolean ceiling,Block planks,int meta){super(TileEntityWoodSign.class,true,wood,planks,meta);this.wood=wood;this.ceiling=ceiling;setBlockName("etfuturum."+wood+"_hanging_sign");setCreativeTab(null);}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new ganymedes01.etfuturum.tileentities.TileEntityHangingSign();}
    @Override public Item getItemDropped(int meta,java.util.Random random,int fortune){return ModernAdditions.item(wood+"_hanging_sign");}
    @Override public Item getItem(World world,int x,int y,int z){return getItemDropped(0,null,0);}
    @Override public boolean canPlaceBlockAt(World world,int x,int y,int z){if(ceiling)return world.getBlock(x,y+1,z).getMaterial().isSolid()||world.getBlock(x,y+1,z) instanceof BlockHangingSign;for(int side=2;side<6;side++){net.minecraftforge.common.util.ForgeDirection d=net.minecraftforge.common.util.ForgeDirection.getOrientation(side);if(world.getBlock(x-d.offsetX,y,z-d.offsetZ).getMaterial().isSolid())return true;}return false;}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block block){if(world.isRemote)return;boolean valid=ceiling?canPlaceBlockAt(world,x,y,z):false;if(!ceiling){net.minecraftforge.common.util.ForgeDirection d=net.minecraftforge.common.util.ForgeDirection.getOrientation(world.getBlockMetadata(x,y,z));valid=world.getBlock(x-d.offsetX,y,z-d.offsetZ).getMaterial().isSolid();}if(!valid){dropBlockAsItem(world,x,y,z,world.getBlockMetadata(x,y,z),0);world.setBlockToAir(x,y,z);}}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess access,int x,int y,int z){setBlockBounds(.125F,.125F,.4375F,.875F,.875F,.5625F);}
}
