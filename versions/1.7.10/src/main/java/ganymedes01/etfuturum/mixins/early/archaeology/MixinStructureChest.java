package ganymedes01.etfuturum.mixins.early.archaeology;

import ganymedes01.etfuturum.world.structure.ExplorationLoot;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;

@Mixin(StructureComponent.class)
public abstract class MixinStructureChest {
	@Shadow protected abstract int getXWithOffset(int x, int z);
	@Shadow protected abstract int getYWithOffset(int y);
	@Shadow protected abstract int getZWithOffset(int x, int z);
	@Inject(method = "generateStructureChestContents", at = @At("RETURN"))
	private void etfu$templateLoot(World world, StructureBoundingBox clip, Random random, int x, int y, int z, WeightedRandomChestContent[] entries, int count, CallbackInfoReturnable<Boolean> result) {
		if (result.getReturnValue() && world.getTileEntity(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z)) instanceof IInventory) ExplorationLoot.nativeChest((StructureComponent) (Object) this, (IInventory) world.getTileEntity(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z)), random);
	}
}
