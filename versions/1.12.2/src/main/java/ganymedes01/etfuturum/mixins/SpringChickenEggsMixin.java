package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.item.Item;
@Mixin(value=EntityChicken.class,remap=false)
public class SpringChickenEggsMixin {
 @Group(name="springChickenEgg",min=1) @ModifyArg(method={"onLivingUpdate","func_70636_d"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/passive/EntityChicken;dropItem(Lnet/minecraft/item/Item;I)Lnet/minecraft/entity/item/EntityItem;"),index=0,require=0,remap=false) private Item uab$mcp(Item item){return SpringAnimals.egg((EntityChicken)(Object)this,item);}
 @Group(name="springChickenEgg",min=1) @ModifyArg(method={"onLivingUpdate","func_70636_d"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/passive/EntityChicken;func_145779_a(Lnet/minecraft/item/Item;I)Lnet/minecraft/entity/item/EntityItem;"),index=0,require=0,remap=false) private Item uab$srg(Item item){return SpringAnimals.egg((EntityChicken)(Object)this,item);}
}
