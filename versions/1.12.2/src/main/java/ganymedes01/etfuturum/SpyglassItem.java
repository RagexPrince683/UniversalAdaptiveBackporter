package ganymedes01.etfuturum;
import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;
public final class SpyglassItem extends Item {
    public SpyglassItem(){setMaxStackSize(1);}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 72000;}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.NONE;}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){if(!Content.available("spyglass"))return new ActionResult<>(EnumActionResult.FAIL,player.getHeldItem(hand));player.setActiveHand(hand);world.playSound(player,player.posX,player.posY,player.posZ,ModernSounds.get("item.spyglass.use"),SoundCategory.PLAYERS,1,1);return new ActionResult<>(EnumActionResult.SUCCESS,player.getHeldItem(hand));}
    @Override public void onPlayerStoppedUsing(ItemStack stack,World world,net.minecraft.entity.EntityLivingBase entity,int remaining){world.playSound(null,entity.posX,entity.posY,entity.posZ,ModernSounds.get("item.spyglass.stop_using"),SoundCategory.PLAYERS,1,1);}
}
