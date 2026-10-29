package ganymedes01.etfuturum.client;

import java.util.*;
import ganymedes01.etfuturum.SpringAnimals;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Tracking snapshots are applied on the client thread; UUID checks prevent entity-id reuse. */
public final class SpringAnimalView {
    private static final Map<Integer,Pending> PENDING=new HashMap<>();private static World world;
    private static final class Pending {final SpringAnimals.State state;int remaining=40;Pending(SpringAnimals.State state){this.state=state;}}
    public static void receive(SpringAnimals.State state){World current=Minecraft.getMinecraft().world;if(current!=world){PENDING.clear();world=current;}if(current!=null&&!state.apply(current.getEntityByID(state.id)))PENDING.put(state.id,new Pending(state));}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.END)return;World current=Minecraft.getMinecraft().world;if(current!=world){PENDING.clear();world=current;}if(current==null)return;Iterator<Pending> iterator=PENDING.values().iterator();while(iterator.hasNext()){Pending pending=iterator.next();if(pending.state.apply(current.getEntityByID(pending.state.id))||--pending.remaining==0)iterator.remove();}}
}
