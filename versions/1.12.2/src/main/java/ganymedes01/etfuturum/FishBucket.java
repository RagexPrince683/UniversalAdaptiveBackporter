package ganymedes01.etfuturum;

import ganymedes01.etfuturum.entities.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

/** Native liquid placement and bucket events, with direct release into source water. */
final class FishBucket extends ItemBucket {
    private final int kind;
    private final ThreadLocal<ItemStack> placement = new ThreadLocal<>();
    FishBucket(int kind) { super(Blocks.FLOWING_WATER); this.kind=kind; setMaxStackSize(1); setContainerItem(Items.BUCKET); }
    @Override public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held=player.getHeldItem(hand);
        if (!enabled()) return new ActionResult<>(EnumActionResult.FAIL,held);
        RayTraceResult hit=rayTrace(world,player,true);
        if (hit!=null && hit.typeOfHit==RayTraceResult.Type.BLOCK && world.getBlockState(hit.getBlockPos()).getMaterial()==Material.WATER) {
            ActionResult<ItemStack> event=net.minecraftforge.event.ForgeEventFactory.onBucketUse(player,world,held,hit);
            if (event!=null) return event;
            BlockPos pos=hit.getBlockPos();
            if (!world.isBlockModifiable(player,pos) || !player.canPlayerEdit(pos,hit.sideHit,held) || !emptyInto(held,world,pos)) return new ActionResult<>(EnumActionResult.FAIL,held);
            player.addStat(net.minecraft.stats.StatList.getObjectUseStats(this));
            return new ActionResult<>(EnumActionResult.SUCCESS,player.capabilities.isCreativeMode?held:new ItemStack(Items.BUCKET));
        }
        placement.set(held.copy());
        try { return super.onItemRightClick(world,player,hand); }
        finally { placement.remove(); }
    }
    private EntityLiving animal(World world) { return kind==5?new EntityTadpole(world):kind==4?new EntityAxolotl(world):kind==3?new EntityPufferfish(world):kind==2?new EntityTropicalFish(world):kind==1?new EntitySalmon(world):new EntityCod(world); }
    private boolean enabled() {
        Class<?> species=kind==5?EntityTadpole.class:kind==4?EntityAxolotl.class:kind==3?EntityPufferfish.class:kind==2?EntityTropicalFish.class:kind==1?EntitySalmon.class:EntityCod.class;
        return MobRegistry.enabled(species) && Content.available(getRegistryName().getPath());
    }
    boolean emptyInto(ItemStack stack,World world,BlockPos pos) {
        if (!enabled() || !world.isBlockLoaded(pos)) return false;
        if (world.getBlockState(pos).getMaterial()!=Material.WATER && !super.tryPlaceContainedLiquid(null,world,pos)) return false;
        if (world.isRemote || world.provider.doesWaterVaporize()) return true;
        EntityLiving animal=animal(world);
        NBTTagCompound tag=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();
        if (animal instanceof EntityFish) ((EntityFish)animal).readBucket(tag);
        else if (animal instanceof EntityAxolotl) ((EntityAxolotl)animal).readBucket(tag);
        else ((EntityTadpole)animal).readBucket(tag);
        if (stack.hasDisplayName()) animal.setCustomNameTag(stack.getDisplayName());
        animal.setPosition(pos.getX()+.5,pos.getY()+.2,pos.getZ()+.5);
        return world.spawnEntity(animal);
    }
    @Override public boolean tryPlaceContainedLiquid(EntityPlayer player,World world,BlockPos pos) {
        ItemStack stack=placement.get();
        return stack==null?super.tryPlaceContainedLiquid(player,world,pos):emptyInto(stack,world,pos);
    }
    static void registerDispensers() {
        for (Item item:Content.ITEMS.values()) if (item instanceof FishBucket) {
            FishBucket bucket=(FishBucket)item;
            net.minecraft.block.BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item,new net.minecraft.dispenser.BehaviorDefaultDispenseItem() {
                @Override protected ItemStack dispenseStack(net.minecraft.dispenser.IBlockSource source,ItemStack stack) {
                    EnumFacing side=source.getBlockState().getValue(net.minecraft.block.BlockDispenser.FACING);
                    return bucket.emptyInto(stack,source.getWorld(),source.getBlockPos().offset(side))?new ItemStack(Items.BUCKET):super.dispenseStack(source,stack);
                }
            });
        }
    }
}
