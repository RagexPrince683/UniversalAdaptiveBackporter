package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import ganymedes01.etfuturum.items.ItemNewBoat;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
/** Native boat physics, paddle packets, movement validation and passenger lists remain authoritative. */
public class EntityNewBoat extends EntityBoat {
 private static final DataParameter<String> WOOD=EntityDataManager.createKey(EntityNewBoat.class,DataSerializers.STRING);
 public EntityNewBoat(World world){super(world);}
 public EntityNewBoat(World world,double x,double y,double z){this(world);setPosition(x,y,z);}
 @Override protected void entityInit(){super.entityInit();dataManager.register(WOOD,"minecraft:oak");}
 public void setBoatType(String domain,String wood){String key=domain+":"+wood;if(!ItemNewBoat.BOAT_INFO.containsKey(key))key="minecraft:oak";dataManager.set(WOOD,key);super.setBoatType(EntityBoat.Type.getTypeFromString(key.substring(key.indexOf(':')+1)));}
 public String getWoodName(){String id=dataManager.get(WOOD);return id.substring(id.indexOf(':')+1);}
 public String getWoodDomain(){String id=dataManager.get(WOOD);return id.substring(0,id.indexOf(':'));}
 public boolean isRaft(){ItemNewBoat.BoatInfo info=ItemNewBoat.BOAT_INFO.get(dataManager.get(WOOD));return info!=null&&info.isRaft();}
 public ItemStack getBoatDrop(){ItemNewBoat.BoatInfo info=ItemNewBoat.BOAT_INFO.get(dataManager.get(WOOD));return info==null?new ItemStack(net.minecraft.init.Items.BOAT):info.getBoatItem();}
 @Override public Item getItemBoat(){return getBoatDrop().getItem();}
 @Override protected void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setString("Type",getWoodName());tag.setString("Domain",getWoodDomain());}
 @Override protected void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setBoatType(tag.hasKey("Domain",8)?tag.getString("Domain"):"minecraft",tag.hasKey("Type",8)?tag.getString("Type"):"oak");}
 @Override protected boolean canFitPassenger(Entity entity){return super.canFitPassenger(entity)&&getPassengers().size()<(this instanceof EntityNewBoatWithChest||!BoatSettings.secondSeat?1:2)&&BoatSettings.allowed(entity);}
 @Override public net.minecraft.entity.item.EntityItem entityDropItem(ItemStack stack,float offset){if(stack.getItem()==Item.getItemFromBlock(net.minecraft.init.Blocks.PLANKS)&&stack.getCount()==3){ItemNewBoat.BoatInfo info=ItemNewBoat.BOAT_INFO.get(dataManager.get(WOOD));if(info!=null){stack=info.getPlank();stack.setCount(3);}}return super.entityDropItem(stack,offset);}

 public String getBoatName(){return hasCustomName()?getCustomNameTag():null;}
 public void setBoatName(String name){setCustomNameTag(name==null?"":name);}
 public ResourceLocation getResourceLocation(){return new ResourceLocation(getWoodDomain().equals("minecraft")?UAB.ID:getWoodDomain(),"textures/entity/boat/"+getWoodName()+".png");}
}
