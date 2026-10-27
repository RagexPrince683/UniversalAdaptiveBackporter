package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.Features;
import ganymedes01.etfuturum.api.inventory.FakeTileEntityProvider;
import net.minecraft.block.Block;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=TileEntityHopper.class,remap=false)
public abstract class BlockInventoryMixin {
 @Group(name="blockInventory",min=1) @Redirect(method={"getInventoryAtPosition","func_145893_b"},at=@At(value="INVOKE",target="Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;",ordinal=0),require=0,remap=false)
 private static TileEntity uab$inventory(World world,BlockPos pos){return uab$tile(world,pos);}
 @Group(name="blockInventory",min=1) @Redirect(method={"getInventoryAtPosition","func_145893_b"},at=@At(value="INVOKE",target="Lnet/minecraft/world/World;func_175625_s(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;",ordinal=0),require=0,remap=false)
 private static TileEntity uab$inventorySrg(World world,BlockPos pos){return uab$tile(world,pos);}
 private static TileEntity uab$tile(World world,BlockPos pos){Block block=world.getBlockState(pos).getBlock();return block instanceof FakeTileEntityProvider&&Features.enabled("mixins.blockHopperInteraction")?((FakeTileEntityProvider)block).getFakeTileEntity(world,pos.getX(),pos.getY(),pos.getZ()):world.getTileEntity(pos);}
}
