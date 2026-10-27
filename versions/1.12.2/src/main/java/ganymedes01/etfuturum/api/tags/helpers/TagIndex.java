package ganymedes01.etfuturum.api.tags.helpers;
import java.util.*;
import net.minecraftforge.oredict.OreDictionary;

/** Canonical native metadata tags. Mutations invalidate cached inheritance and reverse lookups. */
final class TagIndex<T> {
    private final Map<T,Map<Integer,Set<String>>> direct=new IdentityHashMap<>(),cached=new IdentityHashMap<>();
    private final Map<String,Set<String>> parents=new LinkedHashMap<>();
    private static void validate(String name){if(name==null||!name.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))throw new IllegalArgumentException("Invalid tag "+name);}
    synchronized void add(T object,int meta,String...tags){Objects.requireNonNull(object);if(tags.length==0)throw new IllegalArgumentException("No tags supplied");for(String tag:tags)validate(tag);Set<String> set=direct.computeIfAbsent(object,key->new HashMap<>()).computeIfAbsent(meta,key->new LinkedHashSet<>());Collections.addAll(set,tags);clear();}
    synchronized void remove(T object,int meta,String...tags){Map<Integer,Set<String>> map=direct.get(object);if(map!=null){Set<String> set=map.get(meta);if(set!=null){set.removeAll(Arrays.asList(tags));if(set.isEmpty())map.remove(meta);}if(map.isEmpty())direct.remove(object);}clear();}
    synchronized Set<String> base(T object,int meta){Map<Integer,Set<String>> map=direct.get(object);return map==null?Collections.emptySet():Collections.unmodifiableSet(new LinkedHashSet<>(map.getOrDefault(meta,Collections.emptySet())));}
    private void inherit(String tag,Set<String> result){for(String parent:parents.getOrDefault(tag,Collections.emptySet()))if(result.add(parent))inherit(parent,result);}
    synchronized Set<String> tags(T object,int meta){Map<Integer,Set<String>> map=cached.computeIfAbsent(object,key->new HashMap<>());Set<String> found=map.get(meta);if(found!=null)return found;Set<String> result=new LinkedHashSet<>(base(object,meta));if(meta!=OreDictionary.WILDCARD_VALUE)result.addAll(base(object,OreDictionary.WILDCARD_VALUE));for(String tag:new ArrayList<>(result))inherit(tag,result);found=Collections.unmodifiableSet(result);map.put(meta,found);return found;}
    synchronized void addParents(String child,String...values){validate(child);for(String parent:values){validate(parent);Set<String> ancestors=new HashSet<>();ancestors.add(parent);inherit(parent,ancestors);if(ancestors.contains(child))throw new IllegalArgumentException("Tag inheritance cycle: "+child+" -> "+parent);}Collections.addAll(parents.computeIfAbsent(child,key->new LinkedHashSet<>()),values);clear();}
    synchronized void removeParents(String child,String...values){Set<String> set=parents.get(child);if(set!=null)set.removeAll(Arrays.asList(values));clear();}
    synchronized Set<String> parents(String child){return Collections.unmodifiableSet(new LinkedHashSet<>(parents.getOrDefault(child,Collections.emptySet())));}
    synchronized Map<T,Set<Integer>> members(String tag){Map<T,Set<Integer>> result=new IdentityHashMap<>();for(Map.Entry<T,Map<Integer,Set<String>>> entry:direct.entrySet())for(int meta:entry.getValue().keySet())if(tags(entry.getKey(),meta).contains(tag))result.computeIfAbsent(entry.getKey(),key->new LinkedHashSet<>()).add(meta);return result;}
    synchronized void clear(){cached.clear();}
}
