package ganymedes01.etfuturum.mixins.early.shield;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
/** The native convenience getters are client-only in this target; these fields exist on both sides. */
@Mixin(EntityPlayer.class)
public interface ShieldUseAccess {
    @Accessor("itemInUse") ItemStack uab$activeItem();
    @Accessor("itemInUseCount") int uab$useRemaining();
}
