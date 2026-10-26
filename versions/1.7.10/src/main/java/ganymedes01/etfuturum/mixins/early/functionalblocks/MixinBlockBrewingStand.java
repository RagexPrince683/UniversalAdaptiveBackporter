package ganymedes01.etfuturum.mixins.early.functionalblocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.lib.GUIIDs;
import ganymedes01.etfuturum.tileentities.TileEntityNewBrewingStand;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBrewingStand.class)
public abstract class MixinBlockBrewingStand {

	@Inject(method = "createNewTileEntity", at = @At("HEAD"), cancellable = true)
	private void etfuturum$createFuelledTile(World world, int metadata, CallbackInfoReturnable<TileEntity> cir) {
		if (ConfigBlocksItems.enableBrewingStands) cir.setReturnValue(new TileEntityNewBrewingStand());
	}

	@Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
	private void etfuturum$openFuelledContainer(World world, int x, int y, int z, EntityPlayer player, int side,
			float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
		if (!ConfigBlocksItems.enableBrewingStands) return;
		TileEntity tile = world.getTileEntity(x, y, z);
		if (!world.isRemote && tile != null && !(tile instanceof TileEntityNewBrewingStand)) {
			// Upgrade only this already-existing vanilla tile when it is used. The block remains vanilla and
			// vanilla inventory/BrewTime NBT is understood by the augmented TileEntityBrewingStand subclass.
			net.minecraft.nbt.NBTTagCompound state = new net.minecraft.nbt.NBTTagCompound();
			tile.writeToNBT(state);
			TileEntityNewBrewingStand upgraded = new TileEntityNewBrewingStand();
			upgraded.readFromNBT(state);
			world.setTileEntity(x, y, z, upgraded);
			world.markBlockForUpdate(x, y, z);
		}
		if (!world.isRemote) player.openGui(EtFuturum.instance, GUIIDs.BREWING_STAND, world, x, y, z);
		cir.setReturnValue(true);
	}
}
