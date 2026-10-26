package ganymedes01.etfuturum.client.gui.inventory;
import ganymedes01.etfuturum.inventory.ContainerLlama;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
public class GuiLlama extends GuiContainer {
	private final ContainerLlama container;
	public GuiLlama(ContainerLlama container) { super(container); this.container = container; ySize = 168; }
	@Override protected void drawGuiContainerForegroundLayer(int x, int y) { fontRendererObj.drawString(container.llama.getInventoryName(), 8, 6, 4210752); }
	@Override protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) { GL11.glColor4f(1, 1, 1, 1); mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/generic_54.png")); drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, 71); drawTexturedModalRect(guiLeft, guiTop + 71, 0, 126, xSize, 96); }
}
