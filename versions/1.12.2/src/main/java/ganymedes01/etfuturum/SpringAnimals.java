package ganymedes01.etfuturum;

import java.util.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.Side;

/** Additive native-animal appearance and sound data; no entity registration or AI replacement. */
public final class SpringAnimals {
    public static final String COAT="UabSpringCoat",VOICE="UabWolfVoice";
    public static final String[] WOLVES={"","ashen","black","chestnut","rusty","snowy","spotted","striped","woods"},VOICES={"","angry","big","cute","grumpy","puglin","sad"};
    private static SimpleNetworkWrapper network;
    private SpringAnimals(){}
    public static boolean enabled(){return Features.enabled("content.spring_to_life");}
    public static boolean supported(Entity entity){Class<?> type=entity.getClass();return type==EntityCow.class||type==EntityPig.class||type==EntityChicken.class||type==EntityWolf.class;}
    static void initialize(){network=NetworkRegistry.INSTANCE.newSimpleChannel("UAB_Spring");network.registerMessage(Handler.class,State.class,0,Side.CLIENT);MinecraftForge.EVENT_BUS.register(new SpringAnimals());}
    @SubscribeEvent public void join(EntityJoinWorldEvent event){Entity entity=event.getEntity();if(event.getWorld().isRemote||!enabled()||!supported(entity)||entity.getEntityData().hasKey(COAT))return;net.minecraft.world.biome.Biome biome=entity.world.getBiome(new BlockPos(entity));float temperature=biome.getTemperature(new BlockPos(entity));int coat=temperature<.3F?2:temperature>=1?1:0;
        if(entity instanceof EntityWolf)coat=BiomeDictionary.hasType(biome,BiomeDictionary.Type.SNOWY)?5:BiomeDictionary.hasType(biome,BiomeDictionary.Type.MESA)?6:BiomeDictionary.hasType(biome,BiomeDictionary.Type.SAVANNA)?7:BiomeDictionary.hasType(biome,BiomeDictionary.Type.JUNGLE)?4:BiomeDictionary.hasType(biome,BiomeDictionary.Type.CONIFEROUS)?(entity.world.rand.nextBoolean()?1:3):BiomeDictionary.hasType(biome,BiomeDictionary.Type.FOREST)?(entity.world.rand.nextBoolean()?8:2):0;
        entity.getEntityData().setInteger(COAT,coat);entity.getEntityData().setInteger(VOICE,entity instanceof EntityWolf?entity.world.rand.nextInt(7):0);
    }
    @SubscribeEvent public void tracking(PlayerEvent.StartTracking event){if(event.getEntityPlayer() instanceof EntityPlayerMP&&supported(event.getTarget()))network.sendTo(new State(event.getTarget()),(EntityPlayerMP)event.getEntityPlayer());}
    public static void loaded(Entity entity,NBTTagCompound tag){if(!enabled()||!supported(entity))return;NBTTagCompound saved=tag.getCompoundTag("ForgeData");entity.getEntityData().setInteger(COAT,saved.hasKey(COAT)?saved.getInteger(COAT):0);entity.getEntityData().setInteger(VOICE,saved.getInteger(VOICE));}
    public static void inherit(EntityAgeable parent,EntityAgeable mate,EntityAgeable child){if(!enabled()||child==null||!supported(child))return;NBTTagCompound source=(parent.world.rand.nextBoolean()?parent:mate).getEntityData();child.getEntityData().setInteger(COAT,source.getInteger(COAT));child.getEntityData().setInteger(VOICE,source.getInteger(VOICE));}
    public static void sheep(EntitySheep sheep){if(!enabled()||sheep.getClass()!=EntitySheep.class||sheep.getFleeceColor()==net.minecraft.item.EnumDyeColor.PINK)return;net.minecraft.world.biome.Biome biome=sheep.world.getBiome(new BlockPos(sheep));float temperature=biome.getTemperature(new BlockPos(sheep));net.minecraft.item.EnumDyeColor color=temperature<.3F?net.minecraft.item.EnumDyeColor.WHITE:temperature>=1?net.minecraft.item.EnumDyeColor.BROWN:net.minecraft.item.EnumDyeColor.WHITE;if(sheep.getRNG().nextInt(10)==0)color=temperature<.3F?net.minecraft.item.EnumDyeColor.BLACK:net.minecraft.item.EnumDyeColor.GRAY;sheep.setFleeceColor(color);}
    public static Item egg(EntityChicken chicken,Item original){if(!enabled())return original;int coat=chicken.getEntityData().getInteger(COAT);return coat==1?Content.item("brown_egg"):coat==2?Content.item("blue_egg"):original;}
    public static String texture(Entity entity){if(!enabled()||!ganymedes01.etfuturum.configuration.UabConfiguration.get().bool("content","spring_animal_renders",false)||!supported(entity))return null;int coat=entity.getEntityData().getInteger(COAT);String path;if(entity instanceof EntityWolf){EntityWolf wolf=(EntityWolf)entity;coat=Math.max(0,Math.min(8,coat));path="wolf/wolf"+(coat==0?"":"_"+WOLVES[coat])+(wolf.isAngry()?"_angry":wolf.isTamed()?"_tame":"");}else{String type=entity instanceof EntityCow?"cow":entity instanceof EntityPig?"pig":"chicken";path=type+"/"+(coat==1?"warm_":coat==2?"cold_":"temperate_")+type;}return "etfuturum:textures/entity/"+path+".png";}
    public static final class State implements IMessage {
        public int id,coat,voice;public UUID uuid;public State(){}State(Entity entity){id=entity.getEntityId();uuid=entity.getUniqueID();coat=entity.getEntityData().getInteger(COAT);voice=entity.getEntityData().getInteger(VOICE);}
        @Override public void fromBytes(ByteBuf data){id=data.readInt();uuid=new UUID(data.readLong(),data.readLong());coat=data.readUnsignedByte();voice=data.readUnsignedByte();}
        @Override public void toBytes(ByteBuf data){data.writeInt(id);data.writeLong(uuid.getMostSignificantBits());data.writeLong(uuid.getLeastSignificantBits());data.writeByte(coat);data.writeByte(voice);}
        public boolean apply(Entity entity){if(entity==null||!entity.getUniqueID().equals(uuid)||!supported(entity))return false;entity.getEntityData().setInteger(COAT,Math.max(0,Math.min(8,coat)));entity.getEntityData().setInteger(VOICE,Math.max(0,Math.min(6,voice)));return true;}
    }
    public static final class Handler implements IMessageHandler<State,IMessage>{@Override public IMessage onMessage(State state,MessageContext context){UAB.proxy.springState(state,context);return null;}}
}
