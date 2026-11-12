package ganymedes01.etfuturum.mixins.early.nethermobs;
import ganymedes01.etfuturum.potion.ModPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Apply active water drag after movement without competing for another mod's constants. */
@Mixin(EntityLivingBase.class)
public class MixinDolphinGrace {
    @Unique private boolean etfu$graceActive;
    @Unique private double etfu$waterMotionX, etfu$waterMotionZ;

    @Inject(method = "moveEntityWithHeading", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityLivingBase;moveEntity(DDD)V", ordinal = 0, shift = At.Shift.AFTER), require = 1)
    private void etfu$captureWaterMotion(float strafe, float forward, CallbackInfo ci) {
        EntityLivingBase living = (EntityLivingBase) (Object) this;
        etfu$graceActive = living instanceof EntityPlayer && ModPotions.dolphinGrace != null
                && living.isPotionActive(ModPotions.dolphinGrace);
        etfu$waterMotionX = living.motionX;
        etfu$waterMotionZ = living.motionZ;
    }

    @Inject(method = "moveEntityWithHeading", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/EntityLivingBase;motionZ:D", opcode = Opcodes.PUTFIELD,
            ordinal = 0, shift = At.Shift.AFTER), require = 1)
    private void etfu$applyWaterDrag(float strafe, float forward, CallbackInfo ci) {
        if (etfu$graceActive) {
            EntityLivingBase living = (EntityLivingBase) (Object) this;
            living.motionX = etfu$waterMotionX * .96D;
            living.motionZ = etfu$waterMotionZ * .96D;
        }
    }
}
