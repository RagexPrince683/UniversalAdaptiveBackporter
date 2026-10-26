package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;

final class MaterialBlock extends Block {
    MaterialBlock(Material material, SoundType sound) { super(material); setSoundType(sound); }
}
