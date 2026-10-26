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
        RawOreDrops.initialize();
        setHardness(3); setResistance(3); setHarvestLevel("pickaxe", drop.equals("gold_nugget") ? 0 : 1);
    }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) {
        if (drop.equals("gold_nugget")) return Items.GOLD_NUGGET;
        return Content.available(drop) ? Content.item(drop) : Item.getItemFromBlock(this);
    }
    @Override public int quantityDroppedWithBonus(int fortune, Random random) {
        if (!drop.equals("gold_nugget") && !Content.available(drop)) return 1;
        return RawOreDrops.quantity(drop.equals("raw_copper") ? "oreCopper" : null, min, max, fortune, random);
    }
}
