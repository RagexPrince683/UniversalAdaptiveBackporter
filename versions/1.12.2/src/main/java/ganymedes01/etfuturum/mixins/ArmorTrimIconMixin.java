package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.client.renderer.ArmorTrimRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=RenderItem.class,remap=false)
public class ArmorTrimIconMixin {
    @Inject(method={"renderItemModelIntoGUI","func_191962_a"},at=@At("RETURN"),remap=false)
    private void etfu$icon(ItemStack stack,int x,int y,IBakedModel model,CallbackInfo ci){ArmorTrimRenderer.renderIcon(stack,x,y,((RenderItem)(Object)this).zLevel);}
}
