package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.world.DifficultyInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=EntitySheep.class,remap=false)
public class SpringSheepMixin {
    @Inject(method={"onInitialSpawn","func_180482_a"},at=@At("RETURN"),remap=false)private void uab$climate(DifficultyInstance difficulty,IEntityLivingData data,CallbackInfoReturnable<IEntityLivingData> result){SpringAnimals.sheep((EntitySheep)(Object)this);}
}
