package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.HoneyBlock;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=BlockPistonStructureHelper.class,remap=false)
public class HoneyPistonStructureMixin {
    @Shadow(aliases={"field_177261_a"}) @Final private World world;
    private static boolean separate(IBlockState a,IBlockState b){return a.getBlock() instanceof HoneyBlock&&b.getBlock()==Blocks.SLIME_BLOCK||b.getBlock() instanceof HoneyBlock&&a.getBlock()==Blocks.SLIME_BLOCK;}
    @Inject(method={"addBlockLine","func_177251_a"},at=@At("HEAD"),cancellable=true,remap=false)
    private void etfu$separateBranch(BlockPos origin,EnumFacing side,CallbackInfoReturnable<Boolean> result){
        // This boundary applies to sticky side branches, never the mandatory forward push.
        IBlockState parent=world.getBlockState(origin.offset(side.getOpposite()));
        if(separate(parent,world.getBlockState(origin))) {
            // Initial and forward lines are identified by their movement axis.
            // A honey/slime pair on the initial line must still push. The native helper passes
            // movement direction there; all lateral branches pass a perpendicular direction.
            if(side.getAxis()!=etfu$movementAxis())result.setReturnValue(true);
        }
    }
    @Shadow(aliases={"field_177257_d"}) @Final private EnumFacing moveDirection;
    private EnumFacing.Axis etfu$movementAxis(){return moveDirection.getAxis();}
    private boolean etfu$canPull(IBlockState state,World world,BlockPos pos,EnumFacing movement,boolean destroy,EnumFacing side){
        return !separate(state,world.getBlockState(pos.offset(movement)))&&BlockPistonBase.canPush(state,world,pos,movement,destroy,side);
    }
    @Group(name="honeyPull",min=1) @Redirect(method={"addBlockLine","func_177251_a"},at=@At(value="INVOKE",target="Lnet/minecraft/block/BlockPistonBase;canPush(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;ZLnet/minecraft/util/EnumFacing;)Z",ordinal=1),require=0,remap=false)
    private boolean etfu$pullMcp(IBlockState state,World world,BlockPos pos,EnumFacing movement,boolean destroy,EnumFacing side){return etfu$canPull(state,world,pos,movement,destroy,side);}
    @Group(name="honeyPull",min=1) @Redirect(method={"addBlockLine","func_177251_a"},at=@At(value="INVOKE",target="Lnet/minecraft/block/BlockPistonBase;func_185646_a(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;ZLnet/minecraft/util/EnumFacing;)Z",ordinal=1),require=0,remap=false)
    private boolean etfu$pullSrg(IBlockState state,World world,BlockPos pos,EnumFacing movement,boolean destroy,EnumFacing side){return etfu$canPull(state,world,pos,movement,destroy,side);}
}
