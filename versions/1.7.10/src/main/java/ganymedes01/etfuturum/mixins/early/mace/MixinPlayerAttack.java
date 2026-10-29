package ganymedes01.etfuturum.mixins.early.mace;
import ganymedes01.etfuturum.combat.MaceCombat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityPlayer.class)
public class MixinPlayerAttack {
    @Redirect(method="attackTargetEntityWithCurrentItem",at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean etfu$smash(Entity victim,DamageSource source,float damage){return victim.attackEntityFrom(source,MaceCombat.damage(source,victim,damage));}
}
