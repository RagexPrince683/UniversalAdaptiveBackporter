package ganymedes01.etfuturum.mixins.early.strongholds;

import ganymedes01.etfuturum.world.structure.StrongholdDistribution;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(MapGenStronghold.class)
public abstract class MixinMapGenStronghold extends MapGenStructure {
    @Shadow private boolean ranBiomeCheck;
    @Shadow private ChunkCoordIntPair[] structureCoords;
    @Shadow @Final public List<BiomeGenBase> field_151546_e;
    @Inject(method = "canSpawnStructureAtCoords", at = @At("HEAD"))
    private void etfu$modernPositions(int x, int z, CallbackInfoReturnable<Boolean> result) {
        if (!ranBiomeCheck && worldObj.provider.dimensionId == 0) {
            StrongholdDistribution distribution = StrongholdDistribution.get(worldObj);
            if (distribution.modern()) {
                structureCoords = distribution.positions(worldObj, field_151546_e);
                ranBiomeCheck = true;
            }
        }
    }
}
