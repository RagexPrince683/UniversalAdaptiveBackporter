package ganymedes01.etfuturum.mixins.early.lightningrod;

import ganymedes01.etfuturum.tileentities.TileEntityLightningRod;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldServer.class)
public class MixinWorldServer {
	@Redirect(method = "func_147456_g", at = @At(value = "NEW", target = "net/minecraft/entity/effect/EntityLightningBolt"))
	private EntityLightningBolt etfu$attractLightning(World world, double x, double y, double z) {
		TileEntityLightningRod rod = TileEntityLightningRod.nearest(world, x, y, z);
		// Redirect before construction: the constructor already places lightning fire.
		return rod == null ? new EntityLightningBolt(world, x, y, z)
				: new EntityLightningBolt(world, rod.xCoord + 0.5, rod.yCoord, rod.zCoord + 0.5);
	}
}
