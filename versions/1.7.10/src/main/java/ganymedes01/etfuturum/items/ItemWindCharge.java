package ganymedes01.etfuturum.items;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
public class ItemWindCharge extends BaseItem {
	public ItemWindCharge() { super("wind_charge"); }
	@Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) { if (!world.isRemote && player.getEntityData().getLong("EFRWindCooldown") <= world.getTotalWorldTime()) { if (world.spawnEntityInWorld(new ganymedes01.etfuturum.entities.EntityWindCharge(world, player))) { player.getEntityData().setLong("EFRWindCooldown", world.getTotalWorldTime() + 10); if (!player.capabilities.isCreativeMode) stack.stackSize--; } } return stack; }
}
