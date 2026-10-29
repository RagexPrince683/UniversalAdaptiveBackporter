package ganymedes01.etfuturum.mixins.early.spring;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EntityAnimal.class)
public class MixinAnimalData {
    @Inject(method="readEntityFromNBT",at=@At("RETURN"))private void uab$loaded(NBTTagCompound tag,CallbackInfo info){SpringAnimals.loaded((Entity)(Object)this,tag);}
}
