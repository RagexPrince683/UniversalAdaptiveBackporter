package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=BlockFire.class,remap=false)
public class SoulFireMixin {
    @Inject(method={"onBlockAdded","func_176213_c"},at=@At("RETURN"),remap=false)
    private void etfu$soul(World world,BlockPos pos,IBlockState state,CallbackInfo ci){if((Object)this!=Blocks.FIRE||world.isRemote||!Features.enabled("mixins.soulFire")||world.getBlockState(pos).getBlock()!=Blocks.FIRE)return;net.minecraft.block.Block soil=world.getBlockState(pos.down()).getBlock();if(soil==Blocks.SOUL_SAND||soil==Content.BLOCKS.get("soul_soil"))world.setBlockState(pos,Content.block("soul_fire").getDefaultState(),3);}
}
