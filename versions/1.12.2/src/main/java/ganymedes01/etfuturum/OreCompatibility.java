package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.registry.*;
import net.minecraftforge.oredict.*;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import ganymedes01.etfuturum.api.*;
import ganymedes01.etfuturum.api.blocksanditems.utils.BlockMetaPair;
import ganymedes01.etfuturum.api.mappings.RawOreDropMapping;

/** Native optional-ore discovery. Missing ores/ingots defer their paths; no substitute materials are created. */
public final class OreCompatibility {
 static final String[] METALS={"aluminum","tin","silver","lead","nickel","platinum","mythril","uranium","thorium","tungsten","titanium","zinc","magnesium","boron"};
 private static final IBlockState[] BASES=new IBlockState[14],STORAGE=new IBlockState[14];
 private static final boolean[] RAW=new boolean[14],DEEP=new boolean[14];
 private static final Set<String> RAW_BANS=new HashSet<>(),DEEP_BANS=new HashSet<>();
 private static boolean initialized;
 private static IBlockState foreignCopper;
 static IBlockState copperBase(){return !Content.available("copper_ore")?foreignCopper:null;}
 private OreCompatibility(){}
 static String[] rawNames(){String[] names=new String[14];for(int i=0;i<14;i++)names[i]="raw_"+METALS[i];return names;}
 static void define(){UabConfiguration cfg=UabConfiguration.get();Collections.addAll(RAW_BANS,cfg.property("modcompat","moddedRawOresBlacklist",new String[0],Property.Type.STRING).getStringList());Collections.addAll(DEEP_BANS,cfg.property("modcompat","moddedDeepslateOresBlacklist",new String[0],Property.Type.STRING).getStringList());SpecialOres.define();Content.add("modded_raw_ore","modcompat.moddedRawOres",new ModdedRawItem());Content.add("modded_raw_ore_block","modcompat.moddedRawOres",new ModdedOreBlock(false));Content.add("modded_deepslate_ore","modcompat.moddedDeepslateOres",new ModdedOreBlock(true));}
 public static boolean rawAvailable(int meta){return meta>=0&&meta<14&&RAW[meta];}
 public static boolean deepAvailable(int meta){return meta>=0&&meta<14&&DEEP[meta];}
 static IBlockState baseOre(int meta){return meta>=0&&meta<14?BASES[meta]:null;}
 static IBlockState baseStorage(int meta){return meta>=0&&meta<14?STORAGE[meta]:null;}
 public static boolean blockModel(String name){for(String metal:METALS)if(name.equals("raw_"+metal+"_block")||name.equals("deepslate_"+metal+"_ore"))return true;return false;}
 private static String[] suffixes(int meta){if(meta==0)return new String[]{"Aluminum","Aluminium"};if(meta==6)return new String[]{"Mythril","Mithril"};String metal=METALS[meta];return new String[]{Character.toUpperCase(metal.charAt(0))+metal.substring(1)};}
 static boolean deepBanned(String tag,ItemStack stack){return banned(DEEP_BANS,tag,stack);}
 static boolean rawBanned(String tag,ItemStack stack){return banned(RAW_BANS,tag,stack);}
 private static boolean banned(Set<String> bans,String tag,ItemStack stack){return bans.contains(tag)||stack.getItem().getRegistryName()==null||bans.contains(stack.getItem().getRegistryName().getNamespace());}
 private static ItemStack first(String name,Set<String> bans,boolean block){for(ItemStack stack:OreDictionary.getOres(name,false)){if(stack.isEmpty()||banned(bans,name,stack)||stack.getItem().getRegistryName().getNamespace().equals(UAB.ID))continue;if(!block)return stack.copy();if(!(stack.getItem() instanceof ItemBlock)||stack.getMetadata()<0||stack.getMetadata()>15)continue;Block base=Block.getBlockFromItem(stack.getItem());IBlockState state=base.getStateFromMeta(stack.getMetadata());if(!base.hasTileEntity(state))return stack.copy();}return ItemStack.EMPTY;}
 private static IBlockState state(ItemStack stack){return stack.isEmpty()?null:Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getMetadata());}
 private static void tag(String name,ItemStack stack){for(int id:OreDictionary.getOreIDs(stack))if(OreDictionary.getOreName(id).equals(name))return;OreDictionary.registerOre(name,stack);}
 private static void rawDefault(String name,Item item,int meta){if(!RawOreRegistry.hasOre(name))RawOreRegistry.addOre(name,item,meta);}
 public static void initialize(){
  if(initialized)return;initialized=true;
  for(Block block:Content.BLOCKS.values())if(block instanceof DeepslateOre&&Content.available(block.getRegistryName().getPath())){Block base=((DeepslateOre)block).base;if(base.getRegistryName().getNamespace().equals(UAB.ID)&&!Content.available(base.getRegistryName().getPath()))continue;for(IBlockState state:base.getBlockState().getValidStates())DeepslateOreRegistry.addOre(base,base.getMetaFromState(state),block,0,true);}
  if(DeepslateOreRegistry.hasOre(net.minecraft.init.Blocks.REDSTONE_ORE)){BlockMetaPair target=DeepslateOreRegistry.getOre(net.minecraft.init.Blocks.REDSTONE_ORE);DeepslateOreRegistry.addOre(net.minecraft.init.Blocks.LIT_REDSTONE_ORE,0,target.get(),target.getMeta(),true);}
  if(Content.available("raw_iron"))rawDefault("oreIron",Content.item("raw_iron"),0);if(Content.available("raw_gold"))rawDefault("oreGold",Content.item("raw_gold"),0);if(Content.available("raw_copper")&&(Content.available("copper_ingot")||!OreDictionary.getOres("ingotCopper",false).isEmpty()))rawDefault("oreCopper",Content.item("raw_copper"),0);
  for(String metal:new String[]{"Copper","Iron","Gold"})if(RawOreRegistry.hasOre("ore"+metal)&&!OreDictionary.getOres("oreNether"+metal,false).isEmpty()){RawOreDropMapping mapping=RawOreRegistry.getOre("ore"+metal);rawDefault("oreNether"+metal,mapping.get(),mapping.getMeta());}
  if(Features.enabled("modcompat.moddedDeepslateOres")&&Content.available("deepslate_copper_ore"))for(ItemStack ore:new ArrayList<>(OreDictionary.getOres("oreCopper",false)))if(ore.getItem() instanceof ItemBlock&&ore.getMetadata()>=0&&ore.getMetadata()<16&&!deepBanned("oreCopper",ore)){Block base=Block.getBlockFromItem(ore.getItem());if(base!=Content.block("copper_ore")&&!base.hasTileEntity(base.getStateFromMeta(ore.getMetadata()))){if(foreignCopper==null)foreignCopper=base.getStateFromMeta(ore.getMetadata());DeepslateOreRegistry.addOre(base,ore.getMetadata(),Content.block("deepslate_copper_ore"),0,true);}}
  boolean raw=Features.enabled("modcompat.moddedRawOres")&&Features.enabled("blocksitems.enableRawOres"),deep=Features.enabled("modcompat.moddedDeepslateOres")&&Features.enabled("blocksitems.enableDeepslate")&&Features.enabled("blocksitems.enableDeepslateOres");
  for(int meta=0;meta<14;meta++){
   ItemStack ingot=ItemStack.EMPTY;String chosen=null;
   for(String suffix:suffixes(meta)){ItemStack ore=first("ore"+suffix,DEEP_BANS,true);if(BASES[meta]==null&&!ore.isEmpty())BASES[meta]=state(ore);if(STORAGE[meta]==null)STORAGE[meta]=state(first("block"+suffix,RAW_BANS,true));ItemStack input=first("ore"+suffix,RAW_BANS,true),output=first("ingot"+suffix,RAW_BANS,false);if(!input.isEmpty()&&!output.isEmpty()){RAW[meta]=raw;ingot=output;chosen=suffix;}}
   DEEP[meta]=deep&&BASES[meta]!=null;
   if(DEEP[meta])for(String suffix:suffixes(meta)){String name="ore"+suffix;if(DEEP_BANS.contains(name))continue;for(ItemStack ore:new ArrayList<>(OreDictionary.getOres(name,false)))if(!banned(DEEP_BANS,name,ore)&&ore.getItem() instanceof ItemBlock&&ore.getMetadata()>=0&&ore.getMetadata()<16){Block base=Block.getBlockFromItem(ore.getItem());if(!base.getRegistryName().getNamespace().equals(UAB.ID)&&!base.hasTileEntity(base.getStateFromMeta(ore.getMetadata())))DeepslateOreRegistry.addOre(base,ore.getMetadata(),Content.block("modded_deepslate_ore"),meta,true);}tag(name,new ItemStack(Content.block("modded_deepslate_ore"),1,meta));}
   if(RAW[meta]){ItemStack item=new ItemStack(Content.item("modded_raw_ore"),1,meta),block=new ItemStack(Content.block("modded_raw_ore_block"),1,meta);for(String suffix:suffixes(meta)){if(RAW_BANS.contains("ore"+suffix)||first("ore"+suffix,RAW_BANS,true).isEmpty()||first("ingot"+suffix,RAW_BANS,false).isEmpty())continue;rawDefault("ore"+suffix,item.getItem(),meta);if(!OreDictionary.getOres("oreNether"+suffix,false).isEmpty())rawDefault("oreNether"+suffix,item.getItem(),meta);tag("rawOre"+suffix,item.copy());tag("blockRaw"+suffix,block.copy());if(Features.enabled("functions.registerRawItemAsOre"))tag("ore"+suffix,item.copy());}String name="modded_raw_"+METALS[meta];ForgeRegistries.RECIPES.register(new ShapedOreRecipe(new ResourceLocation(UAB.ID,name),block.copy(),"RRR","RRR","RRR",'R',item.copy()).setRegistryName(UAB.ID,name+"_block"));ItemStack nine=item.copy();nine.setCount(9);ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID,name),nine,block.copy()).setRegistryName(UAB.ID,name+"_from_block"));ItemStack result=ingot.copy();result.setCount(1);GameRegistry.addSmelting(item.copy(),result,.7F);}
  }
  if(Content.available("raw_copper")&&!Content.available("copper_ingot")){ItemStack ingot=first("ingotCopper",RAW_BANS,false);if(!ingot.isEmpty()){ingot.setCount(1);GameRegistry.addSmelting(Content.item("raw_copper"),ingot,.7F);}}
  SpecialOres.initialize();
  for(Map.Entry<BlockMetaPair,BlockMetaPair> entry:DeepslateOreRegistry.getOreMap().entrySet()){ItemStack original=new ItemStack(entry.getKey().get(),1,entry.getKey().getMeta()),result=FurnaceRecipes.instance().getSmeltingResult(original);if(!result.isEmpty())GameRegistry.addSmelting(new ItemStack(entry.getValue().get(),1,entry.getValue().getMeta()),result.copy(),FurnaceRecipes.instance().getSmeltingExperience(result));}
 }
 static void harvest(BlockEvent.HarvestDropsEvent event) {
  if(event.isSilkTouching()||event.getDrops().isEmpty())return;
  IBlockState state=event.getState();Block block=state.getBlock();
  ItemStack input=new ItemStack(block,1,block.getMetaFromState(state));
  // Fluid destruction also fires harvest events for blocks with no item form.
  // Forge 1.12 rejects empty stacks in getOreIDs, even when there are no ore drops.
  if(input.isEmpty()||block.getRegistryName()==null)return;
  int dropMeta=block.damageDropped(state);boolean candidate=false;
  for(ItemStack drop:event.getDrops())if(drop!=null&&!drop.isEmpty()&&drop.getItem()==input.getItem()&&drop.getMetadata()==dropMeta){candidate=true;break;}
  if(!candidate)return;
  RawOreDropMapping mapping=null;
  for(int id:OreDictionary.getOreIDs(input)) {
   String name=OreDictionary.getOreName(id);
   if(!block.getRegistryName().getNamespace().equals(UAB.ID)&&!block.getRegistryName().getNamespace().equals("minecraft")&&rawBanned(name,input))continue;
   mapping=RawOreRegistry.getOre(name);if(mapping!=null)break;
  }
  if(mapping==null)return;
  for(int i=0;i<event.getDrops().size();i++) {
   ItemStack drop=event.getDrops().get(i);
   if(drop==null||drop.isEmpty()||drop.getItem()!=input.getItem()||drop.getMetadata()!=dropMeta)continue;
   ItemStack raw=new ItemStack(mapping.get(),drop.getCount()*mapping.getDropAmount(event.getWorld().rand,event.getFortuneLevel()),mapping.getMeta());
   if(drop.hasTagCompound())raw.setTagCompound(drop.getTagCompound().copy());
   event.getDrops().set(i,raw);
  }
 }
 public static boolean layerSupported(IChunkProvider provider,int dimension){UabConfiguration cfg=UabConfiguration.get();if(Features.integer("deepslateGenerationMode",0,-1,1)!=0||Features.integer("deepslateMaxY",22,0,255)<=0)return false;if(provider instanceof ChunkProviderServer&&((ChunkProviderServer)provider).chunkGenerator instanceof ChunkGeneratorFlat)return false;boolean listed=false;for(int id:cfg.property("world","deepslateLayerDimensionBlacklist",new int[]{-1,1},Property.Type.INTEGER).getIntList())listed|=id==dimension;return listed==cfg.bool("world","deepslateLayerDimensionBlacklistAsWhitelist",false);}
 public static int height(World world){int top=Features.integer("deepslateMaxY",22,0,255);for(int id:UabConfiguration.get().property("world","replaceAllStoneWithDeepslateDimensionWhitelist",new int[0],Property.Type.INTEGER).getIntList())if(id==world.provider.getDimension())return world.getHeight();return Math.min(top,world.getHeight());}
}
