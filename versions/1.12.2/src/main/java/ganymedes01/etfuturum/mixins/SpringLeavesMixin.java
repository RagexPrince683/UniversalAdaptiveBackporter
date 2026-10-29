package ganymedes01.etfuturum.mixins;
import java.util.Random;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=BlockLeaves.class,remap=false)
public class SpringLeavesMixin {
    @Inject(method={"randomDisplayTick","func_180655_c"},at=@At("RETURN"),remap=false)private void uab$leaves(IBlockState state,World world,BlockPos pos,Random random,CallbackInfo info){if(SpringAnimals.enabled()&&random.nextInt(35)==0&&world.isAirBlock(pos.down()))ganymedes01.etfuturum.client.FallingLeafParticle.spawn(world,pos,state);}
}
