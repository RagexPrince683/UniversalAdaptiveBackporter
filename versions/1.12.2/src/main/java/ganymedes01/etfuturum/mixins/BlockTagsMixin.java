package ganymedes01.etfuturum.mixins;
import net.minecraft.block.Block;
import ganymedes01.etfuturum.api.tags.interfaces.ITaggableMeta;
import ganymedes01.etfuturum.api.tags.helpers.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import java.util.Set;
@Mixin(value=Block.class,remap=false)
public abstract class BlockTagsMixin implements ITaggableMeta {
 public void addTags(int meta,String...tags){BlockTags.addTags((Block)(Object)this,meta,tags);}
 public void removeTags(int meta,String...tags){BlockTags.removeTags((Block)(Object)this,meta,tags);}
 public Set<String> getTags(int meta){return BlockTags.getTags((Block)(Object)this,meta);}
 public void clearCaches(){new BlockTags((Block)(Object)this).clearCaches();}
}
