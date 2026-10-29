package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value={EntityCow.class,EntityPig.class,EntityChicken.class,EntityWolf.class},remap=false)
public class SpringAnimalBreedingMixin {
 @Inject(method={"createChild(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityCow;","createChild(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityPig;","createChild(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityChicken;","createChild(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityWolf;","func_90011_a(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityCow;","func_90011_a(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityPig;","func_90011_a(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityChicken;","func_90011_a(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/passive/EntityWolf;"},at=@At("RETURN"),remap=false) private void uab$inherit(EntityAgeable mate,CallbackInfoReturnable<EntityAgeable> child){SpringAnimals.inherit((EntityAgeable)(Object)this,mate,child.getReturnValue());}
}
