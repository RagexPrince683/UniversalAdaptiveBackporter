package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/** Explicit flattening adapter. Missing content rejects a structure rather than becoming stone. */
final class ModernStates {
    private ModernStates() { }
    static IBlockState read(NBTTagCompound entry) {
        String original = entry.getString("Name"), name = original.replace("minecraft:", "");
        IBlockState state = null;
        if (name.equals("structure_block")) return Blocks.AIR.getDefaultState();
        if (name.equals("grass_block")) name = "grass";
        if (name.equals("oak_fence")) name = "fence";
        if (name.equals("oak_trapdoor")) name = "trapdoor";
        if (name.equals("oak_door")) name = "wooden_door";
        if (name.equals("magma_block")) name = "magma";
        if (name.equals("bricks")) name = "brick_block";
        if (name.equals("cobblestone_stairs")) state = Blocks.STONE_STAIRS.getDefaultState();
        if (name.equals("stone_bricks")) state = Blocks.STONEBRICK.getStateFromMeta(0);
        if (name.equals("mossy_stone_bricks")) state = Blocks.STONEBRICK.getStateFromMeta(1);
        if (name.equals("cracked_stone_bricks")) state = Blocks.STONEBRICK.getStateFromMeta(2);
        if (name.equals("chiseled_stone_bricks")) state = Blocks.STONEBRICK.getStateFromMeta(3);
        if (name.equals("chiseled_sandstone")) state = Blocks.SANDSTONE.getStateFromMeta(1);
        if (name.equals("cut_sandstone")) state = Blocks.SANDSTONE.getStateFromMeta(2);
        if (name.equals("light_blue_terracotta")) state = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(3);
        for (BlockPlanks.EnumType tree : BlockPlanks.EnumType.values()) {
            if (name.equals(tree.getName() + "_planks")) state = Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, tree);
            if (name.equals(tree.getName() + "_log")) state = tree.getMetadata() < 4 ? Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, tree) : Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, tree);
            if (name.equals(tree.getName() + "_slab")) {
                String type = entry.getCompoundTag("Properties").getString("type");
                state = type.equals("double") ? Blocks.DOUBLE_WOODEN_SLAB.getDefaultState().withProperty(BlockWoodSlab.VARIANT, tree) : Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockWoodSlab.VARIANT, tree).withProperty(BlockSlab.HALF, type.equals("top") ? BlockSlab.EnumBlockHalf.TOP : BlockSlab.EnumBlockHalf.BOTTOM);
            }
        }
        String[] stones = {"stone", "granite", "polished_granite", "diorite", "polished_diorite", "andesite", "polished_andesite"};
        for (int i = 0; i < stones.length; i++) if (name.equals(stones[i])) state = Blocks.STONE.getStateFromMeta(i);
        if(name.equals("tall_seagrass"))name="seagrass";
        if (state == null) {
            ResourceLocation id = new ResourceLocation(original.startsWith("etfuturum:") ? original : "minecraft:" + name);
            if (Content.BLOCKS.containsKey(name)) { if (!Content.available(name)) return null; state = Content.block(name).getDefaultState(); }
            else if (Block.REGISTRY.containsKey(id)) state = Block.REGISTRY.getObject(id).getDefaultState();
            else return null;
        }
        NBTTagCompound properties = entry.getCompoundTag("Properties");
        if(original.endsWith("tall_seagrass")&&Content.available("seagrass"))state=Content.block("seagrass").getDefaultState().withProperty(Seagrass.PART,properties.getString("half").equals("upper")?Seagrass.Part.UPPER:Seagrass.Part.LOWER);
        if(state.getBlock() instanceof AquaticPlant&&!((AquaticPlant)state.getBlock()).submerged&&properties.getString("waterlogged").equals("true")) {
            String wet=state.getBlock().getRegistryName().getPath()+"_underwater";if(Content.BLOCKS.containsKey(wet))state=Content.block(wet).getDefaultState();
        }
        if (state.getBlock() instanceof NativeSlab) {
            NativeSlab slab = (NativeSlab) state.getBlock(); String type = properties.getString("type");
            state = type.equals("double") ? slab.doubled.getDefaultState() : slab.single.getDefaultState().withProperty(BlockSlab.HALF, type.equals("top") ? BlockSlab.EnumBlockHalf.TOP : BlockSlab.EnumBlockHalf.BOTTOM);
        }
        for (String key : properties.getKeySet()) {
            // Waterlogging is outside the port's existing engine boundary.
            if (key.equals("waterlogged")) continue;
            IProperty<?> property = state.getBlock().getBlockState().getProperty(key);
            if (property != null) state = apply(state, property, properties.getString(key));
        }
        return state;
    }
    private static <T extends Comparable<T>> IBlockState apply(IBlockState state, IProperty<T> property, String value) {
        com.google.common.base.Optional<T> parsed = property.parseValue(value);
        return parsed.isPresent() ? state.withProperty(property, parsed.get()) : state;
    }
}
