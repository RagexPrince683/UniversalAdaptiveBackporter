package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.MaceCombat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=EntityPlayer.class,remap=false)
public class MacePlayerArmorMixin {
    @Redirect(method={"damageEntity","func_70665_d"},at=@At(value="INVOKE",target="Lnet/minecraftforge/common/ISpecialArmor$ArmorProperties;applyArmor(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/NonNullList;Lnet/minecraft/util/DamageSource;D)F"),remap=false)
    private float etfu$breach(EntityLivingBase entity,NonNullList<ItemStack> inventory,DamageSource source,double damage){return (float)MaceCombat.armor(damage,ArmorProperties.applyArmor(entity,inventory,source,damage),source);}
}
