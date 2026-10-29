package ganymedes01.etfuturum.mixins.early.spring;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityPig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntityPig.class)
public class MixinPigBreeding {
 @Inject(method="createChild(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityPig;",at=@At("RETURN")) private void uab$inherit(EntityAgeable mate,CallbackInfoReturnable<EntityPig> child){SpringAnimals.inherit((EntityAgeable)(Object)this,mate,child.getReturnValue());}
}
