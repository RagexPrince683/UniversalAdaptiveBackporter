package ganymedes01.etfuturum;

import java.util.*;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Successful native world additions/removals own the index; only natural spawn admission is capped. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class AquaticPopulation {
    public static final String[] NAMES={"cod","salmon","tropical_fish","pufferfish","dolphin","axolotl","glow_squid","nautilus","drowned","guardian","squid"};
    private static final int[] SCHOOLS={6,5,8,3,5,3,4,2,2,4,4},MAXIMUMS=new int[NAMES.length];
    private static final Map<World,AquaticPopulation> WORLDS=new IdentityHashMap<>();
    private static int sharedAnimals,sharedHostiles,localMaximum,fishMaximum;
    private static boolean squidLimit;
    private final World world;
    private final Listener listener=new Listener();
    private final IdentityHashMap<EntityLiving,Boolean> loaded=new IdentityHashMap<>();
    private final int[] counts=new int[NAMES.length];
    private final long[] attempts=new long[NAMES.length],successes=new long[NAMES.length],habitat=new long[NAMES.length],global=new long[NAMES.length],local=new long[NAMES.length],failed=new long[NAMES.length];
    private int eligibleChunks=256;
    private long preparedTick=Long.MIN_VALUE;
    private AquaticPopulation(World world){this.world=world;world.addEventListener(listener);for(Entity entity:world.loadedEntityList)add(entity);}
    static void initialize(){
        UabConfiguration cfg=UabConfiguration.get();sharedAnimals=cfg.integer("entities","aquaticPopulation",48,1,1024);sharedHostiles=cfg.integer("entities","hostileAquaticPopulation",80,1,1024);localMaximum=cfg.integer("entities","aquaticLocalPopulation",24,1,256);fishMaximum=cfg.integer("entities","fishPopulation",20,0,1000);squidLimit=Features.enabled("entities.limitVanillaSquid");
        String[] keys={"codPopulation","salmonPopulation","tropicalFishPopulation","pufferfishPopulation","dolphinPopulation","axolotlPopulation","glowSquidPopulation","nautilusPopulation","drownedPopulation","guardianPopulation","squidPopulation"};int[] defaults={20,20,20,5,5,5,5,5,15,70,5};
        for(int i=0;i<keys.length;i++)MAXIMUMS[i]=i==7&&!MobRegistry.implemented("nautilus")?defaults[i]:cfg.integer("entities",keys[i],defaults[i],0,1000);
    }
    public static AquaticPopulation get(World world){AquaticPopulation index=WORLDS.get(world);if(index==null){index=new AquaticPopulation(world);WORLDS.put(world,index);}return index;}
    @SubscribeEvent public static void load(WorldEvent.Load event){if(!event.getWorld().isRemote)get(event.getWorld());}
    @SubscribeEvent public static void unload(WorldEvent.Unload event){AquaticPopulation index=WORLDS.remove(event.getWorld());if(index!=null){index.world.removeEventListener(index.listener);index.loaded.clear();}}
    public static int species(Entity entity){if(entity instanceof EntityFish)return ((EntityFish)entity).fishType();if(entity instanceof EntityDolphin)return 4;if(entity instanceof EntityAxolotl)return 5;if(entity instanceof EntityGlowSquid)return 6;if(entity instanceof EntityNautilus)return 7;if(entity instanceof EntityDrowned)return 8;if(entity instanceof EntityGuardian)return 9;return entity.getClass()==EntitySquid.class?10:-1;}
    private static boolean counted(EntityLiving entity){return !entity.isDead&&!entity.isNoDespawnRequired()&&!entity.hasCustomName()&&!(entity instanceof EntityElderGuardian);}
    private void add(Entity entity){if(!(entity instanceof EntityLiving))return;int kind=species(entity);if(kind<0)return;EntityLiving living=(EntityLiving)entity;if(!loaded.containsKey(living)){boolean count=counted(living);loaded.put(living,count);if(count)counts[kind]++;}}
    private void remove(Entity entity){if(entity instanceof EntityLiving&&Boolean.TRUE.equals(loaded.remove(entity)))counts[species(entity)]--;}
    @SubscribeEvent public static void update(LivingEvent.LivingUpdateEvent event){EntityLivingBase entity=event.getEntityLiving();if(entity.world.isRemote||!(entity instanceof EntityLiving)||species(entity)<0)return;AquaticPopulation index=WORLDS.get(entity.world);if(index==null)return;EntityLiving living=(EntityLiving)entity;Boolean old=index.loaded.get(living);if(old!=null){boolean count=counted(living);if(old.booleanValue()!=count){index.loaded.put(living,count);index.counts[species(entity)]+=count?1:-1;}}}
    public void prepare(Set<ChunkPos> eligible){if(preparedTick==world.getTotalWorldTime())return;preparedTick=world.getTotalWorldTime();eligibleChunks=0;for(ChunkPos pos:eligible)if(world.isBlockLoaded(new BlockPos(pos.x<<4,0,pos.z<<4)))eligibleChunks++;eligibleChunks=Math.max(1,eligibleChunks);}
    public static boolean indexed(EnumCreatureType type){return type!=null&&(type==MobRegistry.FISH||type==MobRegistry.DOLPHINS||type==MobRegistry.AXOLOTLS||type==MobRegistry.GLOW_SQUID||type==MobRegistry.DROWNED||type==MobRegistry.NAUTILUS);}
    public int count(EnumCreatureType type){return type==MobRegistry.FISH?counts[0]+counts[1]+counts[2]+counts[3]:type==MobRegistry.DOLPHINS?counts[4]:type==MobRegistry.AXOLOTLS?counts[5]:type==MobRegistry.GLOW_SQUID?counts[6]:type==MobRegistry.DROWNED?counts[8]:type==MobRegistry.NAUTILUS?counts[7]:0;}
    public int count(int species){return counts[species];}
    private int scaled(int maximum,int minimum){return Math.max(Math.min(maximum,minimum),(int)Math.min(Integer.MAX_VALUE,(long)maximum*eligibleChunks/256));}
    private int shared(boolean hostile){return hostile?counts[8]+counts[9]:counts[0]+counts[1]+counts[2]+counts[3]+counts[4]+counts[5]+counts[6]+counts[7];}
    public int nativeCount(EnumCreatureType type){
        int count=count(type),minimum=type==MobRegistry.FISH?8:type==MobRegistry.DOLPHINS?5:type==MobRegistry.AXOLOTLS?3:type==MobRegistry.GLOW_SQUID?4:2;boolean hostile=type==MobRegistry.DROWNED;
        if(count>=scaled(type.getMaxNumberOfCreature(),minimum)||shared(hostile)>=scaled(hostile?sharedHostiles:sharedAnimals,hostile?4:12)){
            // At most 289 native candidate chunks per player. Close this full category without LVT hooks.
            return Math.max(count,type.getMaxNumberOfCreature()*Math.max(1,world.playerEntities.size())+1);
        }return count;
    }
    public static boolean managed(Entity entity){int kind=species(entity);return kind>=0&&(kind!=10||squidLimit);}
    public boolean admit(EntityLiving entity){
        int kind=species(entity);attempts[kind]++;AxisAlignedBB box=entity.getEntityBoundingBox();
        for(int cx=MathHelper.floor(box.minX)>>4;cx<=MathHelper.floor(box.maxX)>>4;cx++)for(int cz=MathHelper.floor(box.minZ)>>4;cz<=MathHelper.floor(box.maxZ)>>4;cz++)if(!world.isBlockLoaded(new BlockPos(cx<<4,0,cz<<4))){habitat[kind]++;return false;}
        boolean hostile=kind==8||kind==9;
        if(counts[kind]>=scaled(MAXIMUMS[kind],SCHOOLS[kind])||kind<4&&count(MobRegistry.FISH)>=scaled(fishMaximum,8)||kind<10&&shared(hostile)>=scaled(hostile?sharedHostiles:sharedAnimals,hostile?4:12)){global[kind]++;return false;}
        int same=0,nearby=0;for(EntityLiving other:world.getEntitiesWithinAABB(EntityLiving.class,box.grow(24,12,24))){int species=species(other);if(species<0||!counted(other))continue;if(species==kind)same++;if(species<10&&(species==8||species==9)==hostile)nearby++;}
        if(same>=Math.min(MAXIMUMS[kind],SCHOOLS[kind]*2)||kind<10&&nearby>=localMaximum){local[kind]++;return false;}return true;
    }
    public void spawned(EntityLiving entity,boolean success){if(success)successes[species(entity)]++;else failed[species(entity)]++;}
    public List<String> report(boolean reset){List<String> result=new ArrayList<>();result.add("Loaded natural aquatic counts; eligible loaded chunks="+eligibleChunks+". Admission follows native habitat/event checks.");for(int i=0;i<NAMES.length;i++){result.add(NAMES[i]+" count="+counts[i]+" attempts="+attempts[i]+" success="+successes[i]+" unloaded="+habitat[i]+" cap="+global[i]+" density="+local[i]+" failedJoin="+failed[i]);if(reset)attempts[i]=successes[i]=habitat[i]=global[i]=local[i]=failed[i]=0;}return result;}
    private final class Listener implements IWorldEventListener {
        @Override public void onEntityAdded(Entity entity){add(entity);}
        @Override public void onEntityRemoved(Entity entity){remove(entity);}
        @Override public void notifyBlockUpdate(World world,BlockPos pos,IBlockState before,IBlockState after,int flags){ }
        @Override public void notifyLightSet(BlockPos pos){ }
        @Override public void markBlockRangeForRenderUpdate(int x1,int y1,int z1,int x2,int y2,int z2){ }
        @Override public void playSoundToAllNearExcept(EntityPlayer player,SoundEvent sound,SoundCategory category,double x,double y,double z,float volume,float pitch){ }
        @Override public void playRecord(SoundEvent sound,BlockPos pos){ }
        @Override public void spawnParticle(int id,boolean ignoreRange,double x,double y,double z,double vx,double vy,double vz,int...args){ }
        @Override public void spawnParticle(int id,boolean ignoreRange,boolean minimal,double x,double y,double z,double vx,double vy,double vz,int...args){ }
        @Override public void broadcastSound(int id,BlockPos pos,int data){ }
        @Override public void playEvent(EntityPlayer player,int id,BlockPos pos,int data){ }
        @Override public void sendBlockBreakProgress(int breaker,BlockPos pos,int progress){ }
    }
}
