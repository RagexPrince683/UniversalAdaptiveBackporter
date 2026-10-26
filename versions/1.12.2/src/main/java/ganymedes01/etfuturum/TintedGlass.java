package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;

final class TintedGlass extends BlockGlass {
    TintedGlass() { super(Material.GLASS, false); setHardness(.3F); setResistance(.3F); setLightOpacity(255); }
    @Override public int quantityDropped(Random random) { return 1; }
}
