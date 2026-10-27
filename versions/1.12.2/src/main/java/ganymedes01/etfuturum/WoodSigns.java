package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
/** Native sign text, ownership, editing packets and placement; variants only select material and texture. */
public final class WoodSigns {
 public interface Sign {String wood();boolean standing();String itemId();}
 public static final Map<String,Standing> SIGNS=new LinkedHashMap<>();
 private WoodSigns(){}
 static void define(){boolean old=Features.enabled("blocksitems.enableVanillaSigns"),modern=Features.enabled("blocksitems.enableNewSigns");for(String wood:new String[]{"spruce","birch","jungle","acacia","dark_oak","crimson","warped","mangrove","cherry","bamboo","pale_oak"}){boolean legacy=Arrays.asList("spruce","birch","jungle","acacia","dark_oak").contains(wood);String flag=legacy?"blocksitems.enableVanillaSigns":Content.FEATURE.get(wood+"_planks"),blockId=legacy?"sign_"+wood:wood+"_sign",wallId=legacy?"wall_sign_"+wood:wood+"_wall_sign",itemId=legacy?"item_sign_"+wood:blockId;Standing standing=Content.add(blockId,flag,new Standing(wood,itemId));Wall wall=Content.add(wallId,flag,new Wall(wood,itemId));standing.wall=wall;SIGNS.put(wood,standing);Content.add(itemId,flag,new SignItem(standing));if(!legacy){Content.requires(blockId,"blocksitems.enableNewSigns");Content.requires(wallId,"blocksitems.enableNewSigns");Content.requires(itemId,"blocksitems.enableNewSigns");}wall.setCreativeTab(null);if(!wood.equals("crimson")&&!wood.equals("warped")){Blocks.FIRE.setFireInfo(standing,5,20);Blocks.FIRE.setFireInfo(wall,5,20);}}GameRegistry.registerTileEntity(SignTile.class,new ResourceLocation(UAB.ID,"wood_sign"));}
 public static class Standing extends BlockStandingSign implements Sign {private final String wood,item;Wall wall;Standing(String wood,String item){this.wood=wood;this.item=item;setHardness(1);setSoundType(SoundType.WOOD);}public String wood(){return wood;}public boolean standing(){return true;}public String itemId(){return item;}@Override public TileEntity createNewTileEntity(World world,int meta){return new SignTile();}@Override public Item getItemDropped(IBlockState state,Random random,int fortune){return Content.item(item);}@Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return new ItemStack(Content.item(item));}}
 public static class Wall extends BlockWallSign implements Sign {private final String wood,item;Wall(String wood,String item){this.wood=wood;this.item=item;setHardness(1);setSoundType(SoundType.WOOD);}public String wood(){return wood;}public boolean standing(){return false;}public String itemId(){return item;}@Override public TileEntity createNewTileEntity(World world,int meta){return new SignTile();}@Override public Item getItemDropped(IBlockState state,Random random,int fortune){return Content.item(item);}@Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return new ItemStack(Content.item(item));}}
 public static class SignTile extends TileEntitySign {@Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState before,IBlockState after){return before.getBlock()!=after.getBlock();}}
 public static class SignItem extends ItemBlock {
  private final Standing sign;SignItem(Standing sign){super(sign);this.sign=sign;setMaxStackSize(16);}
  @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos clicked,EnumHand hand,EnumFacing side,float x,float y,float z){ItemStack stack=player.getHeldItem(hand);if(side==EnumFacing.DOWN||!world.getBlockState(clicked).getMaterial().isSolid())return EnumActionResult.FAIL;BlockPos pos=clicked.offset(side);if(!player.canPlayerEdit(pos,side,stack)||!world.mayPlace(sign,pos,false,side,player))return EnumActionResult.FAIL;if(world.isRemote)return EnumActionResult.SUCCESS;IBlockState state=side==EnumFacing.UP?sign.getDefaultState().withProperty(BlockStandingSign.ROTATION,MathHelper.floor((player.rotationYaw+180)*16/360+.5)&15):sign.wall.getDefaultState().withProperty(BlockWallSign.FACING,side);if(!world.setBlockState(pos,state,11))return EnumActionResult.FAIL;TileEntity tile=world.getTileEntity(pos);if(tile instanceof TileEntitySign){TileEntitySign text=(TileEntitySign)tile;if(!ItemBlock.setTileEntityNBT(world,player,pos,stack)){text.setPlayer(player);if(player instanceof EntityPlayerMP)((EntityPlayerMP)player).connection.sendPacket(new net.minecraft.network.play.server.SPacketBlockChange(world,pos));player.openEditSign(text);}}if(!player.capabilities.isCreativeMode)stack.shrink(1);world.playSound(null,pos,SoundEvents.BLOCK_WOOD_PLACE,SoundCategory.BLOCKS,1,.8F);return EnumActionResult.SUCCESS;}
 }
}
