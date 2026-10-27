package ganymedes01.etfuturum;

import ganymedes01.etfuturum.entities.EntityBee;
import ganymedes01.etfuturum.tileentities.TileEntityBeeHive;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class BeeHiveBlock extends Block {
    public static final PropertyDirection FACING=BlockHorizontal.FACING;
    public static final PropertyBool FULL=PropertyBool.create("honey");
    private final boolean nest;
    public BeeHiveBlock(boolean nest){super(Material.WOOD);this.nest=nest;setHardness(nest?.3F:.6F);setResistance(nest?.3F:.6F);setSoundType(SoundType.WOOD);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.SOUTH).withProperty(FULL,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,FULL);}
    @Override public IBlockState getStateFromMeta(int meta){EnumFacing facing=EnumFacing.byIndex(meta%6);if(facing.getAxis()==EnumFacing.Axis.Y)facing=EnumFacing.SOUTH;return getDefaultState().withProperty(FACING,facing).withProperty(FULL,meta>5);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getIndex()+(state.getValue(FULL)?6:0);}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new TileEntityBeeHive();}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing face,float x,float y,float z,int meta,EntityLivingBase placer,EnumHand hand){return getDefaultState().withProperty(FACING,placer.getHorizontalFacing().getOpposite());}
    @Override public void onBlockPlacedBy(World world,BlockPos pos,IBlockState state,EntityLivingBase placer,ItemStack stack){TileEntity tile=world.getTileEntity(pos);if(tile instanceof TileEntityBeeHive)updateHiveState(world,pos,((TileEntityBeeHive)tile).getHoneyLevel()==5);}
    public static void updateHiveState(World world,BlockPos pos,boolean full){if(!world.isBlockLoaded(pos))return;IBlockState state=world.getBlockState(pos);if(state.getBlock() instanceof BeeHiveBlock&&state.getValue(FULL)!=full)world.setBlockState(pos,state.withProperty(FULL,full),2);}
    @Override public boolean hasComparatorInputOverride(IBlockState state){return true;}
    @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos){TileEntity tile=world.getTileEntity(pos);return tile instanceof TileEntityBeeHive?((TileEntityBeeHive)tile).getHoneyLevel():0;}
    private void angerNearby(World world,BlockPos pos,EntityPlayer player){for(EntityBee bee:world.getEntitiesWithinAABB(EntityBee.class,new AxisAlignedBB(pos).grow(8,6,8)))if(bee.getAttackTarget()==null){EntityPlayer target=player;if(target==null){java.util.List<EntityPlayer> players=world.getEntitiesWithinAABB(EntityPlayer.class,new AxisAlignedBB(pos).grow(8,6,8));if(players.isEmpty())continue;target=players.get(world.rand.nextInt(players.size()));}bee.setBeeAttacker(target);}}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){TileEntity tile=world.getTileEntity(pos);ItemStack held=player.getHeldItem(hand);if(!(tile instanceof TileEntityBeeHive)||((TileEntityBeeHive)tile).getHoneyLevel()<5||!player.canPlayerEdit(pos,face,held))return false;boolean shear=held.getItem() instanceof ItemShears,bottle=held.getItem()==Items.GLASS_BOTTLE;if(!shear&&!bottle||!Content.available(shear?"honeycomb":"honey_bottle"))return false;if(world.isRemote)return true;if(shear){if(!ItemDelivery.drop(world,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,new ItemStack(Content.item("honeycomb"),3)))return true;held.damageItem(1,player);}else{ItemStack honey=Content.stack("honey_bottle");if(held.getCount()==1&&!player.capabilities.isCreativeMode)player.setHeldItem(hand,honey);else{if(!player.inventory.addItemStackToInventory(honey)&&!ItemDelivery.drop(world,player.posX,player.posY,player.posZ,honey))return true;if(!player.capabilities.isCreativeMode)held.shrink(1);}}world.playSound(null,pos,ModernSounds.get(shear?"block.beehive.shear":"item.bottle.fill"),SoundCategory.BLOCKS,1,1);TileEntityBeeHive hive=(TileEntityBeeHive)tile;hive.setHoneyLevel(0);if(!hive.isSmoked()){angerNearby(world,pos,player);hive.angerBees(player,TileEntityBeeHive.State.EMERGENCY);}return true;}
    private ItemStack hiveStack(TileEntity tile){ItemStack result=new ItemStack(this);if(tile instanceof TileEntityBeeHive){TileEntityBeeHive hive=(TileEntityBeeHive)tile;NBTTagCompound tag=new NBTTagCompound();if(!hive.hasNoBees())tag.setTag("Bees",hive.getBees());if(hive.getHoneyLevel()>0)tag.setInteger("honeyLevel",hive.getHoneyLevel());if(!tag.isEmpty())result.setTagInfo("BlockEntityTag",tag);}return result;}
    @Override public ItemStack getPickBlock(IBlockState state,RayTraceResult target,World world,BlockPos pos,EntityPlayer player){return hiveStack(world.getTileEntity(pos));}
    @Override public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune){drops.add(hiveStack(world.getTileEntity(pos)));}
    @Override public boolean removedByPlayer(IBlockState state,World world,BlockPos pos,EntityPlayer player,boolean harvest){if(harvest)return true;if(!world.isRemote){TileEntity tile=world.getTileEntity(pos);if(tile instanceof TileEntityBeeHive&&(!((TileEntityBeeHive)tile).hasNoBees()||((TileEntityBeeHive)tile).getHoneyLevel()>0)){if(!ItemDelivery.drop(world,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,hiveStack(tile)))return false;((TileEntityBeeHive)tile).destroyAllBees();}}return world.setBlockToAir(pos);}
    @Override public void harvestBlock(World world,EntityPlayer player,BlockPos pos,IBlockState state,TileEntity tile,ItemStack tool){boolean silk=EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH,tool)>0;if(tile instanceof TileEntityBeeHive&&!silk){TileEntityBeeHive hive=(TileEntityBeeHive)tile;angerNearby(world,pos,player);hive.angerBees(player,TileEntityBeeHive.State.EMERGENCY);}if(silk){net.minecraft.util.NonNullList<ItemStack> drops=net.minecraft.util.NonNullList.create();drops.add(hiveStack(tile));float chance=net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops,world,pos,state,0,1,true,player);for(ItemStack drop:drops)if(world.rand.nextFloat()<=chance)spawnAsEntity(world,pos,drop);player.addStat(net.minecraft.stats.StatList.getBlockStats(this));player.addExhaustion(.005F);}else super.harvestBlock(world,player,pos,state,tile,tool);if(tile instanceof TileEntityBeeHive)((TileEntityBeeHive)tile).destroyAllBees();world.setBlockToAir(pos);}
    @Override protected ItemStack getSilkTouchDrop(IBlockState state){return new ItemStack(this);}
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state){TileEntity tile=world.getTileEntity(pos);if(!world.isRemote&&tile instanceof TileEntityBeeHive){TileEntityBeeHive hive=(TileEntityBeeHive)tile;hive.angerBees(null,TileEntityBeeHive.State.EMERGENCY);if(!hive.hasNoBees())spawnAsEntity(world,pos,hiveStack(hive));}super.breakBlock(world,pos,state);world.removeTileEntity(pos);}
    @Override public int getFlammability(IBlockAccess world,BlockPos pos,EnumFacing side){return nest?30:5;}
    @Override public int getFireSpreadSpeed(IBlockAccess world,BlockPos pos,EnumFacing side){return 20;}
    @Override public void randomDisplayTick(IBlockState state,World world,BlockPos pos,java.util.Random rand){if(state.getValue(FULL)&&!world.getBlockState(pos.down()).isOpaqueCube()&&rand.nextFloat()<.7F)UAB.proxy.honeyDrip(world,pos.getX()+rand.nextDouble(),pos.getY(),pos.getZ()+rand.nextDouble());}
}
