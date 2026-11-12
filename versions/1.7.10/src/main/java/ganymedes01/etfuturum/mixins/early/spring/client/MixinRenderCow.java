package ganymedes01.etfuturum.mixins.early.spring.client;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.client.renderer.entity.RenderCow;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=RenderCow.class)
public abstract class MixinRenderCow extends net.minecraft.client.renderer.entity.RenderLiving {
 @Inject(method={"getEntityTexture(Lnet/minecraft/entity/passive/EntityCow;)Lnet/minecraft/util/ResourceLocation;"},at=@At("HEAD"),cancellable=true,require=0,expect=0) private void uab$texture(EntityCow entity,CallbackInfoReturnable<ResourceLocation> result){String texture=SpringAnimals.texture(entity);if(texture!=null){result.setReturnValue(new ResourceLocation(texture));}}
 protected MixinRenderCow() { super(new net.minecraft.client.model.ModelCow(), .7F); }
 @Unique private net.minecraft.client.model.ModelBase uab$original;
 @Unique private final net.minecraft.client.model.ModelBase[] uab$cows = new net.minecraft.client.model.ModelBase[3];
 @Override protected void preRenderCallback(net.minecraft.entity.EntityLivingBase entity, float partial) {
     super.preRenderCallback(entity, partial);
     if (uab$original == null) uab$original = mainModel;
     if (SpringAnimals.texture(entity) == null) { mainModel = uab$original; mainModel.isChild = entity.isChild(); mainModel.isRiding = entity.isRiding(); return; }
     int coat = Math.max(0, Math.min(2, entity.getEntityData().getInteger(SpringAnimals.COAT)));
     if (uab$cows[coat] == null) uab$cows[coat] = new ganymedes01.etfuturum.client.model.ModelSpringCow(coat);
     mainModel = uab$cows[coat];
     mainModel.isChild = entity.isChild(); mainModel.isRiding = entity.isRiding();
 }
}
