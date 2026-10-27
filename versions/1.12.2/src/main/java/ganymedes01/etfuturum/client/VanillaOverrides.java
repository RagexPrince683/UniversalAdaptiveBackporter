package ganymedes01.etfuturum.client;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import ganymedes01.etfuturum.Features;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.*;
import net.minecraft.util.ResourceLocation;
/** The maintained, explicitly listed vanilla overrides; player packs retain normal priority. */
final class VanillaOverrides implements IResourcePack {
 private static final String ROOT="/resourcepacks/vanilla_overrides/assets/minecraft/";
 private boolean enabled(String path){if(path.equals("models/item/end_portal.json"))return true;return path.startsWith("lang/")?Features.enabled("functions.enableLangReplacements"):Features.enabled("functions.enableNewTextures");}
 @Override public InputStream getInputStream(ResourceLocation location)throws IOException{InputStream stream=VanillaOverrides.class.getResourceAsStream(ROOT+location.getPath());if(stream==null)throw new FileNotFoundException(location.toString());return stream;}
 @Override public boolean resourceExists(ResourceLocation location){return location.getNamespace().equals("minecraft")&&enabled(location.getPath())&&VanillaOverrides.class.getResource(ROOT+location.getPath())!=null;}
 @Override public Set<String> getResourceDomains(){return Collections.singleton("minecraft");}
 @Override public <T extends IMetadataSection>T getPackMetadata(MetadataSerializer serializer,String section){return null;}
 @Override public BufferedImage getPackImage(){return null;}
 @Override public String getPackName(){return "UAB vanilla overrides";}
}
