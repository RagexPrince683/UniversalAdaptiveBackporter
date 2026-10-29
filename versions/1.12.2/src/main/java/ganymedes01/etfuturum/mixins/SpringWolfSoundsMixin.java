package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=EntityWolf.class,remap=false)
public class SpringWolfSoundsMixin {
    @Inject(method={"getAmbientSound","func_184639_G","getHurtSound","func_184601_bQ","getDeathSound","func_184615_bR"},at=@At("RETURN"),cancellable=true,remap=false)
    private void uab$voice(CallbackInfoReturnable<SoundEvent> result){if(!SpringAnimals.enabled()||result.getReturnValue()==null||result.getReturnValue().getRegistryName()==null)return;int voice=((EntityWolf)(Object)this).getEntityData().getInteger(SpringAnimals.VOICE);if(voice<=0||voice>=SpringAnimals.VOICES.length)return;String original=result.getReturnValue().getRegistryName().getPath(),kind=original.substring(original.lastIndexOf('.')+1);result.setReturnValue(ModernSounds.get("entity.wolf_"+SpringAnimals.VOICES[voice]+"."+kind));}
}
