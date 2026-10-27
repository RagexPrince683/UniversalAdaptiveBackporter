package ganymedes01.etfuturum.api.mappings;
import java.util.Random;
import net.minecraft.item.Item;
import ganymedes01.etfuturum.api.blocksanditems.utils.ItemMetaPair;
/** Retained raw-drop extension point, including metadata and the extra-drop policy. */
public class RawOreDropMapping extends ItemMetaPair {
 private boolean extra;
 public RawOreDropMapping(Item item,int meta){super(item,meta);}
 public boolean getDropsExtra(){return extra;}
 public void setDropsExtra(boolean extra){this.extra=extra;}
 public int getDropAmount(Random random,int fortune){fortune=Math.max(0,fortune);return extra?random.nextInt(3*(fortune+1)-1)+2:random.nextInt(fortune+1)+1;}
 @Override public boolean equals(Object other){return other instanceof RawOreDropMapping&&((RawOreDropMapping)other).extra==extra&&super.equals(other);}
}
