package ganymedes01.etfuturum;
import java.util.*;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import net.minecraft.block.*;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class StorageRules {
 private static final String[] DEFAULT_BANS=new String[]{
			"adventurebackpack:adventureBackpack",
			"arsmagica2:essenceBag",
			"arsmagica2:runeBag",
			"betterstorage:backpack",
			"betterstorage:cardboardBox",
			"betterstorage:thaumcraftBackpack",
			"BiblioCraft:item.BigBook",
			"Botania:baubleBox",
			"Botania:flowerBag",
			"cardboardboxes:cbCardboardBox",
			"dendrology:fullDrawers1",
			"dendrology:fullDrawers2",
			"dendrology:fullDrawers4",
			"dendrology:halfDrawers2",
			"dendrology:halfDrawers4",
			"DQMIIINext:ItemMahounoTutu11",
			"DQMIIINext:ItemOokinaFukuro",
			"DQMIIINext:ItemOokinaFukuroB",
			"DQMIIINext:ItemOokinaFukuroG",
			"DQMIIINext:ItemOokinaFukuroR",
			"DQMIIINext:ItemOokinaFukuroY",
			"ExtraSimple:bedrockium",
			"ExtraSimple:bedrockiumBlock",
			"ExtraSimple:goldenBag",
			"ExtraSimple:lasso",
			"ExtraUtilities:bedrockiumIngot",
			"ExtraUtilities:block_bedrockium",
			"ExtraUtilities:golden_bag",
			"ExtraUtilities:golden_lasso",
			"Forestry:adventurerBag",
			"Forestry:adventurerBagT2",
			"Forestry:apiaristBag",
			"Forestry:builderBag",
			"Forestry:builderBagT2",
			"Forestry:diggerBag",
			"Forestry:diggerBagT2",
			"Forestry:foresterBag",
			"Forestry:foresterBagT2",
			"Forestry:hunterBag",
			"Forestry:hunterBagT2",
			"Forestry:lepidopteristBag",
			"Forestry:minerBag",
			"Forestry:minerBagT2",
			"HardcoreEnderExpansion:charm_pouch",
			"ImmersiveEngineering:toolbox",
			"ironbackpacks:basicBackpack",
			"ironbackpacks:diamondBackpack",
			"ironbackpacks:goldBackpack",
			"ironbackpacks:ironBackpack",
			"JABBA:mover",
			"JABBA:moverDiamond",
			"MagicBees:backpack.thaumaturgeT1",
			"MagicBees:backpack.thaumaturgeT2",
			"minecraft:writable_book",
			"minecraft:written_book",
			"MineFactoryReloaded:plastic.bag",
			"MineFactoryReloaded:safarinet.reusable",
			"MineFactoryReloaded:safarinet.singleuse",
			"OpenBlocks:devnull",
			"ProjectE:item.pe_alchemical_bag",
			"ProjRed|Exploration:projectred.exploration.backpack",
			"sgs_treasure:dread_pirate_chest",
			"sgs_treasure:iron_chest",
			"sgs_treasure:locked_wooden_chest",
			"sgs_treasure:obsidian_chest",
			"sgs_treasure:pirate_chest",
			"sgs_treasure:wither_chest",
			"sgs_treasure:wooden_chest",
			"StorageDrawers:compDrawers",
			"StorageDrawers:fullCustom1",
			"StorageDrawers:fullCustom2",
			"StorageDrawers:fullCustom4",
			"StorageDrawers:fullDrawers1",
			"StorageDrawers:fullDrawers2",
			"StorageDrawers:fullDrawers4",
			"StorageDrawers:halfCustom2",
			"StorageDrawers:halfCustom4",
			"StorageDrawers:halfDrawers2",
			"StorageDrawers:halfDrawers4",
			"StorageDrawersBop:fullDrawers1",
			"StorageDrawersBop:fullDrawers1",
			"StorageDrawersBop:fullDrawers2",
			"StorageDrawersBop:fullDrawers2",
			"StorageDrawersBop:fullDrawers4",
			"StorageDrawersBop:fullDrawers4",
			"StorageDrawersBop:halfDrawers2",
			"StorageDrawersBop:halfDrawers2",
			"StorageDrawersBop:halfDrawers4",
			"StorageDrawersBop:halfDrawers4",
			"StorageDrawersForestry:fullDrawers1A",
			"StorageDrawersForestry:fullDrawers2A",
			"StorageDrawersForestry:fullDrawers4A",
			"StorageDrawersForestry:halfDrawers2A",
			"StorageDrawersForestry:halfDrawers4A",
			"StorageDrawersNatura:fullDrawers1",
			"StorageDrawersNatura:fullDrawers2",
			"StorageDrawersNatura:fullDrawers4",
			"StorageDrawersNatura:halfDrawers2",
			"StorageDrawersNatura:halfDrawers4",
			"Thaumcraft:FocusPouch",
			"ThaumicTinkerer:ichorPouch",
			"thebetweenlands:lurkerSkinPouch",
			"warpbook:warpbook",
			"witchery:brewbag",
			"WitchingGadgets:item.WG_Bag"
	};
 private static String[] configured;private static final Set<Item> banned=Collections.newSetFromMap(new IdentityHashMap<>());
 private StorageRules(){}
 static void initialize(){configured=UabConfiguration.get().property("functions","shulkerBans",DEFAULT_BANS,Property.Type.STRING).getStringList();}
 static void resolve(){banned.clear();for(String name:configured)try{ResourceLocation id=new ResourceLocation(name);if(Item.REGISTRY.containsKey(id))banned.add(Item.REGISTRY.getObject(id));}catch(IllegalArgumentException ignored){}}
 public static boolean prohibited(ItemStack stack){return !stack.isEmpty()&&(Block.getBlockFromItem(stack.getItem()) instanceof BlockShulkerBox||banned.contains(stack.getItem()));}
 @SubscribeEvent public static void wash(PlayerInteractEvent.RightClickBlock event){if(!Content.available("shulker_box")||!event.getEntityPlayer().canPlayerEdit(event.getPos(),event.getFace(),event.getItemStack()))return;net.minecraft.block.state.IBlockState state=event.getWorld().getBlockState(event.getPos());ItemStack held=event.getItemStack();if(state.getBlock()!=Blocks.CAULDRON||state.getValue(BlockCauldron.LEVEL)==0||!(Block.getBlockFromItem(held.getItem()) instanceof BlockShulkerBox)||Block.getBlockFromItem(held.getItem()) instanceof UndyedShulkerBox)return;if(!event.getWorld().isRemote){ItemStack result=Content.stack("shulker_box");if(held.hasTagCompound())result.setTagCompound(held.getTagCompound().copy());event.getEntityPlayer().setHeldItem(event.getHand(),result);event.getWorld().setBlockState(event.getPos(),state.withProperty(BlockCauldron.LEVEL,state.getValue(BlockCauldron.LEVEL)-1),2);}event.setCancellationResult(EnumActionResult.SUCCESS);event.setCanceled(true);}
 public static void color(EntityShulker entity,int color){NativeColorAccess.color(entity,color);}
 private static int dyeColor(ItemStack stack){int direct=ModernDye.color(stack);if(direct>=0)return direct;String[] names={"White","Orange","Magenta","LightBlue","Yellow","Lime","Pink","Gray","LightGray","Cyan","Purple","Blue","Brown","Green","Red","Black"};for(int ore:net.minecraftforge.oredict.OreDictionary.getOreIDs(stack)){String id=net.minecraftforge.oredict.OreDictionary.getOreName(ore);for(int i=0;i<names.length;i++)if(id.equals("dye"+names[i]))return i;}return -1;}
 @SubscribeEvent public static void dye(PlayerInteractEvent.EntityInteract event){if(!(event.getTarget() instanceof EntityShulker)||!Features.enabled("tweaks.dyableShulkers"))return;ItemStack held=event.getItemStack();EntityShulker shulker=(EntityShulker)event.getTarget();int color=held.getItem()==Items.WATER_BUCKET?16:dyeColor(held);if(color<0||color==NativeColorAccess.color(shulker))return;if(!event.getWorld().isRemote){NativeColorAccess.color(shulker,color);if(color!=16&&!event.getEntityPlayer().capabilities.isCreativeMode)held.shrink(1);}event.setCancellationResult(EnumActionResult.SUCCESS);event.setCanceled(true);}
 @SubscribeEvent public static void water(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event){if(event.getEntityLiving() instanceof EntityShulker&&!event.getEntityLiving().world.isRemote&&event.getEntityLiving().isInWater()&&Features.enabled("tweaks.dyableShulkers")){EntityShulker shulker=(EntityShulker)event.getEntityLiving();if(NativeColorAccess.color(shulker)!=16)NativeColorAccess.color(shulker,16);}}
 // Inheritance exposes the protected native key through normal mappings; no entity instance is created.
 private static final class NativeColorAccess extends EntityShulker {private NativeColorAccess(){super(null);}static int color(EntityShulker entity){return entity.getDataManager().get(COLOR)&255;}static void color(EntityShulker entity,int color){entity.getDataManager().set(COLOR,(byte)Math.max(0,Math.min(16,color)));}}
}
