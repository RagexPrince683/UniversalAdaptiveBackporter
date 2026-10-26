package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.IShearable;

public final class GlowLichen extends Block implements IGrowable,IShearable {
    public static final PropertyBool[] FACES=new PropertyBool[6];
    static {for(EnumFacing face:EnumFacing.values())FACES[face.getIndex()]=PropertyBool.create(face.getName());}
    GlowLichen(){super(Material.VINE);setHardness(.2F);setSoundType(SoundType.PLANT);setLightLevel(7F/15);setLightOpacity(0);IBlockState state=blockState.getBaseState();for(PropertyBool face:FACES)state=state.withProperty(face,false);setDefaultState(state);}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACES);}
    @Override public IBlockState getStateFromMeta(int meta){return meta>=1&&meta<=6?getDefaultState().withProperty(FACES[meta-1],true):getDefaultState();}
    @Override public int getMetaFromState(IBlockState state){for(int i=0;i<6;i++)if(state.getValue(FACES[i]))return i+1;return 0;}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){LichenTile tile=new LichenTile();for(int i=0;i<6;i++)if(state.getValue(FACES[i]))tile.sides|=1<<i;return tile;}
    @Override public IBlockState getActualState(IBlockState state,IBlockAccess world,BlockPos pos){TileEntity tile=world.getTileEntity(pos);if(tile instanceof LichenTile)for(int i=0;i<6;i++)state=state.withProperty(FACES[i],(((LichenTile)tile).sides&(1<<i))!=0);return state;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return false;}
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing side){return BlockFaceShape.UNDEFINED;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
    @Override public int quantityDropped(Random random){return 0;}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public boolean isReplaceable(IBlockAccess world,BlockPos pos){return false;}
    static boolean support(World world,BlockPos pos,EnumFacing face){BlockPos backing=pos.offset(face);return world.isBlockLoaded(backing)&&world.getBlockState(backing).isSideSolid(world,backing,face.getOpposite());}
    public boolean attach(World world,BlockPos pos,EnumFacing face,boolean notify){
        if(pos.getY()<1||pos.getY()>=world.getHeight()-1||!world.isBlockLoaded(pos)||!support(world,pos,face))return false;
        Block old=world.getBlockState(pos).getBlock();if(old!=this&&!world.isAirBlock(pos))return false;
        if(old!=this)world.setBlockState(pos,getDefaultState().withProperty(FACES[face.getIndex()],true),notify?3:18);
        TileEntity tile=world.getTileEntity(pos);if(!(tile instanceof LichenTile))return false;LichenTile lichen=(LichenTile)tile;
        int bit=1<<face.getIndex();if(old==this&&(lichen.sides&bit)!=0)return false;lichen.setSides(lichen.sides|bit);return true;
    }
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from){if(world.isRemote)return;TileEntity tile=world.getTileEntity(pos);if(!(tile instanceof LichenTile))return;LichenTile lichen=(LichenTile)tile;int sides=lichen.sides;for(EnumFacing face:EnumFacing.values())if(world.isBlockLoaded(pos.offset(face))&&!support(world,pos,face))sides&=~(1<<face.getIndex());if(sides==0)world.setBlockToAir(pos);else if(sides!=lichen.sides)lichen.setSides(sides);}
    private List<Map.Entry<BlockPos,EnumFacing>> growth(World world,BlockPos pos){
        List<Map.Entry<BlockPos,EnumFacing>> result=new ArrayList<>();TileEntity tile=world.getTileEntity(pos);if(!(tile instanceof LichenTile))return result;int sides=((LichenTile)tile).sides;
        for(EnumFacing original:EnumFacing.values())if((sides&(1<<original.getIndex()))!=0)for(EnumFacing step:EnumFacing.values()){
            if(step.getAxis()==original.getAxis())continue;BlockPos next=pos.offset(step);EnumFacing face=original;
            if(!world.isBlockLoaded(next))continue;
            if(!support(world,next,face)){next=next.offset(original);face=step.getOpposite();}
            if(!world.isBlockLoaded(next)||!support(world,next,face))continue;
            Block block=world.getBlockState(next).getBlock();if(block==this){TileEntity existing=world.getTileEntity(next);if(existing instanceof LichenTile&&(((LichenTile)existing).sides&(1<<face.getIndex()))==0)result.add(new AbstractMap.SimpleImmutableEntry<>(next,face));}
            else if(world.isAirBlock(next))result.add(new AbstractMap.SimpleImmutableEntry<>(next,face));
            if((sides&(1<<step.getIndex()))==0&&support(world,pos,step))result.add(new AbstractMap.SimpleImmutableEntry<>(pos,step));
        }
        return result;
    }
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return !growth(world,pos).isEmpty();}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(world.isRemote)return;List<Map.Entry<BlockPos,EnumFacing>> places=growth(world,pos);if(!places.isEmpty()){Map.Entry<BlockPos,EnumFacing> spot=places.get(random.nextInt(places.size()));attach(world,spot.getKey(),spot.getValue(),true);}}
    @Override public boolean isShearable(ItemStack item,IBlockAccess world,BlockPos pos){return true;}
    @Override public List<ItemStack> onSheared(ItemStack item,IBlockAccess world,BlockPos pos,int fortune){return Collections.singletonList(Content.stack("glow_lichen"));}
    private static AxisAlignedBB plane(EnumFacing face){switch(face){case DOWN:return new AxisAlignedBB(0,0,0,1,.0625,1);case UP:return new AxisAlignedBB(0,.9375,0,1,1,1);case NORTH:return new AxisAlignedBB(0,0,0,1,1,.0625);case SOUTH:return new AxisAlignedBB(0,0,.9375,1,1,1);case WEST:return new AxisAlignedBB(0,0,0,.0625,1,1);default:return new AxisAlignedBB(.9375,0,0,1,1,1);}}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){state=getActualState(state,world,pos);AxisAlignedBB box=null;for(EnumFacing face:EnumFacing.values())if(state.getValue(FACES[face.getIndex()]))box=box==null?plane(face):box.union(plane(face));return box==null?FULL_BLOCK_AABB:box;}
    @Override public RayTraceResult collisionRayTrace(IBlockState state,World world,BlockPos pos,Vec3d start,Vec3d end){state=getActualState(state,world,pos);RayTraceResult closest=null;double distance=Double.MAX_VALUE;for(EnumFacing face:EnumFacing.values())if(state.getValue(FACES[face.getIndex()])){RayTraceResult hit=rayTrace(pos,start,end,plane(face));if(hit!=null&&hit.hitVec.squareDistanceTo(start)<distance){closest=hit;distance=hit.hitVec.squareDistanceTo(start);}}return closest;}
    public static final class LichenItem extends ItemBlock {
        public LichenItem(Block block){super(block);}
        @Override public EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing side,float x,float y,float z){ItemStack held=player.getHeldItem(hand);BlockPos target=world.getBlockState(pos).getBlock()==block?pos:pos.offset(side);if(held.isEmpty()||!Content.available("glow_lichen")||!player.canPlayerEdit(target,side,held))return EnumActionResult.FAIL;EnumFacing face=side.getOpposite();if(!support(world,target,face))return EnumActionResult.FAIL;if(world.isRemote)return EnumActionResult.SUCCESS;if(!((GlowLichen)block).attach(world,target,face,true))return EnumActionResult.FAIL;if(!player.capabilities.isCreativeMode)held.shrink(1);return EnumActionResult.SUCCESS;}
    }
}
