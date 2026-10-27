package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class LegacySpawns {
 public static final String NATURAL_SHULKER="UABNaturalShulker";
 private LegacySpawns(){}
 static void initialize(){
  for(Biome biome:Biome.REGISTRY){
   List<Biome.SpawnListEntry> monsters=biome.getSpawnableList(EnumCreatureType.MONSTER);
   if(Features.enabled("world.oldHuskSpawning")&&BiomeDictionary.hasType(biome,BiomeDictionary.Type.SANDY)&&BiomeDictionary.hasType(biome,BiomeDictionary.Type.DRY)&&BiomeDictionary.hasType(biome,BiomeDictionary.Type.HOT)){monsters.removeIf(entry->entry.entityClass==EntityZombie.class||entry.entityClass==EntityHusk.class);monsters.add(new Biome.SpawnListEntry(EntityZombie.class,20,4,4));ganymedes01.etfuturum.configuration.SpawnSettings.add(monsters,EntityHusk.class,80,4,4);}
   if(Features.enabled("world.oldStraySpawning")&&BiomeDictionary.hasType(biome,BiomeDictionary.Type.SNOWY)){monsters.removeIf(entry->entry.entityClass==EntitySkeleton.class||entry.entityClass==EntityStray.class);monsters.add(new Biome.SpawnListEntry(EntitySkeleton.class,20,4,4));ganymedes01.etfuturum.configuration.SpawnSettings.add(monsters,EntityStray.class,80,4,4);}
   if(Features.enabled("tweaks.shulkersSpawnAnywhere")&&BiomeDictionary.hasType(biome,BiomeDictionary.Type.END)&&monsters.stream().noneMatch(entry->entry.entityClass==EntityShulker.class))ganymedes01.etfuturum.configuration.SpawnSettings.add(monsters,EntityShulker.class,1,1,2);
  }
 }
 @SubscribeEvent public static void spawn(LivingSpawnEvent.SpecialSpawn event){
  if(!(event.getEntityLiving() instanceof EntityShulker)||!Features.enabled("tweaks.shulkersSpawnAnywhere"))return;
  EntityShulker shulker=(EntityShulker)event.getEntityLiving();shulker.getEntityData().setBoolean(NATURAL_SHULKER,true);
  if(!Features.enabled("tweaks.spawnAnywhereShulkerColors"))return;
  BlockPos center=new BlockPos(event.getX(),event.getY(),event.getZ());
  for(EnumFacing side:EnumFacing.values()){BlockPos pos=center.offset(side);if(!event.getWorld().isBlockLoaded(pos))continue;IBlockState state=event.getWorld().getBlockState(pos);ResourceLocation id=state.getBlock().getRegistryName();if(id==null)continue;String namespace=id.getNamespace(),path=id.getPath();int meta=state.getBlock().getMetaFromState(state),color=-1;
   if(namespace.equals("hardcoreenderexpansion")&&path.equals("end_stone_terrain"))color=side==EnumFacing.DOWN?(meta==2?10:meta==1?1:14):(meta%4==1?13:-1);
   else if(namespace.equals("enderlicious")&&(path.equals("endsand")||path.equals("end_sand")))color=meta==1?15:0;
   if(color>=0){StorageRules.color(shulker,color);break;}
  }
 }
}
