package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.SleepingRules;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=WorldServer.class,remap=false)
public abstract class SleepingRulesMixin {
 @Inject(method={"areAllPlayersAsleep","func_73056_e"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$asleep(CallbackInfoReturnable<Boolean> ci){if(SleepingRules.enabled())ci.setReturnValue(SleepingRules.asleep((WorldServer)(Object)this));}
 @Inject(method={"updateAllPlayersSleepingFlag","func_72854_c"},at=@At("TAIL"),remap=false)
 private void uab$update(CallbackInfo ci){if(SleepingRules.enabled())SleepingRules.announce((WorldServer)(Object)this);}
 @Inject(method={"wakeAllPlayers","func_73053_d"},at=@At("HEAD"),remap=false)
 private void uab$wake(CallbackInfo ci){if(SleepingRules.enabled())SleepingRules.waking((WorldServer)(Object)this);}
}
