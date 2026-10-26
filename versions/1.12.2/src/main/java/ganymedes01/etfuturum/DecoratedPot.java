package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class DecoratedPot extends Block {
    public static final PropertyDirection FACING=BlockHorizontal.FACING;public static final PropertyBool CRACKED=PropertyBool.create("cracked");
    DecoratedPot(){super(Material.ROCK);setHardness(0);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH).withProperty(CRACKED,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,CRACKED);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3)).withProperty(CRACKED,(meta&4)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex()|(state.getValue(CRACKED)?4:0);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.0625,0,.0625,.9375,1,.9375);}
    @Override public EnumBlockRenderType getRenderType(IBlockState state){return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new DecoratedPotTile();}
    @Override public void onBlockPlacedBy(World world,BlockPos pos,IBlockState state,EntityLivingBase placer,ItemStack stack){world.setBlockState(pos,state.withProperty(FACING,placer.getHorizontalFacing().getOpposite()),2);TileEntity tile=world.getTileEntity(pos);if(tile instanceof DecoratedPotTile)((DecoratedPotTile)tile).setDesigns(stack);}
    @Override public void onBlockHarvested(World world,BlockPos pos,IBlockState state,EntityPlayer player){ItemStack tool=player.getHeldItemMainhand();if(!player.capabilities.isCreativeMode&&(tool.getItem() instanceof ItemTool||tool.getItem() instanceof ItemSword||tool.getItem() instanceof ItemHoe||tool.getItem() instanceof ItemShears)&&EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH,tool)==0)world.setBlockState(pos,state.withProperty(CRACKED,true),2);super.onBlockHarvested(world,pos,state,player);}
    @Override public boolean removedByPlayer(IBlockState state,World world,BlockPos pos,EntityPlayer player,boolean willHarvest){return willHarvest||super.removedByPlayer(state,world,pos,player,false);}
    @Override public void harvestBlock(World world,EntityPlayer player,BlockPos pos,IBlockState state,TileEntity tile,ItemStack tool){super.harvestBlock(world,player,pos,world.getBlockState(pos),tile,tool);world.setBlockToAir(pos);}
    @Override public boolean canDropFromExplosion(Explosion explosion){return false;}
    @Override public void onBlockExploded(World world,BlockPos pos,Explosion explosion){NonNullList<ItemStack> drops=NonNullList.create();getDrops(drops,world,pos,world.getBlockState(pos).withProperty(CRACKED,true),0);for(ItemStack item:drops)spawnAsEntity(world,pos,item);world.setBlockToAir(pos);}
    @Override public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune){TileEntity tile=world.getTileEntity(pos);DecoratedPotTile pot=tile instanceof DecoratedPotTile?(DecoratedPotTile)tile:null;if(!state.getValue(CRACKED)){drops.add(pot==null?Content.stack("decorated_pot"):pot.potItem());return;}for(int i=0;i<4;i++){String name=pot==null?"":pot.designs[i];Item item=name.isEmpty()?Items.BRICK:Item.REGISTRY.getObject(new ResourceLocation(name));if(item!=null&&item!=Items.AIR)drops.add(new ItemStack(item));}}
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state){TileEntity tile=world.getTileEntity(pos);if(!world.isRemote&&tile instanceof DecoratedPotTile)for(ItemStack item:((DecoratedPotTile)tile).allContents())spawnAsEntity(world,pos,item);world.removeTileEntity(pos);super.breakBlock(world,pos,state);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){if(world.isRemote)return true;TileEntity tile=world.getTileEntity(pos);ItemStack held=player.getHeldItem(hand);if(!(tile instanceof DecoratedPotTile)||held.isEmpty()||!player.canPlayerEdit(pos,face,held))return true;DecoratedPotTile pot=(DecoratedPotTile)tile;ItemStack stored=pot.getStackInSlot(0);if(stored.isEmpty()||stored.getCount()<stored.getMaxStackSize()&&ItemStack.areItemsEqual(stored,held)&&ItemStack.areItemStackTagsEqual(stored,held)){if(stored.isEmpty()){stored=held.copy();stored.setCount(1);}else stored.grow(1);pot.setInventorySlotContents(0,stored);if(!player.capabilities.isCreativeMode)held.shrink(1);world.playSound(null,pos,SoundEvents.ENTITY_ITEM_PICKUP,SoundCategory.BLOCKS,.7F,.8F+.4F*stored.getCount()/stored.getMaxStackSize());}return true;}
    @Override public boolean hasComparatorInputOverride(IBlockState state){return true;}
    @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos){TileEntity tile=world.getTileEntity(pos);return tile instanceof DecoratedPotTile?Container.calcRedstoneFromInventory((DecoratedPotTile)tile):0;}
}
