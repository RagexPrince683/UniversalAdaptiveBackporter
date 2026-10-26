package ganymedes01.etfuturum.blocks;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import java.util.Random;

public class BlockEyeblossom extends BaseFlower {
	private IIcon open;
	public BlockEyeblossom() { setNames("eyeblossom"); setTickRandomly(true); }
	@Override public void registerBlockIcons(IIconRegister register) { blockIcon = register.registerIcon("closed_eyeblossom"); open = register.registerIcon("open_eyeblossom"); }
	@Override public IIcon getIcon(int side, int meta) { return (meta & 1) == 0 ? blockIcon : open; }
	@Override public int damageDropped(int meta) { return meta & 1; }
	@Override public void getSubBlocks(net.minecraft.item.Item item, net.minecraft.creativetab.CreativeTabs tab, java.util.List list) {
		list.add(new net.minecraft.item.ItemStack(item, 1, 0)); list.add(new net.minecraft.item.ItemStack(item, 1, 1));
	}
	@Override public void onEntityCollidedWithBlock(World world, int x, int y, int z, net.minecraft.entity.Entity entity) {
		if (!world.isRemote && (world.getBlockMetadata(x, y, z) & 1) != 0 && entity instanceof ganymedes01.etfuturum.entities.EntityBee) {
			ganymedes01.etfuturum.entities.EntityBee bee = (ganymedes01.etfuturum.entities.EntityBee) entity;
			if (!bee.isPotionActive(net.minecraft.potion.Potion.poison)) bee.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.poison.id, 25));
		}
	}
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		super.updateTick(world, x, y, z, random);
		if (world.getBlock(x, y, z) == this && !world.provider.hasNoSky) {
			long time = world.getWorldTime() % 24000;
			world.setBlockMetadataWithNotify(x, y, z, time >= 13000 && time < 23000 ? 1 : 0, 2);
		}
	}
}
