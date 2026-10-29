package ganymedes01.etfuturum.mixins.early.spring.client;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.client.renderer.entity.RenderPig;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=RenderPig.class)
public class MixinRenderPig {
 @Inject(method={"getEntityTexture(Lnet/minecraft/entity/passive/EntityPig;)Lnet/minecraft/util/ResourceLocation;"},at=@At("HEAD"),cancellable=true,require=0) private void uab$texture(EntityPig entity,CallbackInfoReturnable<ResourceLocation> result){String texture=SpringAnimals.texture(entity);if(texture!=null){result.setReturnValue(new ResourceLocation(texture));}}
}
