package ganymedes01.etfuturum.mixins.early.nethermobs;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;
@Mixin(WorldGenDungeons.class)
public class MixinMobDungeons {
	@Inject(method = "generate", at = @At("RETURN"))
	private void etfuturum$legacyTrials(World world, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() || world.isRemote || world.provider.dimensionId != 0 || !ganymedes01.etfuturum.configuration.configs.ConfigEntities.enableMobDungeonContent || world.getBlock(x, y, z) != Blocks.mob_spawner) return;
		if (random.nextInt(6) == 0) world.setBlock(x, y, z, ganymedes01.etfuturum.ModBlocks.TRIAL_SPAWNER.get(), random.nextInt(2), 3);
		else if (y < 30 && random.nextInt(8) == 0) world.setBlock(x, y, z, ganymedes01.etfuturum.ModBlocks.SCULK_SHRIEKER.get(), 8, 3);
	}
}
