package ganymedes01.etfuturum.client;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.*;
import ganymedes01.etfuturum.ModernSounds;
import org.apache.logging.log4j.LogManager;

/** Checksummed content-addressed cache. Acquisition finishes before SoundHandler loads resources. */
final class ModernAudio {
    private final Map<String,File> files=new HashMap<>();
    private final byte[] definitions;
    ModernAudio(File game) throws Exception {
        File cache=new File(game,"assets/uab/audio");Files.createDirectories(cache.toPath());
        File index=new File(cache,"index.json");String offline=System.getProperty("uab.audioIndex");
        if(offline!=null)index=new File(offline);
        else if(!index.isFile()){
            JsonObject manifest=ModernAssets.json("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"),version=null;
            for(JsonElement entry:manifest.getAsJsonArray("versions"))if(entry.getAsJsonObject().get("id").getAsString().equals("1.21.11"))version=ModernAssets.json(entry.getAsJsonObject().get("url").getAsString());
            if(version==null)throw new IOException("Missing Minecraft 1.21.11 audio metadata");
            JsonObject asset=version.getAsJsonObject("assetIndex");download(asset.get("url").getAsString(),asset.get("sha1").getAsString(),index);
        }
        JsonObject objects;try(Reader reader=new InputStreamReader(new FileInputStream(index),StandardCharsets.UTF_8)){objects=new JsonParser().parse(reader).getAsJsonObject().getAsJsonObject("objects");}
        File root=new File(System.getProperty("uab.audioObjects",new File(cache,"objects").getPath()));
        Map<String,File> byHash=new LinkedHashMap<>();Map<String,JsonObject> descriptors=new LinkedHashMap<>();
        for(Map.Entry<String,JsonElement> entry:objects.entrySet())if(entry.getKey().equals("minecraft/sounds.json")||entry.getKey().startsWith("minecraft/sounds/")&&entry.getKey().endsWith(".ogg")){
            JsonObject description=entry.getValue().getAsJsonObject();String hash=description.get("hash").getAsString();File file=new File(root,hash.substring(0,2)+"/"+hash);files.put(entry.getKey().substring("minecraft/".length()),file);byHash.put(hash,file);descriptors.put(hash,description);
        }
        ExecutorService workers=Executors.newFixedThreadPool(8,r->{Thread thread=new Thread(r,"UAB audio acquisition");thread.setDaemon(true);return thread;});
        List<Future<?>> pending=new ArrayList<>();int missing=0;
        try{
            for(Map.Entry<String,File> entry:byHash.entrySet()){String hash=entry.getKey();File file=entry.getValue();long size=descriptors.get(hash).get("size").getAsLong();if(file.isFile()&&file.length()==size)continue;missing++;pending.add(workers.submit(()->{try{download("https://resources.download.minecraft.net/"+hash.substring(0,2)+"/"+hash,hash,file);}catch(Exception failure){throw new IllegalStateException("Cannot obtain modern audio "+hash,failure);}}));}
            if(missing>0)LogManager.getLogger("UAB").info("Obtaining {} missing modern audio resources from Mojang",missing);
            for(Future<?> future:pending)future.get();
        }finally{workers.shutdownNow();}
        JsonObject sounds;try(Reader reader=new InputStreamReader(new FileInputStream(files.get("sounds.json")),StandardCharsets.UTF_8)){sounds=new JsonParser().parse(reader).getAsJsonObject();}
        for(Map.Entry<String,JsonElement> entry:sounds.entrySet()){
            JsonObject sound=entry.getValue().getAsJsonObject();JsonArray adapted=new JsonArray();
            if(sound.has("sounds"))for(JsonElement value:sound.getAsJsonArray("sounds")){if(value.isJsonPrimitive())adapted.add(new JsonPrimitive(name(value.getAsString())));else{JsonObject object=value.getAsJsonObject();object.addProperty("name",name(object.get("name").getAsString()));adapted.add(object);}}
            sound.add("sounds",adapted);
        }
        definitions=sounds.toString().getBytes(StandardCharsets.UTF_8);
    }
    private static String name(String name){return ModernSounds.DOMAIN+":"+(name.startsWith("minecraft:")?name.substring(10):name);}
    boolean contains(String path){return path.equals("sounds.json")||files.containsKey(path);}
    InputStream open(String path)throws IOException{return path.equals("sounds.json")?new ByteArrayInputStream(definitions):new FileInputStream(files.get(path));}
    private static void download(String url,String hash,File target)throws Exception{
        Files.createDirectories(target.toPath().getParent());Path temporary=Files.createTempFile(target.toPath().getParent(),"audio-",".part");
        try{try(InputStream input=ModernAssets.open(url)){Files.copy(input,temporary,StandardCopyOption.REPLACE_EXISTING);}if(!ModernAssets.hash(temporary.toFile()).equals(hash))throw new IOException("Modern audio checksum mismatch");Files.move(temporary,target.toPath(),StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temporary);}
    }
}
