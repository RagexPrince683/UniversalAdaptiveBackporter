package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

public final class CopperDecoration {
    private CopperDecoration() { }
    static final class Grate extends CopperBlock {
        Grate(int stage,boolean waxed){super(stage,waxed,"copper_grate");setLightOpacity(0);}
        @Override public boolean isOpaqueCube(IBlockState state){return false;}
        @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    }
    static final class Bulb extends CopperBlock {
        public static final PropertyBool LIT=PropertyBool.create("lit"),POWERED=PropertyBool.create("powered");
        Bulb(int stage,boolean waxed){super(stage,waxed,"copper_bulb");setDefaultState(blockState.getBaseState().withProperty(LIT,false).withProperty(POWERED,false));}
        @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,LIT,POWERED);}
        @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(LIT,(meta&1)!=0).withProperty(POWERED,(meta&2)!=0);}
        @Override public int getMetaFromState(IBlockState state){return (state.getValue(LIT)?1:0)|(state.getValue(POWERED)?2:0);}
        @Override public int damageDropped(IBlockState state){return 0;}
        @Override public int getLightValue(IBlockState state,IBlockAccess world,BlockPos pos){return state.getValue(LIT)?new int[]{15,12,8,4}[CopperAging.stage(this)]:0;}
        @Override public boolean hasComparatorInputOverride(IBlockState state){return true;}
        @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos){return state.getValue(LIT)?15:0;}
        @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){neighborChanged(state,world,pos,this,pos);}
        @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){if(world.isRemote)return;boolean powered=world.isBlockPowered(pos);if(powered!=state.getValue(POWERED))world.setBlockState(pos,state.withProperty(POWERED,powered).withProperty(LIT,powered?!state.getValue(LIT):state.getValue(LIT)),3);}
    }
    static final class Door extends BlockDoor {
        Door(boolean waxed){super(Material.IRON);setHardness(3);setResistance(6);setTickRandomly(!waxed);}
        @Override public void randomTick(World world,BlockPos pos,IBlockState state,Random random){CopperAging.tick(world,pos,state,random);}
        @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){if(CopperAging.use(world,pos,state,player,hand,side))return true;BlockPos lower=state.getValue(HALF)==EnumDoorHalf.UPPER?pos.down():pos;IBlockState base=world.getBlockState(lower);if(base.getBlock()!=this)return false;world.setBlockState(lower,base.cycleProperty(OPEN),10);world.markBlockRangeForRenderUpdate(lower,lower.up());world.playEvent(player,base.getValue(OPEN)?1011:1005,pos,0);return true;}
    }
    static final class Trapdoor extends BlockTrapDoor {
        Trapdoor(boolean waxed){super(Material.IRON);setHardness(3);setResistance(6);setTickRandomly(!waxed);}
        @Override public void randomTick(World world,BlockPos pos,IBlockState state,Random random){CopperAging.tick(world,pos,state,random);}
        @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){if(CopperAging.use(world,pos,state,player,hand,side))return true;world.setBlockState(pos,state.cycleProperty(OPEN),2);world.playEvent(player,state.getValue(OPEN)?1036:1037,pos,0);return true;}
    }
}
