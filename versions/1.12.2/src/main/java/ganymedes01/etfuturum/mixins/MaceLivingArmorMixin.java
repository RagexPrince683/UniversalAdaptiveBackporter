package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.MaceCombat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=EntityLivingBase.class,remap=false)
public abstract class MaceLivingArmorMixin {
    @Shadow(aliases={"func_70655_b"}) protected abstract float applyArmorCalculations(DamageSource source,float damage);
    private float etfu$armor(DamageSource source,float damage){return (float)MaceCombat.armor(damage,applyArmorCalculations(source,damage),source);}
    @Group(name="maceArmor",min=1) @Redirect(method={"damageEntity","func_70665_d"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/EntityLivingBase;applyArmorCalculations(Lnet/minecraft/util/DamageSource;F)F"),require=0,remap=false)
    private float etfu$mcp(EntityLivingBase entity,DamageSource source,float damage){return etfu$armor(source,damage);}
    @Group(name="maceArmor",min=1) @Redirect(method={"damageEntity","func_70665_d"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/EntityLivingBase;func_70655_b(Lnet/minecraft/util/DamageSource;F)F"),require=0,remap=false)
    private float etfu$srg(EntityLivingBase entity,DamageSource source,float damage){return etfu$armor(source,damage);}
}
