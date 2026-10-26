package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.NetherGameplay;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=BlockSoulSand.class,remap=false)
public class SoulSandMixin {
    @Inject(method={"onEntityCollision","func_180634_a"},at=@At("HEAD"),cancellable=true,remap=false)
    private void etfu$speed(World world,BlockPos pos,IBlockState state,Entity entity,CallbackInfo ci){if(NetherGameplay.soulSpeed(entity))ci.cancel();}
}
