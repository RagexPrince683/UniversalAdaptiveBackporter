package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.NetherBiomes;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=WorldProviderHell.class,remap=false)
public abstract class NetherProviderMixin extends WorldProvider {
    @Inject(method={"init","func_76572_b"},at=@At("TAIL"),remap=false)
    private void etfu$biomes(CallbackInfo ci){biomeProvider=NetherBiomes.provider(world,biomeProvider);}
}
