package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class CampfireBlock extends Block {
    public static final PropertyDirection FACING=BlockHorizontal.FACING;
    public static final PropertyBool LIT=PropertyBool.create("lit"),WATERLOGGED=PropertyBool.create("waterlogged");
    public final boolean soul;
    CampfireBlock(boolean soul){super(Material.WOOD);this.soul=soul;setHardness(2);setSoundType(SoundType.WOOD);setLightOpacity(0);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH).withProperty(LIT,true).withProperty(WATERLOGGED,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,LIT,WATERLOGGED);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3)).withProperty(LIT,(meta&12)==0).withProperty(WATERLOGGED,(meta&8)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex()|(state.getValue(LIT)?0:4)|(state.getValue(WATERLOGGED)?8:0);}
    @Override public IBlockState withRotation(IBlockState state,Rotation rotation){return state.withProperty(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase placer,EnumHand hand){boolean wet=world.getBlockState(pos).getMaterial()==Material.WATER;return getDefaultState().withProperty(FACING,placer.getHorizontalFacing()).withProperty(LIT,!wet).withProperty(WATERLOGGED,wet);}
    @Override public int getLightValue(IBlockState state,IBlockAccess world,BlockPos pos){return state.getValue(LIT)?soul?10:15:0;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,0,0,1,.4375,1);}
    @Override public EnumPushReaction getPushReaction(IBlockState state){return EnumPushReaction.DESTROY;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public boolean canRenderInLayer(IBlockState state,BlockRenderLayer layer){return layer==BlockRenderLayer.CUTOUT||layer==BlockRenderLayer.TRANSLUCENT&&state.getValue(WATERLOGGED);}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new CampfireTile();}
    @Override public boolean canSilkHarvest(World world,BlockPos pos,IBlockState state,EntityPlayer player){return true;}
    @Override public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune){drops.add(soul?Content.stack("soul_soil"):new ItemStack(Items.COAL,2,1));}
    @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity){if(!world.isRemote&&!state.getValue(LIT)&&!state.getValue(WATERLOGGED)&&entity instanceof net.minecraft.entity.projectile.EntityArrow&&entity.isBurning()&&(((net.minecraft.entity.projectile.EntityArrow)entity).shootingEntity instanceof EntityPlayer||world.getGameRules().getBoolean("mobGriefing")))world.setBlockState(pos,state.withProperty(LIT,true),3);if(state.getValue(LIT)&&entity instanceof EntityLivingBase&&net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(Enchantments.FROST_WALKER,((EntityLivingBase)entity).getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot.FEET))==0)entity.attackEntityFrom(DamageSource.IN_FIRE,soul?2:1);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){
        ItemStack held=player.getHeldItem(hand);if(held.isEmpty()||!player.canPlayerEdit(pos,face,held))return false;
        if(state.getValue(WATERLOGGED)&&held.getItem()==Items.BUCKET){if(!world.isRemote){world.setBlockState(pos,state.withProperty(WATERLOGGED,false).withProperty(LIT,false),3);FilledCauldron.exchange(player,hand,new ItemStack(Items.WATER_BUCKET));}return true;}boolean light=!state.getValue(WATERLOGGED)&&!state.getValue(LIT)&&(held.getItem()==Items.FLINT_AND_STEEL||held.getItem()==Items.FIRE_CHARGE),extinguish=state.getValue(LIT)&&held.getItem() instanceof ItemSpade,fill=!state.getValue(WATERLOGGED)&&held.getItem()==Items.WATER_BUCKET&&!world.provider.doesWaterVaporize();
        if(light||extinguish||fill){if(!world.isRemote){world.setBlockState(pos,state.withProperty(LIT,light).withProperty(WATERLOGGED,fill||state.getValue(WATERLOGGED)),3);if(!player.capabilities.isCreativeMode){if(fill)FilledCauldron.exchange(player,hand,new ItemStack(Items.BUCKET));else if(extinguish||held.getItem()==Items.FLINT_AND_STEEL)held.damageItem(1,player);else held.shrink(1);}}return true;}
        TileEntity tile=world.getTileEntity(pos);return tile instanceof CampfireTile&&((CampfireTile)tile).insert(player,hand);
    }
    @Override public Boolean isEntityInsideMaterial(IBlockAccess access,BlockPos pos,IBlockState state,Entity entity,double y,Material material,boolean head){return state.getValue(WATERLOGGED)&&material==Material.WATER?y<pos.getY()+1:null;}
    @Override public Boolean isAABBInsideMaterial(World world,BlockPos pos,AxisAlignedBB box,Material material){return world.getBlockState(pos).getValue(WATERLOGGED)&&material==Material.WATER?box.intersects(new AxisAlignedBB(pos)):null;}
    @Override public Boolean isAABBInsideLiquid(World world,BlockPos pos,AxisAlignedBB box){return world.getBlockState(pos).getValue(WATERLOGGED)?box.intersects(new AxisAlignedBB(pos)):null;}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){if(world.isRemote)return;for(EnumFacing face:EnumFacing.values())if(world.getBlockState(pos.offset(face)).getMaterial()==Material.WATER){world.setBlockState(pos,state.withProperty(LIT,false).withProperty(WATERLOGGED,true),3);break;}}
    @Override public boolean removedByPlayer(IBlockState state,World world,BlockPos pos,EntityPlayer player,boolean harvest){return world.setBlockState(pos,state.getValue(WATERLOGGED)?Blocks.WATER.getDefaultState():Blocks.AIR.getDefaultState(),3);}
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state){TileEntity tile=world.getTileEntity(pos);if(tile instanceof CampfireTile)((CampfireTile)tile).dropContents();super.breakBlock(world,pos,state);world.removeTileEntity(pos);}
    @Override @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT) public void randomDisplayTick(IBlockState state,World world,BlockPos pos,java.util.Random random){if(state.getValue(LIT)){if(random.nextInt(3)==0)ganymedes01.etfuturum.client.CampfireSmoke.spawn(world,pos.getX()+.5,pos.getY()+.7,pos.getZ()+.5,world.getBlockState(pos.down()).getBlock()==Blocks.HAY_BLOCK);if(random.nextInt(10)==0)world.playSound(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,SoundEvents.BLOCK_FIRE_AMBIENT,SoundCategory.BLOCKS,.5F,1,false);}}
}
