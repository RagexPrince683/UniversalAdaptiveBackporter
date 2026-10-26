package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;

/** Native states retain the maintained age/leaves/stage metadata without its bit-clearing defects. */
public final class BambooBlock extends Block implements IGrowable, IPlantable {
    public enum Leaves implements IStringSerializable { NONE, SMALL, LARGE; public String getName(){return name().toLowerCase(java.util.Locale.ROOT);} }
    public static final PropertyInteger AGE=PropertyInteger.create("age",0,1), STAGE=PropertyInteger.create("stage",0,1);
    public static final PropertyEnum<Leaves> LEAVES=PropertyEnum.create("leaves",Leaves.class);
    BambooBlock(){super(Material.WOOD);setHardness(1);setResistance(1);setSoundType(SoundType.WOOD);setTickRandomly(true);setLightOpacity(0);setHarvestLevel("axe",0);setDefaultState(blockState.getBaseState().withProperty(AGE,0).withProperty(LEAVES,Leaves.NONE).withProperty(STAGE,0));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,AGE,LEAVES,STAGE);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(AGE,meta&1).withProperty(LEAVES,Leaves.values()[Math.min(2,(meta>>1)&3)]).withProperty(STAGE,(meta>>3)&1);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(AGE)|(state.getValue(LEAVES).ordinal()<<1)|(state.getValue(STAGE)<<3);}
    static boolean soil(IBlockState state){Block b=state.getBlock();return b==Blocks.GRASS||b==Blocks.DIRT||b==Blocks.SAND||b==Blocks.GRAVEL||b==Content.BLOCKS.get("bamboo")||b==Content.BLOCKS.get("bamboo_sapling");}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return pos.getY()>0&&pos.getY()<world.getHeight()&&world.isAirBlock(pos)&&soil(world.getBlockState(pos.down()));}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing side){return BlockFaceShape.UNDEFINED;}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return false;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public EnumPushReaction getPushReaction(IBlockState state){return EnumPushReaction.DESTROY;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){double radius=state.getValue(LEAVES)==Leaves.LARGE?.3125:.1875;return new AxisAlignedBB(.5-radius,0,.5-radius,.5+radius,1,.5+radius);}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){double radius=state.getValue(AGE)==1?.09375:.0625;return new AxisAlignedBB(.5-radius,0,.5-radius,.5+radius,1,.5+radius);}
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return Content.item("bamboo");}
    @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return Content.stack("bamboo");}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public float getPlayerRelativeBlockHardness(IBlockState state,EntityPlayer player,World world,BlockPos pos){ItemStack tool=player.getHeldItemMainhand();return tool.getItem() instanceof ItemSword||tool.getItem().getToolClasses(tool).contains("sword")?Float.POSITIVE_INFINITY:super.getPlayerRelativeBlockHardness(state,player,world,pos);}
    @Override public int getFlammability(IBlockAccess world,BlockPos pos,EnumFacing side){return 60;}
    @Override public int getFireSpreadSpeed(IBlockAccess world,BlockPos pos,EnumFacing side){return 30;}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from){if(!world.isRemote){if(!soil(world.getBlockState(pos.down()))){world.destroyBlock(pos,true);return;}IBlockState above=world.getBlockState(pos.up());if(above.getBlock()==this&&above.getValue(AGE)>state.getValue(AGE))world.setBlockState(pos,state.withProperty(AGE,1),2);}}
    private int below(World world,BlockPos pos){int n=0;while(n<16&&pos.getY()-n>0&&world.getBlockState(pos.down(n+1)).getBlock()==this)n++;return n;}
    private BlockPos top(World world,BlockPos pos){for(int i=0;i<16&&pos.getY()<world.getHeight()-1&&world.isBlockLoaded(pos.up())&&world.getBlockState(pos.up()).getBlock()==this;i++)pos=pos.up();return pos;}
    private void extend(World world,BlockPos pos,Random random,int height){
        if(height>=16||pos.getY()>=world.getHeight()-1||!world.isAirBlock(pos.up()))return;
        IBlockState origin=world.getBlockState(pos),one=world.getBlockState(pos.down()),two=world.getBlockState(pos.down(2));Leaves leaves=Leaves.SMALL;
        if(one.getBlock()==this&&one.getValue(LEAVES)!=Leaves.NONE){leaves=Leaves.LARGE;if(two.getBlock()==this){world.setBlockState(pos.down(),one.withProperty(LEAVES,Leaves.SMALL),2);world.setBlockState(pos.down(2),two.withProperty(LEAVES,Leaves.NONE),2);}}
        int age=origin.getValue(AGE)==1||two.getBlock()==this?1:0,stage=height==15||height>=11&&random.nextFloat()<.25F?1:0;
        world.setBlockState(pos.up(),getDefaultState().withProperty(AGE,age).withProperty(LEAVES,leaves).withProperty(STAGE,stage),3);
    }
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){if(!world.isRemote&&state.getValue(STAGE)==0&&random.nextInt(3)==0&&world.getLightFromNeighbors(pos.up())>=9)extend(world,pos,random,below(world,pos)+1);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){BlockPos top=top(world,pos);return below(world,top)+1<16&&world.getBlockState(top).getValue(STAGE)==0&&top.getY()<world.getHeight()-1&&world.isAirBlock(top.up());}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(world.isRemote)return;BlockPos top=top(world,pos);int height=below(world,top)+1;for(int i=0,n=1+random.nextInt(2);i<n&&canGrow(world,top,world.getBlockState(top),false);i++){extend(world,top,random,height++);top=top.up();}}
    @Override public EnumPlantType getPlantType(IBlockAccess world,BlockPos pos){return EnumPlantType.Plains;}
    @Override public IBlockState getPlant(IBlockAccess world,BlockPos pos){return world.getBlockState(pos);}
}
