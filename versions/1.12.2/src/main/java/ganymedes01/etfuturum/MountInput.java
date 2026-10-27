package ganymedes01.etfuturum;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import java.util.*;
/** Transient input is recorded only after the native packet handler returns on the server thread. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class MountInput {
    private static final Map<EntityPlayer,Boolean> JUMP=new WeakHashMap<>();
    private MountInput(){}
    public static void record(EntityPlayer player,boolean jump){JUMP.put(player,jump);}
    public static boolean jumping(EntityPlayer player){return Boolean.TRUE.equals(JUMP.get(player));}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){JUMP.remove(event.player);}
}
