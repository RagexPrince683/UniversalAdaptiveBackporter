package ganymedes01.etfuturum.mixins;
import java.util.List;
import ganymedes01.etfuturum.client.ShulkerTooltip;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.client.util.ITooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=BlockShulkerBox.class,remap=false)
public abstract class ShulkerTooltipMixin {
 @Inject(method={"addInformation","func_190948_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$disabled(ItemStack stack,World world,List<String> tooltip,ITooltipFlag flags,CallbackInfo callback){if(ShulkerTooltip.limit()==0||ShulkerTooltip.tiered(stack,tooltip))callback.cancel();}
 @ModifyConstant(method={"addInformation","func_190948_a"},constant=@Constant(intValue=4),remap=false)
 private int uab$lines(int vanilla){return ShulkerTooltip.limit()-1;}
}
