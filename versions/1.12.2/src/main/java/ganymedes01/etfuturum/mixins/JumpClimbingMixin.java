package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=EntityLivingBase.class,remap=false)
public abstract class JumpClimbingMixin {
 @Group(name="jumpClimbing",min=1) @Redirect(method={"travel","func_191986_a"},slice=@Slice(from=@At(value="INVOKE",target="Lnet/minecraft/entity/EntityLivingBase;isOnLadder()Z",ordinal=0),to=@At(value="INVOKE",target="Lnet/minecraft/entity/EntityLivingBase;isOnLadder()Z",ordinal=1)),at=@At(value="FIELD",target="Lnet/minecraft/entity/EntityLivingBase;collidedHorizontally:Z"),require=0,expect=0,remap=false)
 private boolean uab$climb(EntityLivingBase entity){return entity.collidedHorizontally||Features.enabled("mixins.enableClimbingPlants")&&LivingRules.jumping(entity);}
 @Group(name="jumpClimbing",min=1) @Redirect(method={"travel","func_191986_a"},slice=@Slice(from=@At(value="INVOKE",target="Lnet/minecraft/entity/EntityLivingBase;func_70617_f_()Z",ordinal=0),to=@At(value="INVOKE",target="Lnet/minecraft/entity/EntityLivingBase;func_70617_f_()Z",ordinal=1)),at=@At(value="FIELD",target="Lnet/minecraft/entity/EntityLivingBase;field_70123_F:Z"),require=0,expect=0,remap=false)
 private boolean uab$climbSrg(EntityLivingBase entity){return entity.collidedHorizontally||Features.enabled("mixins.enableClimbingPlants")&&LivingRules.jumping(entity);}
}
