package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Bells alert native villagers without replacing their profession or village ownership. */
public final class BellBlock extends Block {
    public static final PropertyDirection FACING=BlockHorizontal.FACING;public static final PropertyInteger ATTACHMENT=PropertyInteger.create("attachment",0,2);
    public BellBlock(){super(Material.IRON);setHardness(5);setResistance(5);setHarvestLevel("pickaxe",0);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH).withProperty(ATTACHMENT,0));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,ATTACHMENT);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3)).withProperty(ATTACHMENT,Math.min(2,meta>>2));}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex()|state.getValue(ATTACHMENT)<<2;}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase entity,EnumHand hand){return getDefaultState().withProperty(FACING,side.getAxis().isHorizontal()?side:entity.getHorizontalFacing()).withProperty(ATTACHMENT,side==EnumFacing.UP?0:side==EnumFacing.DOWN?1:2);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}@Override public boolean isFullCube(IBlockState state){return false;}@Override public int damageDropped(IBlockState state){return 0;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess access,BlockPos pos){return new AxisAlignedBB(.125,0,.125,.875,1,.875);}
    @Override public boolean hasTileEntity(IBlockState state){return true;}@Override public TileEntity createTileEntity(World world,IBlockState state){return new BellTile();}
    @Override public boolean eventReceived(IBlockState state,World world,BlockPos pos,int id,int data){TileEntity tile=world.getTileEntity(pos);return tile!=null&&tile.receiveClientEvent(id,data);}
    @Override public boolean canPlaceBlockOnSide(World world,BlockPos pos,EnumFacing side){return world.getBlockState(pos.offset(side.getOpposite())).isSideSolid(world,pos.offset(side.getOpposite()),side);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){if(!player.canPlayerEdit(pos,side,player.getHeldItem(hand)))return false;ring(world,pos);return true;}
    @Override public void onBlockClicked(World world,BlockPos pos,EntityPlayer player){ring(world,pos);}
    @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity){if(entity instanceof EntityArrow||entity instanceof EntityThrowable)ring(world,pos);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){if(world.isRemote)return;TileEntity tile=world.getTileEntity(pos);if(!(tile instanceof BellTile))return;BellTile bell=(BellTile)tile;EnumFacing anchor=state.getValue(ATTACHMENT)==0?EnumFacing.UP:state.getValue(ATTACHMENT)==1?EnumFacing.DOWN:state.getValue(FACING);if(!canPlaceBlockOnSide(world,pos,anchor)){dropBlockAsItem(world,pos,state,0);world.setBlockToAir(pos);return;}boolean power=world.isBlockPowered(pos);if(power&&!bell.powered)ring(world,pos);if(power!=bell.powered){bell.powered=power;bell.markDirty();}}
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){neighborChanged(state,world,pos,this,pos);}
    public static void ring(World world,BlockPos pos){if(world.isRemote)return;TileEntity tile=world.getTileEntity(pos);if(!(tile instanceof BellTile)||world.getTotalWorldTime()-((BellTile)tile).lastRing<2)return;((BellTile)tile).lastRing=world.getTotalWorldTime();world.addBlockEvent(pos,world.getBlockState(pos).getBlock(),1,0);world.playSound(null,pos,ModernSounds.get("block.bell.use"),SoundCategory.BLOCKS,2,1);
        for(EntityVillager villager:world.getEntitiesWithinAABB(EntityVillager.class,new AxisAlignedBB(pos).grow(32))){villager.getEntityData().setLong("UabBellHeardUntil",world.getTotalWorldTime()+300);if(villager.hasHome()){BlockPos home=villager.getHomePosition();villager.getNavigator().tryMoveToXYZ(home.getX()+.5,home.getY(),home.getZ()+.5,1);}}
        for(EntityLivingBase entity:world.getEntitiesWithinAABB(EntityLivingBase.class,new AxisAlignedBB(pos).grow(32)))if(entity instanceof ganymedes01.etfuturum.entities.EntityIllager||entity instanceof net.minecraft.entity.monster.AbstractIllager||entity.getClass().getSimpleName().equals("EntityRavager"))entity.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.GLOWING,60));
    }
    public static final class BellTile extends TileEntity implements ITickable {public boolean powered;public long lastRing=Long.MIN_VALUE/2;public int ringing;
        @Override public void update(){if(ringing>0)ringing--;}
        @Override public boolean receiveClientEvent(int id,int data){if(id==1){ringing=50;return true;}return super.receiveClientEvent(id,data);}
        @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setBoolean("Powered",powered);return tag;}@Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);powered=tag.getBoolean("Powered");}
    }
}
