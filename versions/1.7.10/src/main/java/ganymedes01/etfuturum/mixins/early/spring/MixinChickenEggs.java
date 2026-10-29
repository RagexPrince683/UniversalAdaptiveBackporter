package ganymedes01.etfuturum.mixins.early.spring;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.item.Item;
@Mixin(value=EntityChicken.class)
public class MixinChickenEggs {
 @ModifyArg(method="onLivingUpdate",at=@At(value="INVOKE",target="Lnet/minecraft/entity/passive/EntityChicken;dropItem(Lnet/minecraft/item/Item;I)Lnet/minecraft/entity/item/EntityItem;"),index=0) private Item uab$egg(Item item){return SpringAnimals.egg((EntityChicken)(Object)this,item);}
}
