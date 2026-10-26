package ganymedes01.etfuturum;

import net.minecraft.item.Item;
import net.minecraft.tileentity.BannerPattern;

final class BannerPatternItem extends Item {
    final BannerPattern pattern;
    BannerPatternItem(BannerPattern pattern) { this.pattern = pattern; setMaxStackSize(1); }
    static String name(BannerPattern pattern) {
        // The native filename accessor is stripped from dedicated servers.
        switch (pattern) {
            case STRIPE_SMALL: return "small_stripes";
            case DIAGONAL_RIGHT: return "diagonal_up_right";
            case DIAGONAL_LEFT_MIRROR: return "diagonal_up_left";
            case DIAGONAL_RIGHT_MIRROR: return "diagonal_right";
            case CIRCLE_MIDDLE: return "circle";
            case RHOMBUS_MIDDLE: return "rhombus";
            case HALF_VERTICAL_MIRROR: return "half_vertical_right";
            case HALF_HORIZONTAL_MIRROR: return "half_horizontal_bottom";
            default: return pattern.name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
