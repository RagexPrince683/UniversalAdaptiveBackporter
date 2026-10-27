package ganymedes01.etfuturum;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Native portal block identity, with the maintained creative pick/placement item. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class NativeBlockItems {
 private NativeBlockItems(){}
 @SubscribeEvent public static void items(RegistryEvent.Register<Item> event){event.getRegistry().register(new ItemBlock(Blocks.END_PORTAL).setRegistryName("minecraft","end_portal"));}
}
