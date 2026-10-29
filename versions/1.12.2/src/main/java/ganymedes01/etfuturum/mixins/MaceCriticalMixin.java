package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.MaceCombat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=EntityPlayer.class,remap=false)
public class MaceCriticalMixin {
    @Group(name="macePlayerHit",min=1) @Redirect(method={"attackTargetEntityWithCurrentItem","func_71059_n"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"),require=0,expect=0,remap=false)
    private boolean etfu$mcp(Entity victim,DamageSource source,float damage){return victim.attackEntityFrom(source,MaceCombat.damage(source,victim,damage));}
    @Group(name="macePlayerHit",min=1) @Redirect(method={"attackTargetEntityWithCurrentItem","func_71059_n"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;func_70097_a(Lnet/minecraft/util/DamageSource;F)Z"),require=0,expect=0,remap=false)
    private boolean etfu$srg(Entity victim,DamageSource source,float damage){return victim.attackEntityFrom(source,MaceCombat.damage(source,victim,damage));}
    @Redirect(method={"attackTargetEntityWithCurrentItem","func_71059_n"},at=@At(value="INVOKE",target="Lnet/minecraftforge/common/ForgeHooks;getCriticalHit(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;ZF)Lnet/minecraftforge/event/entity/player/CriticalHitEvent;"),remap=false)
    private CriticalHitEvent etfu$critical(EntityPlayer player,Entity target,boolean vanilla,float modifier){
        CriticalHitEvent result=ForgeHooks.getCriticalHit(player,target,vanilla,modifier);
        MaceCombat.critical(player,target,result==null?1:result.getDamageModifier());
        return result;
    }
}
