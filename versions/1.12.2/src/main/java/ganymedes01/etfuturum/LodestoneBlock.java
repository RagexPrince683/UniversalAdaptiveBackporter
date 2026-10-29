package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
final class LodestoneBlock extends Block {
 LodestoneBlock(){super(net.minecraft.block.material.Material.ROCK);setHardness(3.5F);setResistance(3.5F);setHarvestLevel("pickaxe",1);}
 @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){return Content.available("lodestone")&&player.canPlayerEdit(pos,side,player.getHeldItem(hand))&&NavigationItems.bind(player,world,pos,hand);}
}
