package ganymedes01.etfuturum;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
/** Reuse native shulker inventory, opening collision, drops, dispenser placement and data contracts. */
public final class UndyedShulkerBox extends BlockShulkerBox {
 UndyedShulkerBox(){super(EnumDyeColor.PURPLE);}
 @Override public TileEntity createNewTileEntity(World world,int meta){return new UndyedShulkerTile();}
 @Override public EnumDyeColor getColor(){return null;}
 @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){ItemStack original=super.getItem(world,pos,state),result=Content.stack("shulker_box");if(original.hasTagCompound())result.setTagCompound(original.getTagCompound().copy());return result;}
}
