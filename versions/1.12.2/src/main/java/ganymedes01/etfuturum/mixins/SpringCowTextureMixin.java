package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.client.renderer.entity.RenderCow;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=RenderCow.class,remap=false)
public class SpringCowTextureMixin {
 @Inject(method={"getEntityTexture(Lnet/minecraft/entity/passive/EntityCow;)Lnet/minecraft/util/ResourceLocation;","func_110775_a(Lnet/minecraft/entity/passive/EntityCow;)Lnet/minecraft/util/ResourceLocation;"},at=@At("HEAD"),cancellable=true,remap=false,require=0,expect=0) private void uab$texture(EntityCow entity,CallbackInfoReturnable<ResourceLocation> result){String texture=SpringAnimals.texture(entity);if(texture!=null){result.setReturnValue(new ResourceLocation(texture));}}
}
