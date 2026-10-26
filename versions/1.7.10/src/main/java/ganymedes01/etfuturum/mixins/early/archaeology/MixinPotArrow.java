package ganymedes01.etfuturum.mixins.early.archaeology;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityArrow.class)
public class MixinPotArrow {
	@Shadow private int field_145791_d;
	@Shadow private int field_145792_e;
	@Shadow private int field_145789_f;
	@Shadow public boolean inGround;
	@Inject(method = "onUpdate", at = @At("HEAD"))
	private void etfu$shatterPot(CallbackInfo ci) {
		World world = ((EntityArrow) (Object) this).worldObj;
		if (!world.isRemote && inGround && world.getBlock(field_145791_d, field_145792_e, field_145789_f) == ModBlocks.DECORATED_POT.get()) {
			world.setBlockMetadataWithNotify(field_145791_d, field_145792_e, field_145789_f, world.getBlockMetadata(field_145791_d, field_145792_e, field_145789_f) | 4, 2);
			world.func_147480_a(field_145791_d, field_145792_e, field_145789_f, true);
		}
	}
}
