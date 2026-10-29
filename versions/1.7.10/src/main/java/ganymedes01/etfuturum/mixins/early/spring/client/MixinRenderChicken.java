package ganymedes01.etfuturum.mixins.early.spring.client;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.client.renderer.entity.RenderChicken;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=RenderChicken.class)
public class MixinRenderChicken {
 @Inject(method={"getEntityTexture(Lnet/minecraft/entity/passive/EntityChicken;)Lnet/minecraft/util/ResourceLocation;"},at=@At("HEAD"),cancellable=true,require=0,expect=0) private void uab$texture(EntityChicken entity,CallbackInfoReturnable<ResourceLocation> result){String texture=SpringAnimals.texture(entity);if(texture!=null){result.setReturnValue(new ResourceLocation(texture));}}
}
