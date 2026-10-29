package ganymedes01.etfuturum.mixins.early.mace;
import ganymedes01.etfuturum.combat.MaceCombat;
import ganymedes01.etfuturum.items.ItemMace;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityMob.class)
public class MixinMobAttack {
    @Redirect(method="attackEntityAsMob",at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean etfu$smash(Entity victim,DamageSource source,float damage){return victim.attackEntityFrom(source,MaceCombat.damage(source,victim,damage));}
    @Inject(method="attackEntityAsMob",at=@At("RETURN"))
    private void etfu$impact(Entity victim,CallbackInfoReturnable<Boolean> result){EntityLivingBase entity=(EntityLivingBase)(Object)this;if(result.getReturnValue()&&victim instanceof EntityLivingBase&&entity.getHeldItem()!=null&&entity.getHeldItem().getItem() instanceof ItemMace)MaceCombat.impact(entity,(EntityLivingBase)victim);}
}
