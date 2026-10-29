package ganymedes01.etfuturum.mixins.early.spring.client;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=RenderWolf.class)
public class MixinRenderWolf {
 @Inject(method={"getEntityTexture(Lnet/minecraft/entity/passive/EntityWolf;)Lnet/minecraft/util/ResourceLocation;"},at=@At("HEAD"),cancellable=true,require=0,expect=0) private void uab$texture(EntityWolf entity,CallbackInfoReturnable<ResourceLocation> result){String texture=SpringAnimals.texture(entity);if(texture!=null){result.setReturnValue(new ResourceLocation(texture));}}
}
