package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.registry.*;
import net.minecraftforge.oredict.*;
import ganymedes01.etfuturum.api.*;
/** Retained optional ore identities resolve real native ores and metadata, never substitute ores. */
public final class SpecialOres {
 private static final List<Family> families=new ArrayList<>();
 private static boolean adamantium;
 private SpecialOres(){}
 static void define(){
  family("deepslate_certus_quartz_ore","ae2","appliedenergistics2","CertusQuartz,ChargedCertusQuartz","deepslate_certus_quartz_ore,deepslate_charged_certus_quartz_ore");
  family("deepslate_draconium_ore","draconic","draconicevolution","Draconium","deepslate_draconium_ore");
  family("deepslate_adamantium_ore","simpleores","simpleores","Adamantium","deepslate_adamantium_ore");
  family("deepslate_am2_ore","am2","arsmagica2","Vinteum,Chimerite,BlueTopaz","deepslate_vinteum_ore,deepslate_chimerite_ore,deepslate_blue_topaz_ore");
  family("deepslate_thaumcraft_ore","thaumcraft","thaumcraft","Cinnabar,InfusedAir,InfusedFire,InfusedWater,InfusedEarth,InfusedOrder,InfusedEntropy,Amber","deepslate_cinnabar_ore,air_infused_deepslate,fire_infused_deepslate,water_infused_deepslate,earth_infused_deepslate,order_infused_deepslate,entropy_infused_deepslate,amber_bearing_deepslate");
  family("deepslate_bop_ore","bop","biomesoplenty","Ruby,Peridot,Topaz,Tanzanite,Malachite,Sapphire,Amber","deepslate_ruby_ore,deepslate_peridot_ore,deepslate_topaz_ore,deepslate_tanzanite_ore,deepslate_malachite_ore,deepslate_sapphire_ore,deepslate_amber_ore");
  family("deepslate_projred_ore","projectred","projectred-exploration,projectred_exploration","Ruby,Sapphire,Peridot,Electrotine","deepslate_ruby_ore,deepslate_sapphire_ore,deepslate_peridot_ore,deepslate_electrotine_ore");
  family("deepslate_bluepower_ore","bluepower","bluepower","Teslatite,Ruby,Sapphire,Amethyst","deepslate_teslatite_ore,deepslate_ruby_ore,deepslate_sapphire_ore,deepslate_amethyst_ore");
  family("deepslate_dbc_ore","dbc","jinryuudragonblockc","Warenai,Jjay,Dlog,Lehnori","deepslate_warenai_ore,deepslate_jjay_ore,deepslate_dlog_ore,deepslate_lehnori_ore");
  family("deepslate_dq_ore","dragonquest","dqmiiinext","Rockbomb,Brighten,Lucida,Resurrock,Mirrorstone,IceCrystal,Minimedal,Densinium,GlassFrit,LunarDiamond,Corundum,Sunstone,AlloyedIron,Chronocrystal,Volcanic","deepslate_bakudanisi_ore,deepslate_hikarinoisi_ore,deepslate_hosinokakera_ore,deepslate_inotinoisi_ore,deepslate_kagaminoisi_ore,deepslate_koorinokessyou_ore,deepslate_littlemedal_ore,deepslate_metaru_ore,deepslate_migakizuna_ore,deepslate_moon_ore,deepslate_rubi_ore,deepslate_taiyounoisi_ore,deepslate_tekkouseki_ore,deepslate_tokinosuisyou_ore,deepslate_yougansekinokakera_ore");
  Content.add("raw_adamantium","modcompat.moddedRawOres",new Item(){@Override public void getSubItems(net.minecraft.creativetab.CreativeTabs tab,NonNullList<ItemStack> items){if(adamantium)super.getSubItems(tab,items);}}).setTranslationKey("etfuturum.simpleores.raw_adamantium");
  Content.add("raw_adamantium_block","modcompat.moddedRawOres",new Block(net.minecraft.block.material.Material.IRON){@Override public void getSubBlocks(net.minecraft.creativetab.CreativeTabs tab,NonNullList<ItemStack> items){if(adamantium)super.getSubBlocks(tab,items);}}.setHardness(5).setResistance(6)).setTranslationKey("etfuturum.simpleores.raw_adamantium_block");
 }
 private static void family(String id,String folder,String namespaces,String ores,String names){Family family=new Family(folder,namespaces.split(","),ores.split(","),names.split(","));families.add(family);Content.add(id,"modcompat.moddedDeepslateOres",family);Content.requires(id,"blocksitems.enableDeepslate","blocksitems.enableDeepslateOres");}
 static void initialize(){
  for(Family family:families)for(int meta=0;meta<family.ores.length;meta++){
   String oreName="ore"+family.ores[meta];if(!Content.available(family.getRegistryName().getPath()))continue;
   for(ItemStack stack:new ArrayList<>(OreDictionary.getOres(oreName,false))){if(stack.isEmpty()||!(stack.getItem() instanceof ItemBlock)||stack.getMetadata()<0||stack.getMetadata()>15||OreCompatibility.deepBanned(oreName,stack))continue;ResourceLocation id=stack.getItem().getRegistryName();if(id==null||!family.namespaces.contains(id.getNamespace()))continue;Block block=Block.getBlockFromItem(stack.getItem());IBlockState state=block.getStateFromMeta(stack.getMetadata());if(block.hasTileEntity(state))continue;if(family.bases[meta]==null)family.bases[meta]=state;DeepslateOreRegistry.addOre(block,stack.getMetadata(),family,meta,true);}
   if(family.bases[meta]!=null)OreDictionary.registerOre(oreName,new ItemStack(family,1,meta));
  }
  ItemStack ore=owned("oreAdamantium","simpleores"),ingot=owned("ingotAdamantium","simpleores");adamantium=Features.enabled("modcompat.moddedRawOres")&&Features.enabled("blocksitems.enableRawOres")&&!ore.isEmpty()&&!ingot.isEmpty()&&!OreCompatibility.rawBanned("oreAdamantium",ore)&&!OreCompatibility.rawBanned("ingotAdamantium",ingot);
  if(adamantium){Item item=Content.item("raw_adamantium");RawOreRegistry.addOre("oreAdamantium",item,0);OreDictionary.registerOre("rawOreAdamantium",new ItemStack(item));OreDictionary.registerOre("blockRawAdamantium",new ItemStack(Content.block("raw_adamantium_block")));if(Features.enabled("functions.registerRawItemAsOre"))OreDictionary.registerOre("oreAdamantium",new ItemStack(item));GameRegistry.addSmelting(item,ingot.copy(),.7F);ForgeRegistries.RECIPES.register(new ShapedOreRecipe(new ResourceLocation(UAB.ID,"raw_adamantium_block"),new ItemStack(Content.block("raw_adamantium_block")),"RRR","RRR","RRR",'R',item).setRegistryName(UAB.ID,"raw_adamantium_block"));ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID,"raw_adamantium"),new ItemStack(item,9),Content.block("raw_adamantium_block")).setRegistryName(UAB.ID,"raw_adamantium_from_block"));}
 }
 private static ItemStack owned(String ore,String namespace){for(ItemStack stack:OreDictionary.getOres(ore,false))if(!stack.isEmpty()&&stack.getItem().getRegistryName()!=null&&stack.getItem().getRegistryName().getNamespace().equals(namespace))return stack;return ItemStack.EMPTY;}
 public static String texture(String model){for(Family family:families)for(int i=0;i<family.names.length;i++)if(family.variant(i).equals(model)){String name=family.names[i];if(family.folder.equals("thaumcraft")&&i>0&&i<7)name="infused_deepslate";return "etfuturum:blocks/"+family.folder+"/"+name;}return null;}
 public static int color(Block block,int meta){return block instanceof Family&&((Family)block).folder.equals("thaumcraft")&&meta>0&&meta<7?new int[]{0xFFFFFF,0xFFFF7E,0xFF3C01,0x0090FF,0x00A000,0xEECCFF,0x555577}[meta]:0xFFFFFF;}
 public static final class Family extends ModdedOreBlock {
  final String folder;final Set<String> namespaces;final String[] ores,names;final IBlockState[] bases;
  Family(String folder,String[] namespaces,String[] ores,String[] names){super(true);this.folder=folder;this.namespaces=new HashSet<>(Arrays.asList(namespaces));this.ores=ores;this.names=names;this.bases=new IBlockState[names.length];}
  @Override public int variants(){return names.length;}
  @Override public String variant(int meta){return "compat/"+folder+"/"+names[Math.max(0,Math.min(names.length-1,meta))];}
  @Override public boolean available(int meta){return meta>=0&&meta<bases.length&&bases[meta]!=null&&Content.available(getRegistryName().getPath());}
  @Override protected IBlockState base(IBlockState state){int meta=getMetaFromState(state);return meta<bases.length?bases[meta]:null;}
 }
}
