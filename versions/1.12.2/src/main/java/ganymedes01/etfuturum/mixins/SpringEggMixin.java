package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.world.World;
@Mixin(value=EntityEgg.class,remap=false)
public class SpringEggMixin {
 @Group(name="springEgg",min=1) @Redirect(method={"onImpact","func_70184_a"},at=@At(value="INVOKE",target="Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),require=0,remap=false) private boolean uab$mcp(World world,Entity spawned){if(SpringAnimals.enabled()&&spawned instanceof EntityChicken)spawned.getEntityData().setInteger(SpringAnimals.COAT,((EntityEgg)(Object)this).getEntityData().getInteger(SpringAnimals.COAT));return world.spawnEntity(spawned);}
 @Group(name="springEgg",min=1) @Redirect(method={"onImpact","func_70184_a"},at=@At(value="INVOKE",target="Lnet/minecraft/world/World;func_72838_d(Lnet/minecraft/entity/Entity;)Z"),require=0,remap=false) private boolean uab$srg(World world,Entity spawned){if(SpringAnimals.enabled()&&spawned instanceof EntityChicken)spawned.getEntityData().setInteger(SpringAnimals.COAT,((EntityEgg)(Object)this).getEntityData().getInteger(SpringAnimals.COAT));return world.spawnEntity(spawned);}
}
