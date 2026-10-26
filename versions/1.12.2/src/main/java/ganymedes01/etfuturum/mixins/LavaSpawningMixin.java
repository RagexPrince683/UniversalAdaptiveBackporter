package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.MobRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=WorldEntitySpawner.class,remap=false)
public class LavaSpawningMixin {
    @Inject(method={"canCreatureTypeSpawnAtLocation","func_180267_a"},at=@At("HEAD"),cancellable=true,remap=false)
    private static void etfu$lava(EntityLiving.SpawnPlacementType type,World world,BlockPos pos,CallbackInfoReturnable<Boolean> ci){if(type==MobRegistry.LAVA)ci.setReturnValue(world.provider.isNether()&&world.isAreaLoaded(pos.down(),pos.up())&&world.getWorldBorder().contains(pos)&&world.getBlockState(pos.down()).getMaterial()==Material.LAVA&&world.isAirBlock(pos)&&world.isAirBlock(pos.up()));}
}
