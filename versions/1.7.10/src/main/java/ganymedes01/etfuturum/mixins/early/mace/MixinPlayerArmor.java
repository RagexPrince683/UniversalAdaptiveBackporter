package ganymedes01.etfuturum.mixins.early.mace;
import ganymedes01.etfuturum.combat.MaceCombat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityPlayer.class)
public class MixinPlayerArmor {
    @Redirect(method={"damageEntity","func_70665_d"},at=@At(value="INVOKE",target="Lnet/minecraftforge/common/ISpecialArmor$ArmorProperties;ApplyArmor(Lnet/minecraft/entity/EntityLivingBase;[Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/DamageSource;D)F"),remap=false)
    private float etfu$breach(EntityLivingBase entity,ItemStack[] inventory,DamageSource source,double damage){return (float)MaceCombat.armor(damage,ArmorProperties.ApplyArmor(entity,inventory,source,damage),source);}
}
