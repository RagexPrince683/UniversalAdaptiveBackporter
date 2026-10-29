package ganymedes01.etfuturum.mixins.early.spring;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntitySheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntitySheep.class)
public class MixinSheep {
    @Inject(method="onSpawnWithEgg",at=@At("RETURN"))private void uab$climate(IEntityLivingData data,CallbackInfoReturnable<IEntityLivingData> result){SpringAnimals.sheep((EntitySheep)(Object)this);}
}
