package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.client.renderer.entity.RenderCow;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=RenderCow.class,remap=false)
public abstract class SpringCowTextureMixin extends net.minecraft.client.renderer.entity.RenderLiving<EntityCow> {
 @Inject(method={"getEntityTexture(Lnet/minecraft/entity/passive/EntityCow;)Lnet/minecraft/util/ResourceLocation;","func_110775_a(Lnet/minecraft/entity/passive/EntityCow;)Lnet/minecraft/util/ResourceLocation;"},at=@At("HEAD"),cancellable=true,remap=false,require=0,expect=0) private void uab$texture(EntityCow entity,CallbackInfoReturnable<ResourceLocation> result){uab$prepare(entity);String texture=SpringAnimals.texture(entity);if(texture!=null){result.setReturnValue(new ResourceLocation(texture));}}
 protected SpringCowTextureMixin() { super(null, new net.minecraft.client.model.ModelCow(), .7F); }
 @Unique private net.minecraft.client.model.ModelBase uab$original;
 @Unique private final net.minecraft.client.model.ModelBase[] uab$cows = new net.minecraft.client.model.ModelBase[3];
 // Use the already mapped texture hook: a new generic pre-render bridge is renamed by Mixin on 1.12.2.
 @Unique private void uab$prepare(EntityCow entity) {
     if (uab$original == null) uab$original = mainModel;
     if (SpringAnimals.texture(entity) == null) { mainModel = uab$original; mainModel.isChild = entity.isChild(); mainModel.isRiding = entity.isRiding(); return; }
     int coat = Math.max(0, Math.min(2, entity.getEntityData().getInteger(SpringAnimals.COAT)));
     if (uab$cows[coat] == null) uab$cows[coat] = new ganymedes01.etfuturum.client.model.ModelSpringCow(coat);
     mainModel = uab$cows[coat];
     mainModel.isChild = entity.isChild(); mainModel.isRiding = entity.isRiding();
 }
}
