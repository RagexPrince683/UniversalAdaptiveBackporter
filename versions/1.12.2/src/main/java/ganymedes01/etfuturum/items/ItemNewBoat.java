package ganymedes01.etfuturum.items;
import java.util.*;
import java.util.function.Supplier;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
public class ItemNewBoat extends Item {
 public static final Map<String,BoatInfo> BOAT_INFO=new LinkedHashMap<>();
 public final String domain,wood;public final boolean chest,raft;
 public ItemNewBoat(String domain,String wood,Supplier<Item> plank,int meta,boolean chest,boolean raft){this.domain=domain;this.wood=wood;this.chest=chest;this.raft=raft;setMaxStackSize(1);BOAT_INFO.put(domain+":"+wood+(chest?"_chest":""),new BoatInfo(new ItemStack(this),plank,meta,raft));}
 @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);Vec3d eye=player.getPositionEyes(1),look=player.getLook(1);RayTraceResult hit=world.rayTraceBlocks(eye,eye.add(look.scale(5)),true);if(hit==null)return new ActionResult<>(EnumActionResult.PASS,held);for(Entity entity:world.getEntitiesWithinAABBExcludingEntity(player,player.getEntityBoundingBox().expand(look.x*5,look.y*5,look.z*5).grow(1)))if(entity.canBeCollidedWith()&&entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize()).contains(eye))return new ActionResult<>(EnumActionResult.PASS,held);if(hit.typeOfHit!=RayTraceResult.Type.BLOCK)return new ActionResult<>(EnumActionResult.PASS,held);EntityNewBoat boat=chest?new EntityNewBoatWithChest(world):new EntityNewBoat(world);boat.setBoatType(domain,wood);boat.setLocationAndAngles(hit.hitVec.x,hit.hitVec.y-(world.getBlockState(hit.getBlockPos()).getMaterial()==net.minecraft.block.material.Material.WATER?.12:0),hit.hitVec.z,player.rotationYaw,0);if(held.hasDisplayName())boat.setCustomNameTag(held.getDisplayName());if(!world.getCollisionBoxes(boat,boat.getEntityBoundingBox().grow(-.1)).isEmpty())return new ActionResult<>(EnumActionResult.FAIL,held);if(!world.isRemote){if(!world.spawnEntity(boat))return new ActionResult<>(EnumActionResult.FAIL,held);if(!player.capabilities.isCreativeMode)held.shrink(1);}return new ActionResult<>(EnumActionResult.SUCCESS,held);}
 public class BoatInfo {private final ItemStack boatItem;private final Supplier<Item> plank;private final int meta;private final boolean raft;public BoatInfo(ItemStack item,Supplier<Item> plank,int meta,boolean raft){boatItem=item;this.plank=plank;this.meta=meta;this.raft=raft;}public ItemStack getBoatItem(){return boatItem.copy();}public ItemStack getPlank(){return new ItemStack(plank.get(),1,meta);}public boolean isRaft(){return raft;}}
}
