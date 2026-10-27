package ganymedes01.etfuturum;
import io.netty.buffer.ByteBuf;
import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.Side;
/** Server-owned damage direction, applied only to the receiving connection's current client player. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class DamageDirection {
 private static SimpleNetworkWrapper channel;
 private static final Map<EntityPlayer,Float> LAST=new WeakHashMap<>();
 private DamageDirection(){}
 static void initialize(){channel=NetworkRegistry.INSTANCE.newSimpleChannel(UAB.ID);channel.registerMessage(Handler.class,Message.class,7,Side.CLIENT);}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){EntityPlayer player=event.player;if(event.phase!=TickEvent.Phase.END||player.world.isRemote||!(player instanceof EntityPlayerMP))return;float yaw=player.attackedAtYaw;if(!Float.isFinite(yaw))return;Float old=LAST.get(player);if(old==null)LAST.put(player,yaw);else if(Math.abs(yaw-old)>.05F){channel.sendTo(new Message(yaw),(EntityPlayerMP)player);LAST.put(player,yaw);}}
 @SubscribeEvent public static void logout(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event){LAST.remove(event.player);}
 public static final class Message implements IMessage {public float yaw;public Message(){}Message(float yaw){this.yaw=yaw;}public void fromBytes(ByteBuf buf){yaw=buf.readFloat();}public void toBytes(ByteBuf buf){buf.writeFloat(yaw);}}
 public static final class Handler implements IMessageHandler<Message,IMessage> {public IMessage onMessage(Message message,MessageContext context){if(Float.isFinite(message.yaw))UAB.proxy.damageDirection(message.yaw,context);return null;}}
}
