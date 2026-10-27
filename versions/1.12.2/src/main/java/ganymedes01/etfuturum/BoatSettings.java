package ganymedes01.etfuturum;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Property;
public final class BoatSettings {
 public static float speed,landGlide,buoyancy,fallBreak;public static boolean secondSeat,whitelist,legacyBuoyancy,freeRotation,preventNpcs;private static String[] banned;
 private BoatSettings(){}
 static void initialize(){UabConfiguration config=UabConfiguration.get();speed=config.decimal("blocksitems","newBoatSpeed",1,.1F,2);landGlide=config.decimal("blocksitems","newBoatMaxLandSpeed",.986F,.1F,1);secondSeat=config.bool("blocksitems","newBoatPassengerSeat",true);whitelist=config.bool("blocksitems","newBoatEntityBlacklistAsWhitelist",false);banned=config.property("blocksitems","newBoatEntityBlacklist",new String[0],Property.Type.STRING).getStringList();legacyBuoyancy=Features.enabled("tweaks.legacyNewBoatBuoyancy");buoyancy=config.decimal("tweaks","legacyNewBoatBuoyancyStrength",0,0,Float.MAX_VALUE);fallBreak=config.decimal("tweaks","newBoatsFallBreakDistance",0,0,Float.MAX_VALUE);freeRotation=Features.enabled("tweaks.stopBoatRotationLock");preventNpcs=Features.enabled("functions.preventCustomNpcsInBoats");}
 public static boolean allowed(Entity entity){String type=entity.getClass().getName();if(preventNpcs&&(type.startsWith("noppes.npcs.")||type.startsWith("kamkeel.npcdbc.")))return false;ResourceLocation id=EntityList.getKey(entity);for(String entry:banned)if(entry.startsWith("classpath:")?type.contains(entry.substring(10)):id!=null&&entry.equals(id.toString()))return whitelist;return !whitelist;}
}
