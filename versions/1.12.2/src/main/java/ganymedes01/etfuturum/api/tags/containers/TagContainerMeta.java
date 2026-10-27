package ganymedes01.etfuturum.api.tags.containers;
import ganymedes01.etfuturum.api.blocksanditems.utils.base.ObjMetaPair;
import java.util.Set;
/** Compatibility contract; the native metadata index owns storage and invalidation. */
public abstract class TagContainerMeta<T,P extends ObjMetaPair<T>> {
    public abstract void addTags(int meta,String...tags);
    public abstract void removeTags(int meta,String...tags);
    public abstract Set<String> getTags(int meta);
    public abstract Set<String> getBaseTags(int meta);
    public abstract Set<String> getWildcardTags();
    public abstract void clearCaches();
}
