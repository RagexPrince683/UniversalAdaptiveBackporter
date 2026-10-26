package ganymedes01.etfuturum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.util.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Stable sound identities are registered on both sides; only the client obtains audio assets. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class ModernSounds {
    public static final String DOMAIN="minecraft_1.21.11";
    private static final Map<String,SoundEvent> SOUNDS=new LinkedHashMap<>();
    static {
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(Objects.requireNonNull(ModernSounds.class.getResourceAsStream("/assets/etfuturum/sound-events.txt")),StandardCharsets.UTF_8))){
            String name;while((name=reader.readLine())!=null)if(!name.isEmpty()){ResourceLocation id=new ResourceLocation(DOMAIN,name);SOUNDS.put(name,new SoundEvent(id).setRegistryName(id));}
        }catch(IOException failure){throw new ExceptionInInitializerError(failure);}
    }
    private ModernSounds(){}
    public static SoundEvent get(String name){SoundEvent sound=SOUNDS.get(name);if(sound==null)throw new IllegalArgumentException("Unknown modern sound "+name);return sound;}
    @SubscribeEvent public static void register(RegistryEvent.Register<SoundEvent> event){for(SoundEvent sound:SOUNDS.values())event.getRegistry().register(sound);}
}
