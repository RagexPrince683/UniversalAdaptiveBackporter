package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.*;
import ganymedes01.etfuturum.api.tags.helpers.BlockTags;
import ganymedes01.etfuturum.entities.EntityBee;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import java.util.*;

/** The hive owns stored bees until a release has actually joined the world. */
public class TileEntityBeeHive extends TileEntity implements ITickable {
    private static final Map<World,Set<BlockPos>> LOADED=new WeakHashMap<>();
    private final List<Bee> bees=new ArrayList<>();
    private BlockPos flowerPos;
    private int honeyLevel;
    public enum State { HONEY_DELIVERED,BEE_RELEASED,EMERGENCY }
    private static final class Bee {
        final NBTTagCompound entityData; int ticks; final int minimum;
        Bee(NBTTagCompound data,int ticks,int minimum){entityData=data.copy();entityData.removeTag("UUIDMost");entityData.removeTag("UUIDLeast");this.ticks=Math.max(0,ticks);this.minimum=Math.max(0,minimum);}
    }
    @Override public void onLoad(){super.onLoad();if(!world.isRemote)LOADED.computeIfAbsent(world,key->new HashSet<>()).add(pos.toImmutable());}
    private void forget(){Set<BlockPos> positions=LOADED.get(world);if(positions!=null){positions.remove(pos);if(positions.isEmpty())LOADED.remove(world);}}
    @Override public void invalidate(){forget();super.invalidate();}
    @Override public void onChunkUnload(){forget();super.onChunkUnload();}
    public static List<BlockPos> nearby(World world,BlockPos center,int radius){List<BlockPos> result=new ArrayList<>();Set<BlockPos> positions=LOADED.get(world);if(positions!=null)for(BlockPos pos:positions)if(Math.abs(pos.getX()-center.getX())<=radius&&Math.abs(pos.getY()-center.getY())<=radius&&Math.abs(pos.getZ()-center.getZ())<=radius&&world.isBlockLoaded(pos))result.add(pos);result.sort(Comparator.comparingDouble(center::distanceSq));return result;}
    public int maxHiveSize(){return 3;}
    public int getBeeCount(){return bees.size();}
    public boolean isFullOfBees(){return bees.size()>=maxHiveSize();}
    public boolean hasNoBees(){return bees.isEmpty();}
    public int getHoneyLevel(){return honeyLevel;}
    public void setHoneyLevel(int value){honeyLevel=MathHelper.clamp(value,0,5);if(world!=null&&!world.isRemote){BeeHiveBlock.updateHiveState(world,pos,honeyLevel==5);world.updateComparatorOutputLevel(pos,getBlockType());world.notifyBlockUpdate(pos,world.getBlockState(pos),world.getBlockState(pos),2);}markDirty();}
    public boolean isNearFire(){if(world!=null)for(BlockPos candidate:BlockPos.getAllInBox(pos.add(-1,-1,-1),pos.add(1,1,1)))if(world.isBlockLoaded(candidate)&&world.getBlockState(candidate).getMaterial()==Material.FIRE)return true;return false;}
    public boolean isSmoked(){return isLitCampfireBelow(world,pos.getX(),pos.getY(),pos.getZ(),5);}
    public static boolean isLitCampfireBelow(World world,int x,int y,int z,int spacing){if(world==null)return false;boolean fallback=BlockTags.getInTag(UAB.ID+":bee_hive_fumigator").isEmpty();for(int i=1;i<=spacing;i++){BlockPos pos=new BlockPos(x,y-i,z);if(!world.isBlockLoaded(pos))return false;IBlockState state=world.getBlockState(pos);if(state.isOpaqueCube())break;if(BlockTags.hasTag(state.getBlock(),state.getBlock().getMetaFromState(state),UAB.ID+":bee_hive_fumigator")||fallback&&state.getMaterial()==Material.FIRE)return true;}return false;}
    @Deprecated public static void addSmokeBlock(Block block){addSmokeBlock(block,OreDictionary.WILDCARD_VALUE);}
    @Deprecated public static void addSmokeBlock(Block block,int meta){BlockTags.addTags(block,meta,UAB.ID+":bee_hive_fumigator");}
    public void tryEnterHive(Entity entity,boolean nectar){tryEnterHive(entity,nectar,0);}
    public void tryEnterHive(Entity entity,boolean nectar,int ticks){if(world==null||world.isRemote||isFullOfBees()||!(entity instanceof EntityBee)||entity.isDead)return;EntityBee bee=(EntityBee)entity;bee.setHivePos(pos);bee.removePassengers();bee.dismountRidingEntity();Entity leash=bee.getLeashHolder();bee.clearLeashed(true,!(leash instanceof EntityPlayer)||!((EntityPlayer)leash).capabilities.isCreativeMode);NBTTagCompound data=bee.serializeNBT();data.removeTag("Passengers");data.removeTag("Riding");bees.add(new Bee(data,ticks,nectar?2400:600));if(bee.hasFlower()&&(flowerPos==null||world.rand.nextBoolean()))flowerPos=bee.getFlowerPos();if(bee.addedToChunk)sound("enter");bee.setDead();markDirty();}
    public void angerBees(EntityPlayer player,State state){Iterator<Bee> it=bees.iterator();while(it.hasNext()){Bee stored=it.next();EntityBee bee=release(stored,state);if(bee!=null){it.remove();if(player!=null&&bee.getDistanceSq(player)<=16){if(isSmoked())bee.setStayOutOfHiveCountdown(400);else bee.setBeeAttacker(player);}}}markDirty();}
    private EntityBee release(Bee stored,State release){if(world==null||world.isRemote||release!=State.EMERGENCY&&(!world.isDaytime()||world.isRaining()))return null;IBlockState state=world.getBlockState(pos);if(!(state.getBlock() instanceof BeeHiveBlock))return null;EnumFacing facing=state.getValue(BeeHiveBlock.FACING);BlockPos front=pos.offset(facing);if(!world.isBlockLoaded(front))return null;IBlockState obstacle=world.getBlockState(front);boolean blocked=obstacle.isOpaqueCube()||obstacle.getMaterial().isLiquid();if(blocked&&release!=State.EMERGENCY)return null;Entity entity=EntityList.createEntityFromNBT(stored.entityData.copy(),world);if(!(entity instanceof EntityBee))return null;EntityBee bee=(EntityBee)entity;bee.setHivePos(pos);double offset=blocked?0:.55+bee.width/2;bee.setLocationAndAngles(pos.getX()+.5+offset*facing.getXOffset(),pos.getY()+.5-bee.height/2,pos.getZ()+.5+offset*facing.getZOffset(),bee.rotationYaw,bee.rotationPitch);if(flowerPos!=null&&!bee.hasFlower()&&world.rand.nextFloat()<.9F)bee.setFlowerPos(flowerPos);if(release==State.HONEY_DELIVERED)bee.onHoneyDelivered();bee.resetTicksWithoutNectar();if(!world.spawnEntity(bee))return null;if(release==State.HONEY_DELIVERED&&honeyLevel<5)setHoneyLevel(honeyLevel+(world.rand.nextInt(100)==0?2:1));sound("exit");return bee;}
    private void sound(String event){world.playSound(null,pos,ModernSounds.get("block.beehive."+event),SoundCategory.BLOCKS,1,1);}
    @Override public void update(){if(world.isRemote)return;if(isNearFire())angerBees(null,State.EMERGENCY);Iterator<Bee> it=bees.iterator();boolean changed=false;while(it.hasNext()){Bee bee=it.next();if(bee.ticks>bee.minimum){if(release(bee,bee.entityData.getBoolean("HasNectar")?State.HONEY_DELIVERED:State.BEE_RELEASED)!=null){it.remove();changed=true;}}else{bee.ticks++;changed=true;}}if(changed)markDirty();if(!bees.isEmpty()&&world.rand.nextDouble()<.005)sound("work");}
    public void destroyAllBees(){bees.clear();markDirty();}
    public NBTTagList getBees(){NBTTagList list=new NBTTagList();for(Bee bee:bees){NBTTagCompound entry=new NBTTagCompound();entry.setTag("EntityData",bee.entityData.copy());entry.setInteger("TicksInHive",bee.ticks);entry.setInteger("MinOccupationTicks",bee.minimum);list.appendTag(entry);}return list;}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);bees.clear();NBTTagList list=tag.getTagList("Bees",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound entry=list.getCompoundTagAt(i);bees.add(new Bee(entry.getCompoundTag("EntityData"),entry.getInteger("TicksInHive"),entry.getInteger("MinOccupationTicks")));}flowerPos=tag.hasKey("FlowerPos",10)?NBTUtil.getPosFromTag(tag.getCompoundTag("FlowerPos")):null;honeyLevel=MathHelper.clamp(tag.getInteger("honeyLevel"),0,5);}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setTag("Bees",getBees());tag.setInteger("honeyLevel",honeyLevel);if(flowerPos!=null)tag.setTag("FlowerPos",NBTUtil.createPosTag(flowerPos));return tag;}
    @Override public NBTTagCompound getUpdateTag(){NBTTagCompound tag=super.writeToNBT(new NBTTagCompound());tag.setInteger("honeyLevel",honeyLevel);return tag;}
    @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,0,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager network,SPacketUpdateTileEntity packet){honeyLevel=MathHelper.clamp(packet.getNbtCompound().getInteger("honeyLevel"),0,5);}
    @Override public boolean shouldRefresh(World world,BlockPos pos,IBlockState old,IBlockState state){return old.getBlock()!=state.getBlock();}
}
