package ganymedes01.etfuturum;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
public final class ColoredEgg extends ItemEgg {
    private final int coat;public ColoredEgg(int coat){this.coat=coat;}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){ItemStack stack=player.getHeldItem(hand);if(!player.capabilities.isCreativeMode)stack.shrink(1);world.playSound(null,player.posX,player.posY,player.posZ,net.minecraft.init.SoundEvents.ENTITY_EGG_THROW,SoundCategory.PLAYERS,.5F,.4F/(itemRand.nextFloat()*.4F+.8F));if(!world.isRemote){EntityEgg egg=new EntityEgg(world,player);egg.getEntityData().setInteger(SpringAnimals.COAT,coat);egg.shoot(player,player.rotationPitch,player.rotationYaw,0,1.5F,1);world.spawnEntity(egg);}return new ActionResult<>(EnumActionResult.SUCCESS,stack);}
}
