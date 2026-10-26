package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.world.nether.NetherSpawning;
import ganymedes01.etfuturum.world.nether.biome.NetherBiomeBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Collections;
import java.util.List;

@Mixin(BiomeGenBase.class)
public class MixinBiomeGenBase {
	@Inject(method = "getSpawnableList", at = @At("HEAD"), cancellable = true)
	private void etfu$striderList(EnumCreatureType type, CallbackInfoReturnable<List<BiomeGenBase.SpawnListEntry>> cir) {
		List<BiomeGenBase.SpawnListEntry> aquatic = ganymedes01.etfuturum.world.MobSpawning.list(type, (BiomeGenBase) (Object) this);
		if (aquatic != null) { cir.setReturnValue(aquatic); return; }
		if (ganymedes01.etfuturum.world.structure.MonumentSpawning.GUARDIANS != null && type == ganymedes01.etfuturum.world.structure.MonumentSpawning.GUARDIANS)
			cir.setReturnValue(ganymedes01.etfuturum.world.structure.MonumentSpawning.SPAWNS);
		if (NetherSpawning.STRIDERS != null && type == NetherSpawning.STRIDERS) {
			Object biome = this;
			cir.setReturnValue(biome == BiomeGenBase.hell || biome instanceof NetherBiomeBase ? NetherSpawning.SPAWNS : Collections.emptyList());
		}
	}
}
