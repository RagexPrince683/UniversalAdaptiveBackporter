package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.entities.EntityTurtle;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Random;

/** Metadata: low two bits are hatch stage; next two bits are egg count minus one. */
public class BlockTurtleEgg extends BaseBlock {
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
	public BlockTurtleEgg() { super(Material.dragonEgg); setNames("turtle_egg"); setHardness(.5F); setTickRandomly(true); setBlockBounds(.1875F, 0, .1875F, .75F, .4375F, .75F); setBlockSound(ganymedes01.etfuturum.client.sound.ModSounds.soundTurtleEgg); }
	@Override public boolean isOpaqueCube() { return false; }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	@Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) {
		models = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[12]; String[] counts = {"", "two_", "three_", "four_"}, cracks = {"", "slightly_cracked_", "very_cracked_"};
		for (int count = 0; count < 4; count++) for (int stage = 0; stage < 3; stage++) models[count * 3 + stage] = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, counts[count] + cracks[stage] + (count == 0 ? "turtle_egg" : "turtle_eggs")); blockIcon = models[0].particle;
	}
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta) { return models[((meta >> 2) & 3) * 3 + Math.min(2, meta & 3)]; }
	@Override public int getRenderType() { return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK; }
	@Override public boolean isSideSolid(net.minecraft.world.IBlockAccess world, int x, int y, int z, net.minecraftforge.common.util.ForgeDirection side) { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
	@Override public int quantityDropped(Random random) { return 0; }
	@Override protected boolean canSilkHarvest() { return true; }
	@Override protected ItemStack createStackedBlock(int meta) { return new ItemStack(this, 1 + (meta >> 2), 0); }
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		if (world.isRemote || world.getBlock(x, y - 1, z) != Blocks.sand) return;
		float angle = world.getCelestialAngle(1);
		if ((angle < .65F || angle > .69F) && random.nextInt(500) != 0) return;
		int meta = world.getBlockMetadata(x, y, z);
		if ((meta & 3) < 2) { world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2); world.playSoundEffect(x + .5, y, z + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.turtle.egg_crack", .7F, 1); return; }
		int count = 1 + (meta >> 2), spawned = 0;
		for (int i = 0; i < count; i++) {
			EntityTurtle turtle = new EntityTurtle(world); turtle.setLocationAndAngles(x + .3 + i * .13, y, z + .3, 0, 0); turtle.setGrowingAge(-24000); turtle.setHome(x, y, z);
			if (world.spawnEntityInWorld(turtle)) spawned++;
		}
		if (spawned > 0) world.playSoundEffect(x + .5, y, z + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.turtle.egg_hatch", .7F, 1);
		if (spawned == count) world.setBlockToAir(x, y, z); else if (spawned > 0) world.setBlockMetadataWithNotify(x, y, z, 2 | ((count - spawned - 1) << 2), 2);
	}
	@Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		ItemStack held = player.getHeldItem(); int meta = world.getBlockMetadata(x, y, z);
		if (held == null || held.getItem() != net.minecraft.item.Item.getItemFromBlock(this) || (meta >> 2) >= 3) return false;
		if (!world.isRemote) { world.setBlockMetadataWithNotify(x, y, z, meta + 4, 2); if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); }
		return true;
	}
	private void trample(World world, int x, int y, int z, Entity entity, int chance) {
		if (world.isRemote || !(entity instanceof EntityLivingBase) || entity instanceof EntityTurtle || entity.isSneaking() || world.rand.nextInt(chance) != 0) return;
		if (!(entity instanceof EntityPlayer) && !world.getGameRules().getGameRuleBooleanValue("mobGriefing")) return;
		int meta = world.getBlockMetadata(x, y, z); if ((meta >> 2) == 0) world.setBlockToAir(x, y, z); else world.setBlockMetadataWithNotify(x, y, z, meta - 4, 2);
		world.playSoundEffect(x + .5, y, z + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.turtle.egg_break", .7F, 1);
	}
	@Override public void onEntityWalking(World world, int x, int y, int z, Entity entity) { trample(world, x, y, z, entity, 100); }
	@Override public void onFallenUpon(World world, int x, int y, int z, Entity entity, float distance) { trample(world, x, y, z, entity, 3); }
}
