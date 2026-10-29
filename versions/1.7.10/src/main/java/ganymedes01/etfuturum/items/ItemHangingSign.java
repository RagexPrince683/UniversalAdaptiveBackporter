package ganymedes01.etfuturum.items;
import ganymedes01.etfuturum.*;
import ganymedes01.etfuturum.blocks.BlockHangingSign;
import ganymedes01.etfuturum.tileentities.TileEntityWoodSign;
import ganymedes01.etfuturum.network.WoodSignOpenMessage;
import net.minecraft.block.Block;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
public final class ItemHangingSign extends BaseItem {
    private final String wood;public ItemHangingSign(String wood){super(wood+"_hanging_sign");this.wood=wood;setMaxStackSize(16);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hx,float hy,float hz){if(side==1)return false;net.minecraftforge.common.util.ForgeDirection face=net.minecraftforge.common.util.ForgeDirection.getOrientation(side);int px=x+face.offsetX,py=y+face.offsetY,pz=z+face.offsetZ;BlockHangingSign block=(BlockHangingSign)ModernAdditions.block(wood+(side==0?"_hanging_sign":"_wall_hanging_sign"));if(!world.getBlock(x,y,z).getMaterial().isSolid()&&!(world.getBlock(x,y,z) instanceof BlockHangingSign)||!player.canPlayerEdit(px,py,pz,side,stack)||!world.getBlock(px,py,pz).isReplaceable(world,px,py,pz)||!block.canPlaceBlockAt(world,px,py,pz))return false;if(world.isRemote)return true;int meta=side==0?MathHelper.floor_double((player.rotationYaw+180)*16/360+.5)&15:side;if(!world.setBlock(px,py,pz,block,meta,3))return false;TileEntityWoodSign sign=(TileEntityWoodSign)world.getTileEntity(px,py,pz);if(sign!=null&&player instanceof EntityPlayerMP){sign.func_145912_a(player);EtFuturum.networkWrapper.sendTo(new WoodSignOpenMessage(sign,Block.getIdFromBlock(block)),(EntityPlayerMP)player);}if(!player.capabilities.isCreativeMode)stack.stackSize--;world.playSoundEffect(px+.5,py+.5,pz+.5,"dig.wood",1,.8F);return true;}
}
