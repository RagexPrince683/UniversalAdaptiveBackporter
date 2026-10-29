package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.MaceCombat;
import ganymedes01.etfuturum.MaceItem;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=EntityMob.class,remap=false)
public class MaceMobAttackMixin {
    @Group(name="maceMobHit",min=1) @Redirect(method={"attackEntityAsMob","func_70652_k"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"),require=0,expect=0,remap=false)
    private boolean etfu$mcp(Entity victim,DamageSource source,float damage){return victim.attackEntityFrom(source,MaceCombat.damage(source,victim,damage));}
    @Group(name="maceMobHit",min=1) @Redirect(method={"attackEntityAsMob","func_70652_k"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;func_70097_a(Lnet/minecraft/util/DamageSource;F)Z"),require=0,expect=0,remap=false)
    private boolean etfu$srg(Entity victim,DamageSource source,float damage){return victim.attackEntityFrom(source,MaceCombat.damage(source,victim,damage));}
    @Inject(method={"attackEntityAsMob","func_70652_k"},at=@At("RETURN"),remap=false)
    private void etfu$impact(Entity victim,CallbackInfoReturnable<Boolean> result){EntityLivingBase entity=(EntityLivingBase)(Object)this;if(result.getReturnValue()&&victim instanceof EntityLivingBase&&entity.getHeldItemMainhand().getItem() instanceof MaceItem)MaceCombat.impact(entity,(EntityLivingBase)victim);}
}
