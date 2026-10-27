package ganymedes01.etfuturum;
/** Keeps the maintained fourteen metadata slots, exposing only ores with real prerequisites. */
public final class ModdedRawItem extends VariantItem {
 ModdedRawItem(){super(OreCompatibility.rawNames());setMaxStackSize(64);}
 @Override public boolean available(int meta){return super.available(meta)&&OreCompatibility.rawAvailable(meta);}
}
