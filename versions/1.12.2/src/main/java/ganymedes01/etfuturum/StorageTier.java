package ganymedes01.etfuturum;

/** Barrel ordinals and the separate shulker order preserve their maintained Type NBT contracts. */
public enum StorageTier {
 WOOD(27,9,""),IRON(54,9,"iron"),GOLD(81,9,"gold"),DIAMOND(108,12,"diamond"),CRYSTAL(108,12,"diamond"),COPPER(45,9,"copper"),SILVER(72,9,"silver"),STEEL(72,9,"silver"),OBSIDIAN(108,12,"diamond"),DARKSTEEL(135,15,"netherite"),NETHERITE(135,15,"netherite");
 public final int size,columns; public final String gui;
 StorageTier(int size,int columns,String gui){this.size=size;this.columns=columns;this.gui=gui;}
 public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
 public static StorageTier read(int value){return value>=0&&value<values().length?values()[value]:WOOD;}
 public static StorageTier named(String value){try{return valueOf(value.equals("VANILLA")?"WOOD":value);}catch(IllegalArgumentException ignored){return null;}}
 private static final StorageTier[] SHULKER={WOOD,IRON,GOLD,DIAMOND,COPPER,SILVER,CRYSTAL,OBSIDIAN};
 public static StorageTier shulker(int value){return value>=0&&value<SHULKER.length?SHULKER[value]:WOOD;}
 public int shulkerId(){for(int i=0;i<SHULKER.length;i++)if(SHULKER[i]==this)return i;return 0;}
 public static StorageTier of(net.minecraft.inventory.IInventory inventory){return inventory instanceof BarrelTile?((BarrelTile)inventory).tier():inventory instanceof TieredShulker?((TieredShulker)inventory).uab$tier():WOOD;}
}
