package ganymedes01.etfuturum.mixins.early.tags;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import ganymedes01.etfuturum.api.tags.helpers.ItemTags;
import ganymedes01.etfuturum.api.tags.interfaces.ITaggableMeta;

import java.util.Set;

@Mixin(Item.class)
public class MixinItem implements ITaggableMeta {

    private final ItemTags container = new ItemTags((Item) (Object) this);

    @Override
    public synchronized void addTags(int meta, String... tags) {
        container.addTags(meta, tags);
    }

    @Override
    public synchronized void removeTags(int meta, String... tags) {
        container.removeTags(meta, tags);
    }

    @Override
    public synchronized Set<String> getTags(int meta) {
        return container.getTags(meta);
    }

    public synchronized void clearCaches() {
        container.clearCaches();
    }
}
