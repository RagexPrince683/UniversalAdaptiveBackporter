package ganymedes01.etfuturum.mixins.early.archaeology;

import net.minecraft.entity.item.EntityFallingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityFallingBlock.class)
public interface FallingBlockAccess {
	@Accessor("field_145808_f") void etfu$breakOnLanding(boolean broken);
}
