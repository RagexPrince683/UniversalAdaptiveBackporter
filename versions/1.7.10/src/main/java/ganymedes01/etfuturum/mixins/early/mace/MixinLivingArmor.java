package ganymedes01.etfuturum.mixins.early.mace;
import ganymedes01.etfuturum.combat.MaceCombat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityLivingBase.class)
public abstract class MixinLivingArmor {
    @Shadow protected abstract float applyArmorCalculations(DamageSource source, float damage);
    @Redirect(method="damageEntity",at=@At(value="INVOKE",target="Lnet/minecraft/entity/EntityLivingBase;applyArmorCalculations(Lnet/minecraft/util/DamageSource;F)F"))
    private float etfu$breach(EntityLivingBase entity,DamageSource source,float damage){return (float)MaceCombat.armor(damage,applyArmorCalculations(source,damage),source);}
}
