package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.LightningRodTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=WorldServer.class,remap=false)
public class LightningAttractionMixin {
    @Inject(method={"adjustPosToNearbyEntity","func_175736_a"},at=@At("RETURN"),cancellable=true,remap=false)
    private void etfu$rod(BlockPos original,CallbackInfoReturnable<BlockPos> ci){ci.setReturnValue(LightningRodTile.attract((WorldServer)(Object)this,ci.getReturnValue()));}
}
