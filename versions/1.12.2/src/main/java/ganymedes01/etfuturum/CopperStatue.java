package ganymedes01.etfuturum;
import ganymedes01.etfuturum.entities.EntityCopperGolem;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public final class CopperStatue extends Block {
    public static final PropertyInteger POSE=PropertyInteger.create("pose",0,3);
    public static final PropertyBool WAXED=PropertyBool.create("waxed");
    CopperStatue(){super(Material.IRON);setHardness(3);setDefaultState(blockState.getBaseState().withProperty(POSE,0).withProperty(WAXED,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,POSE,WAXED);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(POSE,meta&3).withProperty(WAXED,(meta&4)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(POSE)|(state.getValue(WAXED)?4:0);}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new CopperStatueTile();}
    @Override public EnumBlockRenderType getRenderType(IBlockState state){return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.2,0,.2,.8,1,.8);}
    @Override public int damageDropped(IBlockState state){return getMetaFromState(state);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){ItemStack held=player.getHeldItem(hand);if(!player.canPlayerEdit(pos,side,held))return false;if(held.getItem() instanceof ItemAxe){if(!world.isRemote){if(state.getValue(WAXED)){world.setBlockState(pos,state.withProperty(WAXED,false),3);if(!player.capabilities.isCreativeMode)held.damageItem(1,player);}else if(MobRegistry.enabled(EntityCopperGolem.class)){EntityCopperGolem golem=new EntityCopperGolem(world);golem.setLocationAndAngles(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);golem.setOxidation(2);if(world.spawnEntity(golem)){world.setBlockToAir(pos);if(!player.capabilities.isCreativeMode)held.damageItem(1,player);}}}return true;}if(Content.available("honeycomb")&&held.getItem()==Content.item("honeycomb")&&!state.getValue(WAXED)){if(!world.isRemote){world.setBlockState(pos,state.withProperty(WAXED,true),3);if(!player.capabilities.isCreativeMode)held.shrink(1);}return true;}if(!world.isRemote)world.setBlockState(pos,state.cycleProperty(POSE),3);return true;}
}
