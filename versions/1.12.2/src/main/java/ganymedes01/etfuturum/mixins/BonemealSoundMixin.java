package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ItemDye.class,remap=false)
public abstract class BonemealSoundMixin {
 @Inject(method="applyBonemeal(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Z",at=@At("RETURN"),remap=false)
 private static void uab$fertilize(ItemStack stack,World world,BlockPos pos,EntityPlayer player,EnumHand hand,CallbackInfoReturnable<Boolean> ci){if(ci.getReturnValue()&&!world.isRemote&&Features.enabled("sounds.bonemealing"))world.playSound(null,pos,ModernSounds.get("item.bone_meal.use"),SoundCategory.BLOCKS,1,1);}
}
