package ganymedes01.etfuturum.api.blocksanditems.utils;
import net.minecraft.item.Item;
import ganymedes01.etfuturum.api.blocksanditems.utils.base.ObjMetaPair;
import net.minecraftforge.oredict.OreDictionary;
import java.util.*;
public class ItemMetaPair extends ObjMetaPair<Item> {
 private static final Map<Item,Map<Integer,ItemMetaPair>> INTERNER=new IdentityHashMap<>();
 public ItemMetaPair(Item object,int meta){super(object,meta,false);}
 protected ItemMetaPair(Item object,int meta,boolean interned){super(object,meta,interned);}
 public static synchronized ItemMetaPair intern(Item object,int meta){return INTERNER.computeIfAbsent(Objects.requireNonNull(object),key->new HashMap<>()).computeIfAbsent(meta,key->new ItemMetaPair(object,meta,true));}
 public static ItemMetaPair intern(Item object){return intern(object,OreDictionary.WILDCARD_VALUE);}
 public static ItemMetaPair of(Item object,int meta){return new ItemMetaPair(object,meta);}
}
