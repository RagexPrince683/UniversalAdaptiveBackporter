package ganymedes01.etfuturum.mixins.early.functionalblocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.lib.GUIIDs;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEnchantmentTable.class)
public abstract class MixinBlockEnchantmentTable {

	@Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
	private void etfuturum$openModernContainer(World world, int x, int y, int z, EntityPlayer player, int side,
			float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
		if (!ConfigBlocksItems.enableEnchantingTable) return;
		if (!world.isRemote) player.openGui(EtFuturum.instance, GUIIDs.ENCHANTING_TABLE, world, x, y, z);
		cir.setReturnValue(true);
	}
}
