package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.ModernBlockSounds;
import net.minecraft.block.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=Block.class,remap=false)
public abstract class BlockSoundMixin {
 @Inject(method={"getSoundType","func_185467_w"},at=@At("RETURN"),cancellable=true,remap=false)
 private void uab$sound(CallbackInfoReturnable<SoundType> ci){ci.setReturnValue(ModernBlockSounds.resolve((Block)(Object)this,ci.getReturnValue()));}
}
