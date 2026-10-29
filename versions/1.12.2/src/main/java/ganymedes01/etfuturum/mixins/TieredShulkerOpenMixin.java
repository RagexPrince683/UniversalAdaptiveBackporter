package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.IronStorage;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=BlockShulkerBox.class,remap=false)
public abstract class TieredShulkerOpenMixin extends net.minecraft.block.BlockContainer {
 protected TieredShulkerOpenMixin(net.minecraft.block.material.Material material){super(material);}
 @Override public float getExplosionResistance(net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,net.minecraft.entity.Entity entity,net.minecraft.world.Explosion explosion){TileEntity tile=world.getTileEntity(pos);return tile instanceof IInventory&&ganymedes01.etfuturum.StorageTier.of((IInventory)tile)==ganymedes01.etfuturum.StorageTier.OBSIDIAN?10000:super.getExplosionResistance(world,pos,entity,explosion);}
 @Group(name="uab$open",min=1)
 @Redirect(method={"onBlockActivated","func_180639_a"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/EntityPlayer;displayGUIChest(Lnet/minecraft/inventory/IInventory;)V"),require=0,expect=0,remap=false)
 private void uab$open(EntityPlayer player,IInventory inventory){open(player,inventory);}
 @Group(name="uab$open",min=1)
 @Redirect(method={"onBlockActivated","func_180639_a"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/EntityPlayer;func_71007_a(Lnet/minecraft/inventory/IInventory;)V"),require=0,expect=0,remap=false)
 private void uab$openSrg(EntityPlayer player,IInventory inventory){open(player,inventory);}
 private static void open(EntityPlayer player,IInventory inventory){TileEntity tile=(TileEntity)inventory;IronStorage.open(player,inventory,tile.getWorld(),tile.getPos());}
}
