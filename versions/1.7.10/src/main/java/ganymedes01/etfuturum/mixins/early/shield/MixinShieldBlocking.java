package ganymedes01.etfuturum.mixins.early.shield;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import ganymedes01.etfuturum.items.ItemShield;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Keep the native blocking pose without giving shields the legacy sword's omnidirectional half damage. */
@Mixin(EntityPlayer.class)
public class MixinShieldBlocking {
    @WrapOperation(method="damageEntity",at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/EntityPlayer;isBlocking()Z"))
    private boolean uab$shieldDamage(EntityPlayer player,Operation<Boolean> original){ItemStack held=((ShieldUseAccess)player).uab$activeItem();return (held==null||!(held.getItem() instanceof ItemShield))&&original.call(player);}
}
