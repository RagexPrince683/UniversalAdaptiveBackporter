package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.NetherBiomes;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorHell;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ChunkGeneratorHell.class,remap=false)
public abstract class NetherSurfaceMixin {
    @Shadow(aliases="field_185952_n",remap=false) @Final private World world;
    // TAIL deliberately excludes Forge's early return when another mod denies surface replacement.
    @Inject(method={"buildSurfaces","func_185937_b"},at=@At("TAIL"),remap=false)
    private void etfu$surface(int x,int z,ChunkPrimer primer,CallbackInfo ci){NetherBiomes.surface(world,x,z,primer);}
}
