package ganymedes01.etfuturum.items;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.item.*;
import net.minecraft.world.World;
public final class ItemColoredEgg extends BaseItem {
    private final int coat;public ItemColoredEgg(String name,int coat){super(name);this.coat=coat;setMaxStackSize(16);}
    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){if(!player.capabilities.isCreativeMode)stack.stackSize--;world.playSoundAtEntity(player,"random.bow",.5F,.4F/(itemRand.nextFloat()*.4F+.8F));if(!world.isRemote){EntityEgg egg=new EntityEgg(world,player);egg.getEntityData().setInteger(SpringAnimals.COAT,coat);world.spawnEntityInWorld(egg);}return stack;}
}
