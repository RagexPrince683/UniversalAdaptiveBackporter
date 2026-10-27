package ganymedes01.etfuturum.api.blocksanditems.utils;
import net.minecraft.block.Block;
import ganymedes01.etfuturum.api.blocksanditems.utils.base.ObjMetaPair;
import net.minecraftforge.oredict.OreDictionary;
import java.util.*;
public class BlockMetaPair extends ObjMetaPair<Block> {
 private static final Map<Block,Map<Integer,BlockMetaPair>> INTERNER=new IdentityHashMap<>();
 public BlockMetaPair(Block object,int meta){super(object,meta,false);}
 protected BlockMetaPair(Block object,int meta,boolean interned){super(object,meta,interned);}
 public static synchronized BlockMetaPair intern(Block object,int meta){return INTERNER.computeIfAbsent(Objects.requireNonNull(object),key->new HashMap<>()).computeIfAbsent(meta,key->new BlockMetaPair(object,meta,true));}
 public static BlockMetaPair intern(Block object){return intern(object,OreDictionary.WILDCARD_VALUE);}
 public static BlockMetaPair of(Block object,int meta){return new BlockMetaPair(object,meta);}
}
