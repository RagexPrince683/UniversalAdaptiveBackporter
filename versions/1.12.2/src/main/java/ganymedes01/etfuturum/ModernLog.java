package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.state.*;

final class ModernLog extends BlockLog {
    ModernLog() { setDefaultState(blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y)); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, LOG_AXIS); }
    @Override public IBlockState getStateFromMeta(int meta) {
        switch (meta & 12) {
            case 4: return getDefaultState().withProperty(LOG_AXIS, EnumAxis.X);
            case 8: return getDefaultState().withProperty(LOG_AXIS, EnumAxis.Z);
            case 12: return getDefaultState().withProperty(LOG_AXIS, EnumAxis.NONE);
            default: return getDefaultState();
        }
    }
    @Override public int getMetaFromState(IBlockState state) {
        switch (state.getValue(LOG_AXIS)) {
            case X: return 4;
            case Z: return 8;
            case NONE: return 12;
            default: return 0;
        }
    }
    @Override public int damageDropped(IBlockState state) { return 0; }
}
