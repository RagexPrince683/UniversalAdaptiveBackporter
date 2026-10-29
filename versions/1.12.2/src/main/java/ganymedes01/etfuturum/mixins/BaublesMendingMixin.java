package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.MendingCompatibility;
import net.minecraft.enchantment.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EntityXPOrb.class,remap=false)
public abstract class BaublesMendingMixin {
 @Unique private MendingCompatibility.Selection uab$selection;
 @Inject(method={"onCollideWithPlayer","func_70100_b_"},at=@At("HEAD"),remap=false)
 private void uab$begin(EntityPlayer player,CallbackInfo ci){uab$selection=null;}
 @Group(name="baublesMending",min=1) @Redirect(method={"onCollideWithPlayer","func_70100_b_"},at=@At(value="INVOKE",target="Lnet/minecraft/enchantment/EnchantmentHelper;getEnchantedItem(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/item/ItemStack;"),require=0,expect=0,remap=false)
 private ItemStack uab$select(Enchantment enchantment,EntityLivingBase entity){return uab$equipment(enchantment,entity);}
 @Group(name="baublesMending",min=1) @Redirect(method={"onCollideWithPlayer","func_70100_b_"},at=@At(value="INVOKE",target="Lnet/minecraft/enchantment/EnchantmentHelper;func_92099_a(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/item/ItemStack;"),require=0,expect=0,remap=false)
 private ItemStack uab$selectSrg(Enchantment enchantment,EntityLivingBase entity){return uab$equipment(enchantment,entity);}
 @Unique private ItemStack uab$equipment(Enchantment enchantment,EntityLivingBase entity){uab$selection=MendingCompatibility.select(enchantment,entity);return uab$selection==null?EnchantmentHelper.getEnchantedItem(enchantment,entity):uab$selection.stack;}
 @Inject(method={"onCollideWithPlayer","func_70100_b_"},at=@At("RETURN"),remap=false)
 private void uab$finish(EntityPlayer player,CallbackInfo ci){if(uab$selection!=null){uab$selection.finish();uab$selection=null;}}
}
