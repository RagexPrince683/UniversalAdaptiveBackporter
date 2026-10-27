package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.ILockableContainer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/** Optional native Iron Chests discovery; stored capacity survives disabling or removing the integration. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class IronStorage {
 static final String[] SHULKER_UPGRADES={"wood_iron","wood_copper","iron_gold","gold_diamond","diamond_obsidian","diamond_crystal","copper_iron","copper_silver","silver_gold"};
 static final String[] BARREL_UPGRADES={"wood_iron","wood_copper","iron_gold","gold_diamond","steel_gold","diamond_obsidian","obsidian_netherite","obsidian_darksteel","copper_iron","copper_silver","silver_gold","copper_steel","iron_silver","iron_steel","diamond_crystal","diamond_darksteel","crystal_darksteel"};
 private static String[] paths(boolean shulker){return shulker?SHULKER_UPGRADES:BARREL_UPGRADES;}
 private static final Set<StorageTier> available=EnumSet.of(StorageTier.WOOD);
 private static final Map<Item,Upgrade> external=new IdentityHashMap<>();
 private static final Map<String,Upgrade> upgrades=new HashMap<>();
 private static final Map<TileEntity,Integer> crystalHashes=Collections.synchronizedMap(new WeakHashMap<>());
 private IronStorage(){}
 public static void preview(NBTTagCompound tag,List<ItemStack> items){List<ItemStack> top=new ArrayList<>();for(ItemStack stack:items){if(stack.isEmpty())continue;int index=0;while(index<top.size()&&top.get(index).getCount()>=stack.getCount())index++;if(index<8){top.add(index,stack);if(top.size()>8)top.remove(8);}}net.minecraft.util.NonNullList<ItemStack> visible=net.minecraft.util.NonNullList.withSize(8,ItemStack.EMPTY);for(int i=0;i<top.size();i++)visible.set(i,top.get(i));ItemStackHelper.saveAllItems(tag,visible);}
 public static void syncCrystal(TileEntity tile){if(tile.getWorld()==null||tile.getWorld().isRemote||tile.getWorld().getTotalWorldTime()%20!=0||StorageTier.of((IInventory)tile)!=StorageTier.CRYSTAL||tile instanceof TileEntityLockableLoot&&((TileEntityLockableLoot)tile).getLootTable()!=null)return;IInventory inventory=(IInventory)tile;int hash=1;for(int i=0;i<inventory.getSizeInventory();i++){ItemStack stack=inventory.getStackInSlot(i);hash=31*hash+(stack.isEmpty()?0:Objects.hash(stack.getItem(),stack.getMetadata(),stack.getCount(),stack.getTagCompound()));}Integer old=crystalHashes.put(tile,hash);if(old==null||old!=hash){IBlockState state=tile.getWorld().getBlockState(tile.getPos());tile.getWorld().notifyBlockUpdate(tile.getPos(),state,state,2);}}
 public static final class Upgrade {public final StorageTier from,to;public final Item item;Upgrade(StorageTier from,StorageTier to,Item item){this.from=from;this.to=to;this.item=item;}}
 static void define(){
  for(StorageTier tier:StorageTier.values())if(tier!=StorageTier.WOOD){Content.add(tier.id()+"_barrel","modcompat.barrelIronChest",new BarrelBlock(tier));Content.requires(tier.id()+"_barrel","blocksitems.enableBarrel");}
  Content.add("barrel_upgrade","modcompat.barrelIronChestUpgradeItem",new UpgradeItem(false));Content.requires("barrel_upgrade","modcompat.barrelIronChest","blocksitems.enableBarrel");
  Content.add("shulker_box_upgrade","modcompat.shulkerBoxesIronChestUpgradeItem",new UpgradeItem(true));Content.requires("shulker_box_upgrade","modcompat.shulkerBoxesIronChest","blocksitems.enableShulkerBoxes");
 }
 public static boolean available(StorageTier tier){return available.contains(tier);}
 public static boolean upgradeAvailable(boolean shulker,int meta){return meta>=0&&meta<paths(shulker).length&&upgrades.containsKey(paths(shulker)[meta]);}
 static void initialize(){
  if(!Loader.isModLoaded("ironchest"))return;
  try{
   Class<?> types=Class.forName("cpw.mods.ironchest.common.blocks.chest.IronChestType");
   for(Object type:types.getEnumConstants()){StorageTier tier=StorageTier.named(((Enum<?>)type).name());if(tier!=null)available.add(tier);}
   Class<?> changers=Class.forName("cpw.mods.ironchest.common.items.ChestChangerType");
   for(Object changer:changers.getEnumConstants()){StorageTier from=StorageTier.named(((Enum<?>)changers.getField("source").get(changer)).name()),to=StorageTier.named(((Enum<?>)changers.getField("target").get(changer)).name());Object item=changers.getField("item").get(changer);if(from==null||to==null||!(item instanceof Item)||to.size<from.size)continue;Upgrade upgrade=new Upgrade(from,to,(Item)item);external.put((Item)item,upgrade);upgrades.put(from.id()+"_"+to.id(),upgrade);}
   for(boolean shulker:new boolean[]{false,true})for(int meta=0;meta<paths(shulker).length;meta++){
    Upgrade upgrade=upgrades.get(paths(shulker)[meta]);if(upgrade==null)continue;
    String name=shulker?"shulker_box_upgrade":"barrel_upgrade";if(!Content.available(name))continue;
    ItemStack own=new ItemStack(Content.item(name),1,meta),base=new ItemStack(upgrade.item);Object[] input=shulker?new Object[]{base,net.minecraft.init.Items.SHULKER_SHELL}:new Object[]{base};register(name+"_"+meta,own,input);register(name+"_return_"+meta,base,own.copy());
   }
  }catch(ReflectiveOperationException failure){throw new IllegalStateException("Cannot initialize native Iron Chests storage integration",failure);}
 }
 private static void register(String name,ItemStack output,Object...input){ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID,name),output,input).setRegistryName(UAB.ID,name));}
 private static Upgrade upgrade(ItemStack stack,boolean shulker){if(stack.isEmpty())return null;if(stack.getItem() instanceof UpgradeItem){UpgradeItem own=(UpgradeItem)stack.getItem();return own.shulker==shulker&&Content.available(stack.getItem().getRegistryName().getPath())&&upgradeAvailable(shulker,stack.getMetadata())?upgrades.get(paths(shulker)[stack.getMetadata()]):null;}return Content.available(shulker?"shulker_box_upgrade":"barrel_upgrade")?null:external.get(stack.getItem());}
 public static boolean open(EntityPlayer player,IInventory inventory,World world,BlockPos pos){
  if(StorageTier.of(inventory)==StorageTier.WOOD){player.displayGUIChest(inventory);return true;}
  if(inventory instanceof ILockableContainer&&!player.canOpen(((ILockableContainer)inventory).getLockCode())&&!player.isSpectator())return false;
  if(inventory instanceof TileEntityLockableLoot){TileEntityLockableLoot loot=(TileEntityLockableLoot)inventory;if(player.isSpectator()&&loot.getLootTable()!=null)return false;loot.fillWithLoot(player);}
  // Block notifications flush later in the tick. The client must know the tier before sizing its window.
  if(player instanceof EntityPlayerMP&&inventory instanceof TileEntity){EntityPlayerMP receiver=(EntityPlayerMP)player;receiver.connection.sendPacket(new net.minecraft.network.play.server.SPacketBlockChange(world,pos));net.minecraft.network.play.server.SPacketUpdateTileEntity update=((TileEntity)inventory).getUpdatePacket();if(update!=null)receiver.connection.sendPacket(update);}
  player.openGui(UAB.instance,5,world,pos.getX(),pos.getY(),pos.getZ());return true;
 }
 @SubscribeEvent public static void use(PlayerInteractEvent.RightClickBlock event){
  World world=event.getWorld();BlockPos pos=event.getPos();TileEntity tile=world.getTileEntity(pos);boolean shulker=tile instanceof TileEntityShulkerBox;if(!shulker&&!(tile instanceof BarrelTile))return;
  if(!Features.enabled(shulker?"modcompat.shulkerBoxesIronChest":"modcompat.barrelIronChest"))return;
  Upgrade upgrade=upgrade(event.getItemStack(),shulker);if(upgrade==null||upgrade.from!=StorageTier.of((IInventory)tile)||!event.getEntityPlayer().canPlayerEdit(pos,event.getFace(),event.getItemStack()))return;
  if(tile instanceof ILockableContainer&&!event.getEntityPlayer().canOpen(((ILockableContainer)tile).getLockCode()))return;
  event.setCanceled(true);event.setCancellationResult(EnumActionResult.SUCCESS);if(world.isRemote)return;
  for(EntityPlayer viewer:new ArrayList<>(world.playerEntities)){Container container=viewer.openContainer;if(container instanceof StorageContainer&&((StorageContainer)container).storage==tile||container instanceof ContainerChest&&((ContainerChest)container).getLowerChestInventory()==tile||container instanceof ContainerShulkerBox&&container.inventorySlots.size()>0&&container.inventorySlots.get(0).inventory==tile)viewer.closeScreen();}
  NBTTagCompound saved=tile.writeToNBT(new NBTTagCompound());saved.setByte("Type",(byte)(shulker?upgrade.to.shulkerId():upgrade.to.ordinal()));IBlockState before=world.getBlockState(pos);
  if(shulker){tile.readFromNBT(saved);}else{
   BarrelTile barrel=(BarrelTile)tile;barrel.upgrading=true;
   boolean placed;try{placed=world.setBlockState(pos,Content.block(upgrade.to.id()+"_barrel").getDefaultState().withProperty(BarrelBlock.FACING,before.getValue(BarrelBlock.FACING)),3);}finally{barrel.upgrading=false;}
   if(!placed)return;tile=world.getTileEntity(pos);if(!(tile instanceof BarrelTile))throw new IllegalStateException("Missing upgraded barrel at "+pos);tile.readFromNBT(saved);
  }
  tile.markDirty();IBlockState state=world.getBlockState(pos);world.notifyBlockUpdate(pos,state,state,3);world.updateComparatorOutputLevel(pos,state.getBlock());if(!event.getEntityPlayer().capabilities.isCreativeMode)event.getItemStack().shrink(1);
 }
 private static final class UpgradeItem extends VariantItem {
  final boolean shulker;
  UpgradeItem(boolean shulker){super(names(shulker));this.shulker=shulker;setMaxStackSize(64);}
  private static String[] names(boolean shulker){String[] names=new String[paths(shulker).length];for(int i=0;i<names.length;i++)names[i]=(shulker?"shulker_":"barrel_")+(shulker?paths(shulker)[i].replace("wood_","vanilla_"):paths(shulker)[i])+"_upgrade";return names;}
  @Override public boolean available(int meta){return super.available(meta)&&upgradeAvailable(shulker,meta);}
  @Override public boolean hasContainerItem(ItemStack stack){return shulker&&Features.enabled("modcompat.shulkerUpgradeReturnsShell");}
  @Override public ItemStack getContainerItem(ItemStack stack){return hasContainerItem(stack)?new ItemStack(net.minecraft.init.Items.SHULKER_SHELL):ItemStack.EMPTY;}
 }
}
