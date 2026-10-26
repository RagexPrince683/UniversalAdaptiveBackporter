package ganymedes01.etfuturum.mixins.early.tags;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import ganymedes01.etfuturum.api.tags.helpers.BlockTags;
import ganymedes01.etfuturum.api.tags.interfaces.ITaggableMeta;

import java.util.Set;

@Mixin(Block.class)
public class MixinBlock implements ITaggableMeta {

    private final BlockTags container = new BlockTags((Block) (Object) this);

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
