package ganymedes01.etfuturum;
import ganymedes01.etfuturum.entities.EntityWindCharge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
final class WindChargeItem extends Item {
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(player.getCooldownTracker().hasCooldown(this))return new ActionResult<>(EnumActionResult.FAIL,held);if(!world.isRemote){EntityWindCharge charge=new EntityWindCharge(world,player);charge.shoot(player,player.rotationPitch,player.rotationYaw,0,1.5F,0);if(!world.spawnEntity(charge))return new ActionResult<>(EnumActionResult.FAIL,held);player.getCooldownTracker().setCooldown(this,10);player.getEntityData().setLong("EFRWindCooldown",world.getTotalWorldTime()+10);if(!player.capabilities.isCreativeMode)held.shrink(1);}return new ActionResult<>(EnumActionResult.SUCCESS,held);}
}
