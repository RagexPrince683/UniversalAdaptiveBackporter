package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Native sign ownership, text packets and editor; separate ceiling and wall identities preserve orientation. */
public final class HangingSigns {
    private HangingSigns(){}
    public static final String[] WOODS={"oak","spruce","birch","jungle","acacia","dark_oak","crimson","warped","mangrove","cherry","bamboo","pale_oak"};
    static void define(){for(String wood:WOODS){String id=wood+"_hanging_sign";Ceiling ceiling=Content.add(id,"content.hanging_signs",new Ceiling(wood));Wall wall=Content.add(wood+"_wall_hanging_sign","content.hanging_signs",new Wall(wood));ceiling.wall=wall;Content.add(id,"content.hanging_signs",new HangingItem(ceiling));wall.setCreativeTab(null);if(!wood.equals("crimson")&&!wood.equals("warped")){net.minecraft.init.Blocks.FIRE.setFireInfo(ceiling,5,20);net.minecraft.init.Blocks.FIRE.setFireInfo(wall,5,20);}String prerequisite=Content.FEATURE.get(wood+"_planks");if(prerequisite!=null){Content.requires(id,prerequisite);Content.requires(wood+"_wall_hanging_sign",prerequisite);}}net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(HangingTile.class,new ResourceLocation(UAB.ID,"hanging_sign"));}
    public static class HangingTile extends WoodSigns.SignTile{}
    public static class Ceiling extends BlockStandingSign implements WoodSigns.Sign {
        public final String wood;Wall wall;Ceiling(String wood){this.wood=wood;setHardness(1);setSoundType(SoundType.WOOD);}
        public String wood(){return wood;}public boolean standing(){return true;}public String itemId(){return wood+"_hanging_sign";}
        @Override public TileEntity createNewTileEntity(World world,int meta){return new HangingTile();}
        @Override public Item getItemDropped(IBlockState state,java.util.Random random,int fortune){return Content.item(itemId());}
        @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return Content.stack(itemId());}
        @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return world.getBlockState(pos.up()).getMaterial().isSolid()||world.getBlockState(pos.up()).getBlock() instanceof Ceiling;}
        @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){if(!world.isRemote&&!canPlaceBlockAt(world,pos)){dropBlockAsItem(world,pos,state,0);world.setBlockToAir(pos);}}
        @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess access,BlockPos pos){return new AxisAlignedBB(.125,.125,.4375,.875,.875,.5625);}
    }
    public static class Wall extends BlockWallSign implements WoodSigns.Sign {
        public final String wood;Wall(String wood){this.wood=wood;setHardness(1);setSoundType(SoundType.WOOD);}
        public String wood(){return wood;}public boolean standing(){return false;}public String itemId(){return wood+"_hanging_sign";}
        @Override public TileEntity createNewTileEntity(World world,int meta){return new HangingTile();}
        @Override public Item getItemDropped(IBlockState state,java.util.Random random,int fortune){return Content.item(itemId());}
        @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return Content.stack(itemId());}
    }
    public static final class HangingItem extends ItemBlock {
        private final Ceiling ceiling;HangingItem(Ceiling ceiling){super(ceiling);this.ceiling=ceiling;setMaxStackSize(16);}
        @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos clicked,EnumHand hand,EnumFacing side,float x,float y,float z){if(side==EnumFacing.UP)return EnumActionResult.FAIL;ItemStack held=player.getHeldItem(hand);BlockPos pos=clicked.offset(side);Block block=side==EnumFacing.DOWN?ceiling:ceiling.wall;if(!player.canPlayerEdit(pos,side,held)||!world.mayPlace(block,pos,false,side,player))return EnumActionResult.FAIL;if(side==EnumFacing.DOWN?!ceiling.canPlaceBlockAt(world,pos):!world.getBlockState(clicked).getMaterial().isSolid())return EnumActionResult.FAIL;if(world.isRemote)return EnumActionResult.SUCCESS;
            IBlockState state=side==EnumFacing.DOWN?ceiling.getDefaultState().withProperty(BlockStandingSign.ROTATION,MathHelper.floor((player.rotationYaw+180)*16/360+.5)&15):ceiling.wall.getDefaultState().withProperty(BlockWallSign.FACING,side);if(!world.setBlockState(pos,state,11))return EnumActionResult.FAIL;TileEntity tile=world.getTileEntity(pos);if(tile instanceof HangingTile&&!ItemBlock.setTileEntityNBT(world,player,pos,held)){HangingTile sign=(HangingTile)tile;sign.setPlayer(player);if(player instanceof EntityPlayerMP)((EntityPlayerMP)player).connection.sendPacket(new net.minecraft.network.play.server.SPacketBlockChange(world,pos));player.openEditSign(sign);}if(!player.capabilities.isCreativeMode)held.shrink(1);world.playSound(null,pos,SoundEvents.BLOCK_WOOD_PLACE,SoundCategory.BLOCKS,1,.8F);return EnumActionResult.SUCCESS;}
    }
}
