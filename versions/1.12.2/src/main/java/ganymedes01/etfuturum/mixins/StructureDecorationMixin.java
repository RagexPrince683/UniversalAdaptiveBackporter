package ganymedes01.etfuturum.mixins;
import java.util.Random;
import ganymedes01.etfuturum.ModernStructures;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value={WorldGenLakes.class,WorldGenLiquids.class,WorldGenDungeons.class,WorldGenFossils.class},remap=false)
public abstract class StructureDecorationMixin {
 @Inject(method={"generate","func_180709_b"},at=@At("HEAD"),cancellable=true,remap=false)
 private void etfu$reserve(World world,Random random,BlockPos pos,CallbackInfoReturnable<Boolean> callback){Object self=this;int radius=self instanceof WorldGenFossils?32:self instanceof WorldGenLakes?16:self instanceof WorldGenDungeons?6:1;int low=self instanceof WorldGenFossils?0:pos.getY()-16,high=self instanceof WorldGenFossils?80:pos.getY()+16;if(ModernStructures.reserved(world,new StructureBoundingBox(pos.getX()-radius,low,pos.getZ()-radius,pos.getX()+radius,high,pos.getZ()+radius)))callback.setReturnValue(false);}
}
