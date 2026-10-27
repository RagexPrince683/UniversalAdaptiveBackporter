package ganymedes01.etfuturum.mixins;
import net.minecraft.item.Item;
import ganymedes01.etfuturum.api.tags.interfaces.ITaggableMeta;
import ganymedes01.etfuturum.api.tags.helpers.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import java.util.Set;
@Mixin(value=Item.class,remap=false)
public abstract class ItemTagsMixin implements ITaggableMeta {
 public void addTags(int meta,String...tags){ItemTags.addTags((Item)(Object)this,meta,tags);}
 public void removeTags(int meta,String...tags){ItemTags.removeTags((Item)(Object)this,meta,tags);}
 public Set<String> getTags(int meta){return ItemTags.getTags((Item)(Object)this,meta);}
 public void clearCaches(){new ItemTags((Item)(Object)this).clearCaches();}
}
