package ganymedes01.etfuturum.api.tags.containers;
import java.util.Set;
import ganymedes01.etfuturum.api.tags.interfaces.ITaggable;
/** Public container contract; native helpers own the canonical tag index. */
public abstract class TagContainerBasic<T> implements ITaggable {
    public abstract Set<String> getBaseTags();
}
