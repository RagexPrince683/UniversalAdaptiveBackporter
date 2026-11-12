package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import net.minecraft.entity.monster.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EntityHusk.class,remap=false)
public abstract class HuskSpawningMixin extends EntityZombie {
 protected HuskSpawningMixin(World world){super(world);}
 @Inject(method="<init>",at=@At("RETURN"),remap=false)
 private void uab$sink(World world,CallbackInfo ci){
  for(net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry entry:new java.util.ArrayList<>(tasks.taskEntries))
   if(entry.action instanceof net.minecraft.entity.ai.EntityAISwimming)tasks.removeTask(entry.action);
  ((net.minecraft.pathfinding.PathNavigateGround)getNavigator()).setCanSwim(false);
 }
 @Override public boolean canBreatheUnderwater(){return true;}
 @Inject(method={"getCanSpawnHere","func_70601_bi"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$legacy(CallbackInfoReturnable<Boolean> ci){if(Features.enabled("world.oldHuskSpawning"))ci.setReturnValue(super.getCanSpawnHere());}
}
