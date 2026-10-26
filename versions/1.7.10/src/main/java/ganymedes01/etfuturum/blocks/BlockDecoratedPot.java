package ganymedes01.etfuturum.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.tileentities.TileEntityDecoratedPot;
import java.util.ArrayList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Four persisted designs and one stack of storage, with lossless legacy inventory reads. */
public class BlockDecoratedPot extends BaseBlock {
	public BlockDecoratedPot() { super(Material.rock); setNames("decorated_pot"); setBlockTextureName("decorated_pot_side"); setHardness(0); setBlockBounds(.0625F, 0, .0625F, .9375F, 1, .9375F); }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public boolean canHarvestBlock(EntityPlayer player, int meta) { return true; }
	@Override public int getRenderType() { return -1; }
	@Override public boolean hasTileEntity(int meta) { return true; }
	@Override public TileEntity createTileEntity(World world, int meta) { return new TileEntityDecoratedPot(); }
	public static TileEntityDecoratedPot pot(World world, int x, int y, int z) {
		TileEntity old = world.getTileEntity(x, y, z);
		if (old instanceof TileEntityDecoratedPot) return (TileEntityDecoratedPot) old;
		if (old instanceof IInventory && !world.isRemote) {
			NBTTagCompound tag = new NBTTagCompound(); old.writeToNBT(tag);
			TileEntityDecoratedPot pot = new TileEntityDecoratedPot(); pot.readFromNBT(tag); world.setTileEntity(x, y, z, pot); pot.markDirty(); return pot;
		} return null;
	}
	@Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		world.setBlockMetadataWithNotify(x, y, z, MathHelper.floor_double(placer.rotationYaw * 4 / 360F + .5) & 3, 2);
		TileEntityDecoratedPot pot = pot(world, x, y, z); if (pot != null) pot.setDesigns(stack);
	}
	@Override public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (!player.capabilities.isCreativeMode && held != null && (held.getItem() instanceof ItemTool || held.getItem() instanceof ItemSword || held.getItem() instanceof ItemHoe || held.getItem() instanceof ItemShears) && !EnchantmentHelper.getSilkTouchModifier(player)) world.setBlockMetadataWithNotify(x, y, z, meta | 4, 2);
		super.onBlockHarvested(world, x, y, z, meta, player);
	}
	@Override public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) { return willHarvest || super.removedByPlayer(world, player, x, y, z, false); }
	@Override public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) { super.harvestBlock(world, player, x, y, z, world.getBlockMetadata(x, y, z)); world.setBlockToAir(x, y, z); }
	@Override public boolean canDropFromExplosion(net.minecraft.world.Explosion explosion) { return false; }
	@Override public void onBlockExploded(World world, int x, int y, int z, net.minecraft.world.Explosion explosion) { dropBlockAsItemWithChance(world, x, y, z, world.getBlockMetadata(x, y, z) | 4, 1, 0); super.onBlockExploded(world, x, y, z, explosion); }
	@Override public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList<>(); TileEntityDecoratedPot pot = pot(world, x, y, z);
		if ((meta & 4) == 0) drops.add(pot == null ? ModBlocks.DECORATED_POT.newItemStack() : pot.potItem());
		else for (int i = 0; i < 4; i++) { String name = pot == null ? "" : pot.designs[i]; Item item = name.isEmpty() ? Items.brick : cpw.mods.fml.common.registry.GameRegistry.findItem("etfuturum", name + "_pottery_sherd"); if (item != null) drops.add(new ItemStack(item)); }
		return drops;
	}
	@Override public void breakBlock(World world, int x, int y, int z, Block old, int meta) {
		if (!world.isRemote) { TileEntityDecoratedPot pot = pot(world, x, y, z); if (pot != null) for (ItemStack stack : pot.allContents()) world.spawnEntityInWorld(new EntityItem(world, x + .5, y + .5, z + .5, stack)); }
		world.removeTileEntity(x, y, z); super.breakBlock(world, x, y, z, old, meta);
	}
	@Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
		if (world.isRemote) return true;
		TileEntityDecoratedPot pot = pot(world, x, y, z); ItemStack held = player.getHeldItem();
		if (pot == null || held == null || !player.canPlayerEdit(x, y, z, side, held)) return true;
		ItemStack stored = pot.getStackInSlot(0);
		if (stored == null || stored.stackSize < stored.getMaxStackSize() && stored.isItemEqual(held) && ItemStack.areItemStackTagsEqual(stored, held)) {
			if (stored == null) { stored = held.copy(); stored.stackSize = 1; } else stored.stackSize++;
			pot.setInventorySlotContents(0, stored); if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			world.playSoundEffect(x + .5, y + .5, z + .5, "random.pop", .7F, .8F + .4F * stored.stackSize / stored.getMaxStackSize());
		} return true;
	}
	@Override public boolean hasComparatorInputOverride() { return true; }
	@Override public int getComparatorInputOverride(World world, int x, int y, int z, int side) { return Container.calcRedstoneFromInventory(pot(world, x, y, z)); }
}
