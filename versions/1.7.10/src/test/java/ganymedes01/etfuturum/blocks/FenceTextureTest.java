package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.blocks.compat.BlockExternalWoodFence;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IIcon;

/** Exercises the actual fence registration methods without creating an OpenGL context. */
public final class FenceTextureTest {
	private static int assertions;

	private static void check(boolean condition, String message) {
		assertions++;
		if (!condition) throw new AssertionError(message);
	}

	public static void main(String[] ignored) {
		RecordingRegister register = new RecordingRegister();
		for (String texture : new String[]{"planks_oak", "nether_brick"}) {
			BlockFence vanilla = new BlockFence(texture, "planks_oak".equals(texture) ? Material.wood : Material.rock);
			int calls = register.calls;
			vanilla.registerBlockIcons(register);
			check(register.calls == calls + 1 && texture.equals(register.name), "vanilla registers its constructor texture");
			check(vanilla.getIcon(0, 0) == register.icon, "vanilla retains the registered sprite");
		}

		SourcePlanks source = new SourcePlanks();
		for (int meta = 0; meta < 16; meta++) {
			BlockFence fence = new BlockExternalWoodFence(source, meta);
			for (int reload = 0; reload < 2; reload++) {
				// Exercise both atlas iteration orders; fences must neither cache nor register source icons.
				int calls = register.calls;
				fence.registerBlockIcons(register);
				check(register.calls == calls, "external fence leaves registration to its source");
				source.registerBlockIcons(register);
				for (int side = 0; side < 6; side++) {
					check(fence.getIcon(side, 0) == source.getIcon(side, meta), "source side and metadata preserved");
				}
				calls = register.calls;
				fence.registerBlockIcons(register);
				check(register.calls == calls, "source-first registration also remains delegated");
			}
		}
		System.out.println("Fence texture tests passed: " + assertions + " assertions");
	}

	private static final class RecordingRegister implements IIconRegister {
		int calls;
		String name;
		IIcon icon;

		@Override
		public IIcon registerIcon(String name) {
			check(name != null && name.matches("(?:[a-z0-9_.-]+:)?[a-z0-9_./-]+"), "valid texture identifier: " + name);
			calls++;
			this.name = name;
			return icon = new TextureAtlasSprite(name) {};
		}
	}

	/** A source with distinct side/metadata sprites, replaced on every registration. */
	private static final class SourcePlanks extends Block {
		private final IIcon[][] icons = new IIcon[6][16];

		SourcePlanks() { super(Material.wood); }

		@Override
		public void registerBlockIcons(IIconRegister register) {
			for (int side = 0; side < 6; side++) {
				for (int meta = 0; meta < 16; meta++) {
					icons[side][meta] = register.registerIcon("test:planks_" + side + "_" + meta);
				}
			}
		}

		@Override
		public IIcon getIcon(int side, int meta) { return icons[side][meta]; }
	}
}
