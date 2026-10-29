package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EntityAnimal.class,remap=false)
public class SpringAnimalDataMixin {
    @Inject(method={"readEntityFromNBT","func_70037_a"},at=@At("RETURN"),remap=false)private void uab$loaded(NBTTagCompound tag,CallbackInfo info){SpringAnimals.loaded((Entity)(Object)this,tag);}
}
