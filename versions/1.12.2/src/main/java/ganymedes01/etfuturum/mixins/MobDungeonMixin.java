package ganymedes01.etfuturum.mixins;
import java.util.Random;
import ganymedes01.etfuturum.*;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=WorldGenDungeons.class,remap=false)
public abstract class MobDungeonMixin {
 @Inject(method={"generate","func_180709_b"},at=@At("RETURN"),remap=false)
 private void etfu$acquisition(World world,Random random,BlockPos pos,CallbackInfoReturnable<Boolean> callback){if(!callback.getReturnValue()||world.isRemote||world.provider.getDimension()!=0||!Features.enabled("entities.enableMobDungeonContent")||world.getBlockState(pos).getBlock()!=Blocks.MOB_SPAWNER)return;if(random.nextInt(6)==0&&Content.available("trial_spawner"))world.setBlockState(pos,Content.block("trial_spawner").getStateFromMeta(random.nextInt(2)),3);else if(pos.getY()<30&&random.nextInt(8)==0&&Content.available("sculk_shrieker"))world.setBlockState(pos,Content.block("sculk_shrieker").getStateFromMeta(8),3);}
}
