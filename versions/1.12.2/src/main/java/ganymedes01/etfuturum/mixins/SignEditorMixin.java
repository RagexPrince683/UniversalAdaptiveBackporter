package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.WoodSigns;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=GuiEditSign.class,remap=false)
public abstract class SignEditorMixin {
 @Group(name="signBlock",min=1) @Redirect(method={"drawScreen","func_73863_a"},at=@At(value="INVOKE",target="Lnet/minecraft/tileentity/TileEntitySign;getBlockType()Lnet/minecraft/block/Block;"),require=0,remap=false)
 private Block uab$sign(TileEntitySign tile){return uab$type(tile);}
 @Group(name="signBlock",min=1) @Redirect(method={"drawScreen","func_73863_a"},at=@At(value="INVOKE",target="Lnet/minecraft/tileentity/TileEntitySign;func_145838_q()Lnet/minecraft/block/Block;"),require=0,remap=false)
 private Block uab$signSrg(TileEntitySign tile){return uab$type(tile);}
 private static Block uab$type(TileEntitySign tile){Block block=tile.getBlockType();return block instanceof WoodSigns.Sign&&((WoodSigns.Sign)block).standing()?Blocks.STANDING_SIGN:block;}
}
