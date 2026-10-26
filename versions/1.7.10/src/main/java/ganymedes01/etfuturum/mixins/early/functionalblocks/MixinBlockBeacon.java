package ganymedes01.etfuturum.mixins.early.functionalblocks;

import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.tileentities.TileEntityNewBeacon;
import net.minecraft.block.BlockBeacon;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBeacon.class)
public abstract class MixinBlockBeacon {

	@Inject(method = "createNewTileEntity", at = @At("HEAD"), cancellable = true)
	private void etfuturum$createColourBeamTile(World world, int metadata, CallbackInfoReturnable<TileEntity> cir) {
		if (ConfigBlocksItems.enableColourfulBeacons) cir.setReturnValue(new TileEntityNewBeacon());
	}
}
