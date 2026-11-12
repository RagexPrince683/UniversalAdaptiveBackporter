package ganymedes01.etfuturum.client;
import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
/** Resolve optional artwork once per resource reload, keeping missing assets out of the render loop. */
public final class CompatTextures {
    private static final Map<ResourceLocation,ResourceLocation> CACHE=new HashMap<>();
    static { ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(manager -> CACHE.clear()); }
    private CompatTextures() { }
    public static ResourceLocation sign(ResourceLocation requested) {
        ResourceLocation cached=CACHE.get(requested); if(cached!=null)return cached;
        try(InputStream stream=Minecraft.getMinecraft().getResourceManager().getResource(requested).getInputStream()) { cached=requested; }
        catch(IOException missing) { cached=new ResourceLocation("textures/entity/sign.png"); }
        CACHE.put(requested,cached); return cached;
    }
}
