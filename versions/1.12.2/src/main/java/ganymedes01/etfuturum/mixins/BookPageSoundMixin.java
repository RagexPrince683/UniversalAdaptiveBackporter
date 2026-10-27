package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=GuiButton.class,remap=false)
public abstract class BookPageSoundMixin {
 @Inject(method={"playPressSound","func_146113_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$page(SoundHandler handler,CallbackInfo ci){int id=((GuiButton)(Object)this).id;if(Features.enabled("sounds.bookPageTurn")&&(id==1||id==2)&&Minecraft.getMinecraft().currentScreen instanceof GuiScreenBook){handler.playSound(PositionedSoundRecord.getMasterRecord(ModernSounds.get("item.book.page_turn"),1));ci.cancel();}}
}
