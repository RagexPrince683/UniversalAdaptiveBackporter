package ganymedes01.etfuturum.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import java.util.Random;

public final class BlockCoralBlock extends BaseBlock {
	public final String color;
	public final boolean dead;
	public BlockCoralBlock(String color, boolean dead) {
		super(Material.rock); this.color = color; this.dead = dead;
		setNames((dead ? "dead_" : "") + color + "_coral_block"); setHardness(1.5F); setResistance(6);
		setBlockSound(ganymedes01.etfuturum.client.sound.ModSounds.soundCoralBlock); setHarvestLevel("pickaxe", 0);
	}
	@Override public Item getItemDropped(int meta, Random random, int fortune) { return Item.getItemFromBlock(CoralContent.get(color, true, "_block", false)); }
	@Override public void onBlockAdded(World world, int x, int y, int z) { if (!dead && !world.isRemote) world.scheduleBlockUpdate(x, y, z, this, 60 + world.rand.nextInt(40)); }
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) { onBlockAdded(world, x, y, z); }
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote && !dead && !CoralContent.hasWater(world, x, y, z)) world.setBlock(x, y, z, CoralContent.get(color, true, "_block", false), 0, 3);
	}
}
