package ganymedes01.etfuturum.client.gui.inventory;

import ganymedes01.etfuturum.inventory.*;
import java.util.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Native workstation layouts and actual result previews; the container owns every transaction. */
public class GuiWorkstation extends GuiContainer {
    private static final Map<String, ResourceLocation> TEXTURES = new HashMap<>();
    private final ContainerSelectableWorkstation container;
    private final String kind;
    private final boolean cutter, loom, cartography;
    private final int gridX, gridY, cellWidth, cellHeight, visibleRows;
    private final ItemStack[] previousInputs;
    private List<ItemStack> previews = Collections.emptyList();
    private boolean inputsKnown, dragging;
    private int scrollRow;
    private MapData lastMap;

    public GuiWorkstation(ContainerSelectableWorkstation container) {
        super(container);
        this.container = container;
        cutter = container instanceof ContainerStonecutter;
        loom = container instanceof ContainerLoom;
        cartography = container instanceof ContainerCartography;
        kind = cutter ? "stonecutter" : loom ? "loom" : cartography ? "cartography_table" : "grindstone";
        xSize = 176; ySize = 166;
        gridX = loom ? 60 : 52; gridY = loom ? 13 : 14;
        cellWidth = loom ? 14 : 16; cellHeight = loom ? 14 : 18; visibleRows = loom ? 4 : 3;
        previousInputs = new ItemStack[container.inputCount];
    }

    private static boolean empty(ItemStack stack) { return stack == null; }
    private static boolean sameType(ItemStack a, ItemStack b) {
        return empty(a) ? empty(b) : !empty(b) && a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private void syncPreviews() {
        boolean changed = !inputsKnown;
        for (int i = 0; i < previousInputs.length; i++)
            changed |= !sameType(previousInputs[i], container.inputs.getStackInSlot(i));
        if (changed) {
            for (int i = 0; i < previousInputs.length; i++) {
                ItemStack input = container.inputs.getStackInSlot(i);
                previousInputs[i] = empty(input) ? null : input.copy();
            }
            previews = container.previews();
            inputsKnown = true; scrollRow = 0; dragging = false;
        }
        scrollRow = Math.max(0, Math.min(scrollRow, maxScroll()));
    }

    private int maxScroll() { return Math.max(0, (previews.size() + 3) / 4 - visibleRows); }
    private boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= guiLeft + x && mouseX < guiLeft + x + width
                && mouseY >= guiTop + y && mouseY < guiTop + y + height;
    }
    private int hoveredChoice(int mouseX, int mouseY) {
        if (!(cutter || loom) || !inside(mouseX, mouseY, gridX, gridY, cellWidth * 4, cellHeight * visibleRows)) return -1;
        int index = (scrollRow + (mouseY - guiTop - gridY) / cellHeight) * 4 + (mouseX - guiLeft - gridX) / cellWidth;
        return index < previews.size() ? index : -1;
    }

    @Override public void updateScreen() { super.updateScreen(); syncPreviews(); }
    @Override public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if ((cutter || loom) && wheel != 0) {
            syncPreviews();
            scrollRow = Math.max(0, Math.min(maxScroll(), scrollRow - Integer.signum(wheel)));
        }
    }
    @Override protected void mouseClicked(int mouseX, int mouseY, int button) {
        syncPreviews();
        if (button == 0 && (cutter || loom)) {
            int choice = hoveredChoice(mouseX, mouseY);
            if (choice >= 0) {
                if (container.enchantItem(mc.thePlayer, choice)) {
                    mc.playerController.sendEnchantPacket(container.windowId, choice);
                    mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1));
                }
                return;
            }
            if (maxScroll() > 0 && inside(mouseX, mouseY, 119, gridY, 12, visibleRows * cellHeight)) {
                dragging = true; dragScroll(mouseY); return;
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
    private void dragScroll(int mouseY) {
        float fraction = (mouseY - guiTop - gridY - 7.5F) / (visibleRows * cellHeight - 15F);
        scrollRow = Math.round(Math.max(0, Math.min(1, fraction)) * maxScroll());
    }
    @Override protected void mouseClickMove(int x, int y, int button, long elapsed) {
        if (dragging && button == 0) dragScroll(y); else super.mouseClickMove(x, y, button, elapsed);
    }
    @Override protected void mouseMovedOrUp(int x, int y, int button) {
        if (button == 0) dragging = false;
        super.mouseMovedOrUp(x, y, button);
    }

    private ResourceLocation texture(String path) {
        return TEXTURES.computeIfAbsent(path, name -> new ResourceLocation(ganymedes01.etfuturum.Tags.MC_ASSET_VER, "textures/gui/" + name + ".png"));
    }
    private void sprite(String name, int x, int y, int width, int height) {
        GL11.glColor4f(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(texture("sprites/container/" + name));
        func_146110_a(x, y, 0, 0, width, height, width, height);
    }
    private ItemStack result() { return container.getSlot(container.inputCount).getStack(); }

    @Override protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(I18n.format(container.title()), 8, cartography ? 4 : 6, 0x404040);
        fontRendererObj.drawString(I18n.format("container.inventory"), 8, 72, 0x404040);
    }

    @Override protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        syncPreviews();
        GL11.glColor4f(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(texture("container/" + kind));
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (cutter || loom) drawChoices(mouseX, mouseY);
        if (loom) {
            String[] slots = {"banner", "dye", "banner_pattern"};
            int[] positions = {13, 26, 33, 26, 23, 45};
            for (int i = 0; i < slots.length; i++) if (empty(container.inputs.getStackInSlot(i)))
                sprite("slot/" + slots[i], guiLeft + positions[i * 2], guiTop + positions[i * 2 + 1], 16, 16);
            if (!empty(result())) drawItem(result(), guiLeft + 135, guiTop + 11, 2);
            else if (!empty(container.inputs.getStackInSlot(0)) && !empty(container.inputs.getStackInSlot(1)))
                sprite("loom/error", guiLeft + 139, guiTop + 52, 26, 26);
        } else if (cartography) {
            drawMapPreview();
            if (!empty(container.inputs.getStackInSlot(0)) && !empty(container.inputs.getStackInSlot(1)) && empty(result()))
                sprite("cartography_table/error", guiLeft + 35, guiTop + 31, 28, 21);
        } else if (!cutter && empty(result()) && (!empty(container.inputs.getStackInSlot(0)) || !empty(container.inputs.getStackInSlot(1))))
            sprite("grindstone/error", guiLeft + 92, guiTop + 31, 28, 21);
    }

    private void drawChoices(int mouseX, int mouseY) {
        int hovered = hoveredChoice(mouseX, mouseY), first = scrollRow * 4;
        for (int i = first; i < Math.min(previews.size(), first + visibleRows * 4); i++) {
            int x = guiLeft + gridX + (i - first) % 4 * cellWidth, y = guiTop + gridY + (i - first) / 4 * cellHeight;
            String state = i == container.selected ? "_selected" : i == hovered ? "_highlighted" : "";
            sprite(kind + "/" + (loom ? "pattern" : "recipe") + state, x, y, cellWidth, cellHeight);
        }
        int thumbY = maxScroll() == 0 ? 0 : Math.round((visibleRows * cellHeight - 15F) * scrollRow / maxScroll());
        sprite(kind + "/scroller" + (maxScroll() == 0 ? "_disabled" : ""), guiLeft + 119, guiTop + gridY + thumbY, 12, 15);
        for (int i = first; i < Math.min(previews.size(), first + visibleRows * 4); i++) {
            int x = guiLeft + gridX + (i - first) % 4 * cellWidth, y = guiTop + gridY + (i - first) / 4 * cellHeight;
            drawItem(previews.get(i), x + (loom ? 1 : 0), y + 1, loom ? .75F : 1);
        }
    }

    private void drawItem(ItemStack stack, int x, int y, float scale) {
        GL11.glPushMatrix();
        if (scale != 1) { GL11.glTranslatef(x, y, 0); GL11.glScalef(scale, scale, 1); x = y = 0; }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glColor4f(1, 1, 1, 1);
        itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    private String operation() {
        List<String> choices = container.choices();
        return choices.isEmpty() ? "" : choices.get(0);
    }
    private void drawMapPreview() {
        ItemStack input = container.inputs.getStackInSlot(0);
        if (empty(input) || input.getItem() != Items.filled_map) { lastMap = null; return; }
        MapData data = Items.filled_map.getMapData(input, mc.theWorld);
        if (data != null && data != lastMap) {
            mc.entityRenderer.getMapItemRenderer().func_148246_a(data); lastMap = data;
        }
        String operation = operation();
        if (operation.endsWith(".copy")) {
            mapThumbnail(data, 67, 13, .7F, false);
            mapThumbnail(data, 85, 30, .7F, false);
        } else {
            mapThumbnail(data, 67, 13, 1, operation.endsWith(".scale"));
            if (operation.endsWith(".lock")) sprite("cartography_table/locked", guiLeft + 120, guiTop + 61, 10, 14);
        }
    }
    private void mapThumbnail(MapData data, int x, int y, float scale, boolean expanding) {
        GL11.glPushMatrix();
        GL11.glTranslatef(guiLeft + x, guiTop + y, 0);
        GL11.glScalef(scale, scale, 1);
        sprite("cartography_table/" + (expanding ? "scaled_map" : "map"), 0, 0, 66, 66);
        if (data != null) {
            GL11.glTranslatef(expanding ? 17 : 1, expanding ? 17 : 1, 1);
            GL11.glScalef(expanding ? .25F : .5F, expanding ? .25F : .5F, 1);
            mc.entityRenderer.getMapItemRenderer().func_148250_a(data, false);
        }
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partial) {
        super.drawScreen(mouseX, mouseY, partial);

        if (!empty(mc.thePlayer.inventory.getItemStack())) return;
        int hovered = hoveredChoice(mouseX, mouseY);
        if (hovered >= 0) renderToolTip(previews.get(hovered), mouseX, mouseY);
        else if (cartography && inside(mouseX, mouseY, 67, 13, 66, 66) && !operation().isEmpty())
            drawHoveringText(Collections.singletonList(I18n.format(operation())), mouseX, mouseY, fontRendererObj);
        else if (loom) {
            String[] names = {"banner", "dye", "pattern"};
            int[] positions = {13, 26, 33, 26, 23, 45};
            for (int i = 0; i < names.length; i++) if (empty(container.inputs.getStackInSlot(i)) && inside(mouseX, mouseY, positions[i * 2], positions[i * 2 + 1], 16, 16))
                drawHoveringText(Collections.singletonList(I18n.format("gui.etfuturum.loom.slot." + names[i])), mouseX, mouseY, fontRendererObj);
        }
    }
}
