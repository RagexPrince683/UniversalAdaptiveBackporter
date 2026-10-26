package ganymedes01.etfuturum.client.renderer.tileentity;
import cpw.mods.fml.client.registry.ClientRegistry;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.client.model.ModelModernMob;
import ganymedes01.etfuturum.tileentities.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.*;
import org.lwjgl.opengl.GL11;
public class CopperMobBlockRenderer extends TileEntitySpecialRenderer implements IItemRenderer {
	private final boolean statue;
	private final ModelChest chest = new ModelChest();
	private final ModelModernMob[] poses;
	public CopperMobBlockRenderer(boolean statue) { this.statue = statue; poses = statue ? new ModelModernMob[]{new ModelModernMob("copper_golem"), new ModelModernMob("copper_statue_running"), new ModelModernMob("copper_statue_sitting"), new ModelModernMob("copper_statue_star")} : null; }
	@Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partial) { int meta = tile.hasWorldObj() ? tile.getBlockMetadata() : 0; float lid = tile instanceof TileEntityCopperChest copper ? copper.prevLidAngle + (copper.lidAngle - copper.prevLidAngle) * partial : 0; draw(x, y, z, meta, lid); }
	private void draw(double x, double y, double z, int meta, float lid) {
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT); GL11.glPushMatrix();
		try {
		GL11.glTranslated(x + .5, y + (statue ? 1.5 : 1), z + .5); GL11.glScalef(1, -1, -1); GL11.glColor4f(1, 1, 1, 1);
		if (statue) { Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("etfuturum:textures/entity/copper_golem/oxidized_copper_golem.png")); poses[meta & 3].render(null, 0, 0, 0, 0, 0, .0625F); }
		else { Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("etfuturum:textures/entity/chest/copper.png")); GL11.glRotatef((meta & 3) * 90, 0, 1, 0); GL11.glTranslatef(-.5F, 0, -.5F); lid = 1 - lid; chest.chestLid.rotateAngleX = -(1 - lid * lid * lid) * (float) Math.PI / 2; chest.renderAll(); }
		} finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
	}
	@Override public boolean handleRenderType(ItemStack stack, ItemRenderType type) { return true; }
	@Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) { return true; }
	@Override public void renderItem(ItemRenderType type, ItemStack stack, Object... data) { draw(0, 0, 0, stack.getItemDamage(), 0); }
	public static void register() { CopperMobBlockRenderer chest = new CopperMobBlockRenderer(false), statue = new CopperMobBlockRenderer(true); ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCopperChest.class, chest); ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCopperStatue.class, statue); MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.COPPER_CHEST.get()), chest); MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.COPPER_GOLEM_STATUE.get()), statue); }
}
