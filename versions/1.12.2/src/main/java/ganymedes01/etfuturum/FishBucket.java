package ganymedes01.etfuturum;

import ganymedes01.etfuturum.entities.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Native bucket events, ray tracing, permissions and evaporation own liquid placement. */
final class FishBucket extends ItemBucket {
    private final int kind;
    private static final class Placement { BlockPos pos; }
    private final ThreadLocal<Placement> placement=new ThreadLocal<>();
    FishBucket(int kind){super(Blocks.FLOWING_WATER);this.kind=kind;setMaxStackSize(1);setContainerItem(Items.BUCKET);}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){
        Class<?> species=kind==4?EntityAxolotl.class:kind==3?EntityPufferfish.class:kind==2?EntityTropicalFish.class:kind==1?EntitySalmon.class:EntityCod.class;
        if(!MobRegistry.enabled(species)||!Features.enabled(kind==4?"content.axolotl_buckets":"content.fish_buckets"))return new ActionResult<>(EnumActionResult.FAIL,player.getHeldItem(hand));
        ItemStack original=player.getHeldItem(hand).copy();Placement attempt=new Placement();placement.set(attempt);
        try{
            ActionResult<ItemStack> result=super.onItemRightClick(world,player,hand);
            if(attempt.pos!=null&&!world.isRemote&&!world.provider.doesWaterVaporize()){
                EntityLiving animal=kind==4?new EntityAxolotl(world):kind==3?new EntityPufferfish(world):kind==2?new EntityTropicalFish(world):kind==1?new EntitySalmon(world):new EntityCod(world);
                NBTTagCompound tag=original.hasTagCompound()?original.getTagCompound():new NBTTagCompound();if(animal instanceof EntityFish)((EntityFish)animal).readBucket(tag);else((EntityAxolotl)animal).readBucket(tag);
                if(original.hasDisplayName())animal.setCustomNameTag(original.getDisplayName());animal.setPosition(attempt.pos.getX()+.5,attempt.pos.getY()+.2,attempt.pos.getZ()+.5);
                if(!world.spawnEntity(animal))return new ActionResult<>(EnumActionResult.FAIL,original);
            }
            return result;
        }finally{placement.remove();}
    }
    @Override public boolean tryPlaceContainedLiquid(EntityPlayer player,World world,BlockPos pos){boolean success=super.tryPlaceContainedLiquid(player,world,pos);Placement attempt=placement.get();if(success&&attempt!=null)attempt.pos=pos.toImmutable();return success;}
}
