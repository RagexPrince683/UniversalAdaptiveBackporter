package ganymedes01.etfuturum;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public class CopperBlock extends Block {
    CopperBlock(int stage,boolean waxed,String kind){super(Material.IRON);setHardness(3);setResistance(6);setHarvestLevel("pickaxe",1);setTickRandomly(!waxed);}
    @Override public void randomTick(World world,BlockPos pos,IBlockState state,Random random){CopperAging.tick(world,pos,state,random);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){return CopperAging.use(world,pos,state,player,hand,face);}
}
