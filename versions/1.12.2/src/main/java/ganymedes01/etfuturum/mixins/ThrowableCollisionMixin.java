package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EntityThrowable.class,remap=false)
public abstract class ThrowableCollisionMixin extends Entity {
 public ThrowableCollisionMixin(World world){super(world);}
 @Inject(method={"onUpdate","func_70071_h_"},at=@At("TAIL"),remap=false)
 private void uab$collision(CallbackInfo ci){if(!isDead&&Features.enabled("mixins.collidedThrowableFix"))doBlockCollisions();}
}
