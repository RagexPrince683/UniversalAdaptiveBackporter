package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class LoomContainer extends WorkstationContainer {
    private static final String[] DYES = {"dyeBlack","dyeRed","dyeGreen","dyeBrown","dyeBlue","dyePurple","dyeCyan","dyeLightGray","dyeGray","dyePink","dyeLime","dyeYellow","dyeLightBlue","dyeMagenta","dyeOrange","dyeWhite"};
    private List<BannerPattern> patterns = Collections.emptyList();
    public LoomContainer(InventoryPlayer inventory, World world, BlockPos pos) { super(inventory, world, pos, Content.block("loom"), 3); layoutSlots(new int[]{13,26,33,26,23,45},143,58); }
    public static int dye(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        if (stack.getItem() == Items.DYE && stack.getMetadata() >= 0 && stack.getMetadata() < 16) return stack.getMetadata();
        for (int ore : net.minecraftforge.oredict.OreDictionary.getOreIDs(stack)) for (int i = 0; i < DYES.length; i++) if (DYES[i].equals(net.minecraftforge.oredict.OreDictionary.getOreName(ore))) return i;
        return -1;
    }
    @Override protected boolean accepts(int slot, ItemStack stack) { return !stack.isEmpty() && (slot == 0 ? stack.getItem() == Items.BANNER : slot == 1 ? dye(stack) >= 0 : stack.getItem() instanceof BannerPatternItem); }
    @Override protected void refresh() {
        patterns = new ArrayList<>();
        ItemStack banner = inputs.getStackInSlot(0), dye = inputs.getStackInSlot(1), template = inputs.getStackInSlot(2);
        if (accepts(0, banner) && accepts(1, dye) && TileEntityBanner.getPatterns(banner) < 6) {
            if (!template.isEmpty()) { if (accepts(2, template)) patterns.add(((BannerPatternItem) template.getItem()).pattern); }
            else for (BannerPattern pattern : BannerPattern.values()) if (pattern.hasPattern() && !pattern.hasPatternItem()) patterns.add(pattern);
        }
        if (selected >= patterns.size()) selected = 0;
        ItemStack result = ItemStack.EMPTY;
        if (!patterns.isEmpty()) {
            result = applyPattern(banner, dye(dye), patterns.get(selected));
        }
        output.setInventorySlotContents(0, result);
    }
    @Override public List<String> choices() { List<String> result = new ArrayList<>(); for (BannerPattern pattern : patterns) result.add("gui.etfuturum.loom." + BannerPatternItem.name(pattern)); return result; }
    private static ItemStack applyPattern(ItemStack banner, int color, BannerPattern pattern) {
        ItemStack result = banner.copy(); result.setCount(1); NBTTagCompound tag = result.getOrCreateSubCompound("BlockEntityTag");
        NBTTagList layers = tag.getTagList("Patterns", 10); NBTTagCompound layer = new NBTTagCompound();
        layer.setString("Pattern", pattern.getHashname()); layer.setInteger("Color", color); layers.appendTag(layer); tag.setTag("Patterns", layers);
        return result;
    }
    @Override public List<ItemStack> previews() { List<ItemStack> result = new ArrayList<>(); for (BannerPattern pattern : patterns) result.add(applyPattern(inputs.getStackInSlot(0),dye(inputs.getStackInSlot(1)),pattern)); return result; }
    @Override protected ItemStack craft(EntityPlayer player) { ItemStack result = output.getStackInSlot(0).copy(); inputs.decrStackSize(0, 1); inputs.decrStackSize(1, 1); return result; }
    @Override public String title() { return "tile.etfuturum.loom.name"; }
}
