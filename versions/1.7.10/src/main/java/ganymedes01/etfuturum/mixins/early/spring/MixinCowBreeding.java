package ganymedes01.etfuturum.mixins.early.spring;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityCow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntityCow.class)
public class MixinCowBreeding {
 @Inject(method="createChild(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityCow;",at=@At("RETURN")) private void uab$inherit(EntityAgeable mate,CallbackInfoReturnable<EntityCow> child){SpringAnimals.inherit((EntityAgeable)(Object)this,mate,child.getReturnValue());}
}
