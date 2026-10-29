package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.client.ContainedWaterAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockRendererDispatcher.class, remap = false)
public class ContainedWaterRenderingMixin {
    @Inject(method = {"renderBlock", "func_175018_a"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void etfu$containedWater(IBlockState state, BlockPos pos, IBlockAccess access, BufferBuilder buffer,
                                     CallbackInfoReturnable<Boolean> result) {
        if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT && ContainedWaterAccess.containsWater(state)) {
            // Dispatch native water, including its tint, height, flow, overlay and resource-pack sprites.
            // The replacement state cannot re-enter this branch. No world or shared renderer state is mutated.
            result.setReturnValue(((BlockRendererDispatcher) (Object) this).renderBlock(
                    Blocks.WATER.getDefaultState(), pos, new ContainedWaterAccess(access), buffer));
        }
    }
}
