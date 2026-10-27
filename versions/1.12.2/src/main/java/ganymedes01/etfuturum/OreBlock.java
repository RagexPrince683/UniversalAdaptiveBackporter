package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public final class OreBlock extends Block {
    private final String drop;
    private final int min, max;
    public OreBlock(String drop, int min, int max) {
        super(Material.ROCK); this.drop = drop; this.min = min; this.max = max;
        setHardness(3); setResistance(3); setHarvestLevel("pickaxe", drop.equals("gold_nugget") ? 0 : 1);
    }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) {
        if (drop.equals("gold_nugget")) return Items.GOLD_NUGGET;
        ganymedes01.etfuturum.api.mappings.RawOreDropMapping mapping=rawMapping();
        return mapping!=null?mapping.get():drop.equals("raw_copper")?Item.getItemFromBlock(this):Content.available(drop)?Content.item(drop):Item.getItemFromBlock(this);
    }
    private ganymedes01.etfuturum.api.mappings.RawOreDropMapping rawMapping(){return drop.equals("raw_copper")&&Content.available(drop)?ganymedes01.etfuturum.api.RawOreRegistry.getOre("oreCopper"):null;}
    @Override public int damageDropped(IBlockState state){ganymedes01.etfuturum.api.mappings.RawOreDropMapping mapping=rawMapping();return mapping==null?0:mapping.getMeta();}
    @Override public int quantityDroppedWithBonus(int fortune, Random random) {
        if (!drop.equals("gold_nugget") && !Content.available(drop)) return 1;
        ganymedes01.etfuturum.api.mappings.RawOreDropMapping mapping=rawMapping();if(mapping!=null)return mapping.getDropAmount(random,fortune);
        if(drop.equals("raw_copper"))return 1;
        int base=min+random.nextInt(max-min+1);return base*(fortune<=0?1:Math.max(1,random.nextInt(fortune+2)));
    }
}
