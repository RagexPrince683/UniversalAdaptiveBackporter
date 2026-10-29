package ganymedes01.etfuturum.mixins;

import ganymedes01.etfuturum.HoneyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=TileEntityPiston.class,remap=false)
public class HoneyPistonMovementMixin extends TileEntity {
    @Shadow(aliases={"field_145873_m"}) private float progress;
    @Redirect(method={"moveCollidedEntities","func_184322_i"},at=@At(value="INVOKE",target="Lnet/minecraft/block/Block;isStickyBlock(Lnet/minecraft/block/state/IBlockState;)Z"),remap=false)
    private boolean etfu$noHoneyBounce(Block block,IBlockState state){return !(block instanceof HoneyBlock)&&block.isStickyBlock(state);}
    @Inject(method={"moveCollidedEntities","func_184322_i"},at=@At("RETURN"),remap=false)
    private void etfu$carry(float next,CallbackInfo ci){
        TileEntityPiston piston=(TileEntityPiston)(Object)this;
        if(world.isRemote||!(piston.getPistonState().getBlock() instanceof HoneyBlock))return;
        EnumFacing direction=piston.isExtending()?piston.getFacing():piston.getFacing().getOpposite();
        if(direction.getAxis()==EnumFacing.Axis.Y)return;
        float current=progress,offset=piston.isExtending()?current-1:1-current;
        AxisAlignedBB top=new AxisAlignedBB(.0625,.9375,.0625,.9375,1.5,.9375).offset(pos).offset(piston.getFacing().getXOffset()*offset,piston.getFacing().getYOffset()*offset,piston.getFacing().getZOffset()*offset);
        for(Entity entity:world.getEntitiesWithinAABBExcludingEntity(null,top))if(entity.onGround&&entity.getPushReaction()!=EnumPushReaction.IGNORE&&entity.getEntityBoundingBox().minY>=top.minY-.01)
            entity.move(MoverType.PISTON,(next-current)*direction.getXOffset(),0,(next-current)*direction.getZOffset());
    }
}
