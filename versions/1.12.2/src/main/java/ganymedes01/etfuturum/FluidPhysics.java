package ganymedes01.etfuturum;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.Vec3d;
public final class FluidPhysics {
 private FluidPhysics(){}
 public static Vec3d level(Vec3d flow,IBlockState state){if(!Features.enabled("mixins.adjustedLiquidPhysics")||!(state.getBlock() instanceof BlockLiquid))return flow;int level=state.getValue(BlockLiquid.LEVEL);double power=level>=8?1:1-level/7D*.6;return new Vec3d(flow.x*power,flow.y,flow.z*power);}
 public static Vec3d normalize(Vec3d flow,Entity entity){Vec3d unit=flow.normalize();if(!Features.enabled("mixins.adjustedLiquidPhysics")||entity instanceof EntityItem)return unit;double length=Math.min(flow.length(),1.15);return new Vec3d(unit.x*length,unit.y,unit.z*length);}
}
