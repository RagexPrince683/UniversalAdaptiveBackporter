package ganymedes01.etfuturum.mixins.early.spring;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.world.World;
@Mixin(value=EntityEgg.class)
public class MixinEgg {
 @Redirect(method="onImpact",at=@At(value="INVOKE",target="Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z")) private boolean uab$coat(World world,Entity spawned){if(SpringAnimals.enabled()&&spawned instanceof EntityChicken)spawned.getEntityData().setInteger(SpringAnimals.COAT,((EntityEgg)(Object)this).getEntityData().getInteger(SpringAnimals.COAT));return world.spawnEntityInWorld(spawned);}
}
