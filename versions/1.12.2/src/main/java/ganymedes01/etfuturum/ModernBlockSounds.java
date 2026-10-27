package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.block.*;
/** Native sound-type replacement at the public block boundary; custom sound types remain authoritative. */
public final class ModernBlockSounds {
 private static final Map<String,SoundType> TYPES=new java.util.concurrent.ConcurrentHashMap<>();
 private static boolean enabled;
 public static void initialize(){enabled=Features.enabled("sounds.newBlockSounds");}
 private ModernBlockSounds(){}
 public static SoundType resolve(Block block,SoundType original){
  if(!enabled||block.getRegistryName()==null)return original;String id=block.getRegistryName().getPath(),sound=null;
  if(original==SoundType.STONE){if(id.contains("nether")&&id.contains("brick"))sound="nether_bricks";else if(id.contains("netherrack")||id.contains("hellfish"))sound="netherrack";else if(id.contains("nether")&&(block instanceof BlockOre||id.contains("ore")))sound="nether_ore";else if(id.contains("deepslate"))sound=id.contains("brick")?"deepslate_bricks":"deepslate";else if(id.contains("bone")||id.contains("ivory"))sound="bone_block";}
  if(original==SoundType.PLANT){if(block instanceof BlockVine)sound="vine";else if(block instanceof BlockLilyPad)sound="wet_grass";}
  if(block instanceof BlockCrops||block instanceof BlockStem)sound="crop";if(block instanceof BlockNetherWart)sound="nether_wart";
  if(original==SoundType.SAND&&id.contains("soul")&&id.contains("sand"))sound="soul_sand";
  if(original==SoundType.METAL&&(id.contains("copper")||id.contains("tin")))sound="copper";
  if(block instanceof BlockHopper)return SoundType.METAL;
  if(sound==null)return original;final String type=sound;return TYPES.computeIfAbsent(type,key->new SoundType(original.getVolume(),original.getPitch(),ModernSounds.get("block."+type+".break"),(type.equals("crop")||type.equals("nether_wart"))?original.getStepSound():ModernSounds.get("block."+type+".step"),ModernSounds.get((type.equals("crop")||type.equals("nether_wart"))?"item."+type+".plant":"block."+type+".place"),(type.equals("crop")||type.equals("nether_wart"))?original.getHitSound():ModernSounds.get("block."+type+".hit"),(type.equals("crop")||type.equals("nether_wart"))?original.getFallSound():ModernSounds.get("block."+type+".fall")));
 }
}
