package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.RaidProgress;
import net.minecraft.entity.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EntityLiving.class)
public abstract class RaidMembershipMixin {
    @Inject(method={"canDespawn()Z","func_70692_ba()Z"},at=@At("HEAD"),cancellable=true,remap=false)
    private void uab$raidOwnership(CallbackInfoReturnable<Boolean> callback){EntityLiving mob=(EntityLiving)(Object)this;int raid=RaidProgress.membership(mob);if(raid!=0&&!mob.world.isRemote&&RaidProgress.get(mob.world).isActive(raid))callback.setReturnValue(false);}
}
