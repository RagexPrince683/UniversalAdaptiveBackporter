package ganymedes01.etfuturum.client;
import java.util.*;
import ganymedes01.etfuturum.*;
import net.minecraft.client.audio.ISound;
public final class SoundDistances {
 private static final Map<String,Float> DISTANCES=new HashMap<>();
 static{String[] names={"block.beacon.ambient","block.beehive.drip","block.beehive.enter","block.beehive.exit","block.beehive.work","block.sculk_catalyst.bloom","entity.bee.loop","entity.bee.loop_aggressive","entity.bee.pollinate","block.copper_bulb.turn_on","block.copper_bulb.turn_off"};float[] values={7,8,14,14,12,12,6,10,12,6,6};for(int i=0;i<names.length;i++)DISTANCES.put(ModernSounds.DOMAIN+":"+names[i],values[i]);DISTANCES.put("minecraft:block.portal.ambient",10F);DISTANCES.put("minecraft:block.beacon.ambient",7F);}
 private SoundDistances(){}
 public static float distance(float original,ISound sound){return Features.enabled("mixins.adjustedAttenuation")?DISTANCES.getOrDefault(sound.getSoundLocation().toString(),original):original;}
}
