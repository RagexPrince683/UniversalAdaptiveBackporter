package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
/** Saved generic metal slots forward actual native base states, drops, harvest rules and experience. */
public class ModdedOreBlock extends Block {
 public static final PropertyInteger VARIANT=PropertyInteger.create("variant",0,15);
 public final boolean deepslate;
 ModdedOreBlock(boolean deepslate){super(Material.ROCK);this.deepslate=deepslate;setHardness(deepslate?(Features.enabled("functions.useStoneHardnessForDeepslate")?3:4.5F):5);setResistance(deepslate?3:6);setHarvestLevel("pickaxe",1);setDefaultState(blockState.getBaseState().withProperty(VARIANT,0));}
 @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,VARIANT);}
 @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(VARIANT,meta&15);}
 @Override public int getMetaFromState(IBlockState state){return state.getValue(VARIANT);}
 public int variants(){return 14;}
 public String variant(int meta){return (deepslate?"deepslate_":"raw_")+OreCompatibility.METALS[Math.max(0,Math.min(13,meta))]+(deepslate?"_ore":"_block");}
 public boolean available(int meta){return deepslate?OreCompatibility.deepAvailable(meta):OreCompatibility.rawAvailable(meta);}
 @Override public int damageDropped(IBlockState state){return getMetaFromState(state);}
 @Override public void getSubBlocks(CreativeTabs tab,NonNullList<ItemStack> items){if(tab==getCreativeTab()||tab==CreativeTabs.SEARCH)for(int meta=0;meta<variants();meta++)if(available(meta))items.add(new ItemStack(this,1,meta));}
 protected IBlockState base(IBlockState state){if(state.getBlock()!=this)return null;return deepslate?OreCompatibility.baseOre(getMetaFromState(state)):OreCompatibility.baseStorage(getMetaFromState(state));}
 @Override public int getHarvestLevel(IBlockState state){IBlockState base=base(state);return base==null?1:base.getBlock().getHarvestLevel(base);}
 @Override public String getHarvestTool(IBlockState state){IBlockState base=base(state);return base==null?"pickaxe":base.getBlock().getHarvestTool(base);}
 @Override public float getBlockHardness(IBlockState state,World world,BlockPos pos){IBlockState base=base(state);float hardness=base==null?super.getBlockHardness(state,world,pos):base.getBlock().getBlockHardness(base,world,pos);return deepslate&&base!=null&&!Features.enabled("functions.useStoneHardnessForDeepslate")?hardness*1.5F:hardness;}
 @Override public float getExplosionResistance(World world,BlockPos pos,Entity entity,Explosion explosion){IBlockState base=base(world.getBlockState(pos));return base==null?super.getExplosionResistance(world,pos,entity,explosion):base.getBlock().getExplosionResistance(world,pos,entity,explosion);}
 @Override public int getExpDrop(IBlockState state,IBlockAccess world,BlockPos pos,int fortune){IBlockState base=base(state);return !deepslate||base==null?0:base.getBlock().getExpDrop(base,world,pos,fortune);}
 @Override public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune){IBlockState base=base(state);if(!deepslate||base==null){drops.add(new ItemStack(this,1,getMetaFromState(state)));return;}NonNullList<ItemStack> original=NonNullList.create();base.getBlock().getDrops(original,world,pos,base,fortune);for(ItemStack stack:original){if(stack.getItem()==Item.getItemFromBlock(base.getBlock())&&(stack.getMetadata()==base.getBlock().damageDropped(base)||stack.getMetadata()==base.getBlock().getMetaFromState(base))){ItemStack replacement=new ItemStack(this,stack.getCount(),getMetaFromState(state));if(stack.hasTagCompound())replacement.setTagCompound(stack.getTagCompound().copy());drops.add(replacement);}else drops.add(stack);}}
}
