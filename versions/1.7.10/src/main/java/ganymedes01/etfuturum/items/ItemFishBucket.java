package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import cpw.mods.fml.common.eventhandler.Event;

/** Mob buckets can release into existing water as well as place new water. */
public class ItemFishBucket extends ItemBucket {
    private final int type;
    public ItemFishBucket(int type, String name) {
        super(Blocks.flowing_water); this.type = type; setMaxStackSize(1); setContainerItem(Items.bucket);
        setUnlocalizedName("etfuturum." + name); setTextureName(name); setCreativeTab(EtFuturum.creativeTabItems);
    }
    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        // Filled vanilla buckets ignore water in their ray trace. Mob buckets must target it.
        MovingObjectPosition hit = getMovingObjectPositionFromPlayer(world, player, true);
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return stack;
        FillBucketEvent event = new FillBucketEvent(player, stack, world, hit);
        if (MinecraftForge.EVENT_BUS.post(event) || event.getResult() == Event.Result.DENY) return stack;
        if (event.getResult() == Event.Result.ALLOW) {
            if (player.capabilities.isCreativeMode) return stack;
            if (--stack.stackSize <= 0) return event.result;
            if (!player.inventory.addItemStackToInventory(event.result)) player.dropPlayerItemWithRandomChoice(event.result, false);
            return stack;
        }
        int x = hit.blockX, y = hit.blockY, z = hit.blockZ;
        if (!world.canMineBlock(player, x, y, z)) return stack;
        if (world.getBlock(x, y, z).getMaterial() != Material.water) {
            net.minecraftforge.common.util.ForgeDirection side = net.minecraftforge.common.util.ForgeDirection.getOrientation(hit.sideHit);
            x += side.offsetX; y += side.offsetY; z += side.offsetZ;
        }
        if (!player.canPlayerEdit(x, y, z, hit.sideHit, stack)) return stack;
        return emptyInto(stack, world, x, y, z) && !player.capabilities.isCreativeMode ? new ItemStack(Items.bucket) : stack;
    }
    public boolean emptyInto(ItemStack stack, World world, int x, int y, int z) {
        if (!world.blockExists(x, y, z) || !ganymedes01.etfuturum.configuration.ContentAvailability.available(this)) return false;
        EntityLiving animal = switch (type) { case 1 -> new EntitySalmon(world); case 2 -> new EntityTropicalFish(world); case 3 -> new EntityPufferfish(world); case 4 -> new EntityAxolotl(world); case 5 -> new EntityTadpole(world); default -> new EntityCod(world); };
        if (!ganymedes01.etfuturum.configuration.ContentAvailability.mobEnabled(animal.getClass())) return false;
        if (world.getBlock(x, y, z).getMaterial() != Material.water && !super.tryPlaceContainedLiquid(world, x, y, z)) return false;
        if (world.isRemote || world.provider.isHellWorld) return true;
        animal.setLocationAndAngles(x + .5, y + .2, z + .5, 0, 0);
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        if (animal instanceof EntityFish) ((EntityFish) animal).readBucket(tag);
        else if (animal instanceof EntityAxolotl) ((EntityAxolotl) animal).readBucket(tag);
        else ((EntityTadpole) animal).readBucket(tag);
        if (stack.hasDisplayName()) animal.setCustomNameTag(stack.getDisplayName());
        return world.spawnEntityInWorld(animal);
    }
    public void registerDispenser() {
        net.minecraft.block.BlockDispenser.dispenseBehaviorRegistry.putObject(this, new net.minecraft.dispenser.BehaviorDefaultDispenseItem() {
            @Override protected ItemStack dispenseStack(net.minecraft.dispenser.IBlockSource source, ItemStack stack) {
                net.minecraftforge.common.util.ForgeDirection side = net.minecraftforge.common.util.ForgeDirection.getOrientation(source.getBlockMetadata() & 7);
                if (emptyInto(stack, source.getWorld(), source.getXInt() + side.offsetX, source.getYInt() + side.offsetY, source.getZInt() + side.offsetZ)) return new ItemStack(Items.bucket);
                return super.dispenseStack(source, stack);
            }
        });
    }
}
