package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.api.tags.helpers.BiomeTags;
import ganymedes01.etfuturum.api.tags.interfaces.ITaggable;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import java.util.Set;
@Mixin(value=Biome.class,remap=false)
public class BiomeTagsMixin implements ITaggable {
    public void addTags(String...tags){BiomeTags.addTags((Biome)(Object)this,tags);}
    public void removeTags(String...tags){BiomeTags.removeTags((Biome)(Object)this,tags);}
    public Set<String> getTags(){return BiomeTags.getTags((Biome)(Object)this);}
    public void clearCaches(){new BiomeTags((Biome)(Object)this).clearCaches();}
}
