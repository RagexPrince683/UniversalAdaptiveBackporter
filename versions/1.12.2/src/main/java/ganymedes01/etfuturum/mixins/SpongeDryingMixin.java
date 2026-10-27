package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import net.minecraft.block.BlockSponge;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=BlockSponge.class,remap=false)
public abstract class SpongeDryingMixin {
 @Inject(method={"onBlockAdded","func_176213_c"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$dry(World world,BlockPos pos,IBlockState state,CallbackInfo ci){if(!world.isRemote&&Features.enabled("blocksitems.enableSponge")&&state.getValue(BlockSponge.WET)&&BiomeDictionary.hasType(world.getBiome(pos),BiomeDictionary.Type.NETHER)){world.setBlockState(pos,state.withProperty(BlockSponge.WET,false),2);world.playSound(null,pos,SoundEvents.BLOCK_FIRE_EXTINGUISH,SoundCategory.BLOCKS,1,1);ci.cancel();}}
}
