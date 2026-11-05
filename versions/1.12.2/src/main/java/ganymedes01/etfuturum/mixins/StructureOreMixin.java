package ganymedes01.etfuturum.mixins;
import java.util.Random;
import ganymedes01.etfuturum.UndergroundStructures;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(WorldGenMinable.class)
public abstract class StructureOreMixin {
 @Shadow @Final protected int numberOfBlocks;
 @Inject(method="generate",at=@At("HEAD"),cancellable=true)
 private void etfu$reserve(World world,Random random,BlockPos pos,CallbackInfoReturnable<Boolean> callback){if(UndergroundStructures.oreReserved(world,pos.getX(),pos.getY(),pos.getZ(),numberOfBlocks))callback.setReturnValue(false);}
}
