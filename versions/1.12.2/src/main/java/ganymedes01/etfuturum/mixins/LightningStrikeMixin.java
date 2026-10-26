package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EntityLightningBolt.class,remap=false)
public class LightningStrikeMixin {
    @Inject(method="<init>",at=@At("RETURN"),remap=false)
    private void etfu$strike(World world,double x,double y,double z,boolean effectOnly,CallbackInfo ci){if(world.isRemote||effectOnly)return;BlockPos pos=new BlockPos(x,y,z);if(world.isBlockLoaded(pos.down())&&world.getBlockState(pos.down()).getBlock() instanceof LightningRod)((LightningRod)world.getBlockState(pos.down()).getBlock()).strike(world,pos.down());CopperAging.lightning(world,pos);}
}
