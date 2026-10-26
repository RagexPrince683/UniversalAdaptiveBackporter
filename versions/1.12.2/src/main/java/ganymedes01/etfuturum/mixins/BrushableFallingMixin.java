package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.BrushableBlock;
import net.minecraft.entity.item.EntityFallingBlock;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EntityFallingBlock.class,remap=false)
public class BrushableFallingMixin {
    @Shadow(aliases="field_145808_f",remap=false) private boolean dontSetBlock;
    @Inject(method={"onUpdate","func_70071_h_"},at=@At("HEAD"),remap=false)
    private void etfu$fragile(CallbackInfo ci){EntityFallingBlock falling=(EntityFallingBlock)(Object)this;if(falling.getBlock()!=null&&falling.getBlock().getBlock() instanceof BrushableBlock){dontSetBlock=true;falling.shouldDropItem=false;}}
}
