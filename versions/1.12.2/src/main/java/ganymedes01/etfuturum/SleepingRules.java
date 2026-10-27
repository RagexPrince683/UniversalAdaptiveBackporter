package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Sleeping counts are derived per world, so departures and rule changes cannot retain stale sleepers. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class SleepingRules {
 public static final String RULE="playersSleepingPercentage";
 private static final Map<World,int[]> ANNOUNCED=new WeakHashMap<>();
 private SleepingRules(){}
 public static int required(int players,int percentage){if(percentage>100)return Integer.MAX_VALUE;return (int)Math.max(1,((long)players*Math.max(0,percentage)+99)/100);}
 public static boolean enabled(){return Features.enabled("mixins.enablePlayersSleepingPecentageGamerule");}
 @SubscribeEvent public static void load(WorldEvent.Load event){World world=event.getWorld();if(!world.isRemote&&enabled()&&!world.getGameRules().hasRule(RULE))world.getGameRules().addGameRule(RULE,"100",GameRules.ValueType.NUMERICAL_VALUE);}
 @SubscribeEvent public static void unload(WorldEvent.Unload event){ANNOUNCED.remove(event.getWorld());}
 public static int percentage(World world){return world.getGameRules().hasRule(RULE)?world.getGameRules().getInt(RULE):100;}
 public static boolean asleep(WorldServer world){int players=0,sleepers=0;for(EntityPlayer player:world.playerEntities)if(!player.isSpectator()){players++;if(player.isPlayerFullyAsleep())sleepers++;}return players>0&&sleepers>=required(players,percentage(world));}
 public static void announce(WorldServer world){int percent=percentage(world);if(percent<=0||percent>=100)return;int players=0,sleepers=0;for(EntityPlayer player:world.playerEntities)if(!player.isSpectator()){players++;if(player.isPlayerSleeping())sleepers++;}int required=required(players,percent);int[] previous=ANNOUNCED.put(world,new int[]{sleepers,required});if(sleepers>0&&(previous==null||previous[0]!=sleepers||previous[1]!=required))for(EntityPlayer player:world.playerEntities)player.sendMessage(new TextComponentTranslation("sleep.players_sleeping",sleepers,required));}
 public static void waking(WorldServer world){int percent=percentage(world);if(percent>0&&percent<100)for(EntityPlayer player:world.playerEntities)player.sendMessage(new TextComponentTranslation("sleep.skipping_night"));ANNOUNCED.remove(world);}
}
