package ganymedes01.etfuturum.mixins.early.sculk;
import ganymedes01.etfuturum.core.handlers.SculkEventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(EntityLivingBase.class)
public abstract class MixinCatalystExperience {
 @Shadow protected abstract int getExperiencePoints(EntityPlayer player);
 @Redirect(method="onDeathUpdate",at=@At(value="INVOKE",target="Lnet/minecraft/entity/EntityLivingBase;getExperiencePoints(Lnet/minecraft/entity/player/EntityPlayer;)I"))
 private int uab$charge(EntityLivingBase entity,EntityPlayer player){return SculkEventHandler.absorb(entity,getExperiencePoints(player));}
}
