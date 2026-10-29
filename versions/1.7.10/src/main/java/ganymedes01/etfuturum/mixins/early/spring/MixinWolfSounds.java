package ganymedes01.etfuturum.mixins.early.spring;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.passive.EntityWolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntityWolf.class)
public class MixinWolfSounds {
    @Inject(method={"getLivingSound","getHurtSound","getDeathSound"},at=@At("RETURN"),cancellable=true)
    private void uab$voice(CallbackInfoReturnable<String> result){if(!SpringAnimals.enabled()||result.getReturnValue()==null)return;int voice=((EntityWolf)(Object)this).getEntityData().getInteger(SpringAnimals.VOICE);if(voice<=0||voice>=SpringAnimals.VOICES.length)return;String original=result.getReturnValue(),kind=original.substring(original.lastIndexOf('.')+1);if(kind.equals("bark"))kind="ambient";result.setReturnValue("minecraft_1.21.11:entity.wolf_"+SpringAnimals.VOICES[voice]+"."+kind);}
}
