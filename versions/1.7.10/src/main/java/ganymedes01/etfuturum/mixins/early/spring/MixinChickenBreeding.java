package ganymedes01.etfuturum.mixins.early.spring;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityChicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntityChicken.class)
public class MixinChickenBreeding {
 @Inject(method="createChild(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityChicken;",at=@At("RETURN")) private void uab$inherit(EntityAgeable mate,CallbackInfoReturnable<EntityChicken> child){SpringAnimals.inherit((EntityAgeable)(Object)this,mate,child.getReturnValue());}
}
