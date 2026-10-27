package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import ganymedes01.etfuturum.client.BoundedParticles;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=ParticleManager.class,remap=false)
public abstract class BoundedParticlesMixin {
 @Inject(method={"addBlockDestroyEffects","func_180533_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$destroy(BlockPos pos,IBlockState state,CallbackInfo ci){if(Features.enabled("mixins.boundedBlockBreakingParticles")){BoundedParticles.destroy((ParticleManager)(Object)this,Minecraft.getMinecraft().world,pos,state);ci.cancel();}}
}
