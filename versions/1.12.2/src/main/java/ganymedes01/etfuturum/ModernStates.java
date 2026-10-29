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
        if(!Content.BLOCKS.containsKey(name)&&name.endsWith("_brick_stairs")&&Content.BLOCKS.containsKey(name.replace("_brick_stairs","_bricks_stairs")))name=name.replace("_brick_stairs","_bricks_stairs");
        if(!Content.BLOCKS.containsKey(name)&&name.endsWith("_tile_stairs")&&Content.BLOCKS.containsKey(name.replace("_tile_stairs","_tiles_stairs")))name=name.replace("_tile_stairs","_tiles_stairs");
        if(name.equals("iron_chain"))name="chain";
        if(name.equals("note_block"))name="noteblock";
        if(name.equals("dead_bush"))name="deadbush";
        if(name.equals("oak_button"))name="wooden_button";
        if(name.equals("oak_pressure_plate"))name="wooden_pressure_plate";
        if(name.equals("cobweb"))name="web";
        if(name.equals("carved_pumpkin"))name="pumpkin";
        if(name.equals("light_gray_glazed_terracotta"))name="silver_glazed_terracotta";
        if(name.equals("coarse_dirt"))state=Blocks.DIRT.getStateFromMeta(1);
        if(name.equals("podzol"))state=Blocks.DIRT.getStateFromMeta(2);
        if(name.equals("skeleton_skull")||name.equals("skeleton_wall_skull"))state=Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING,name.equals("skeleton_skull")?net.minecraft.util.EnumFacing.UP:net.minecraft.util.EnumFacing.byName(entry.getCompoundTag("Properties").getString("facing")));
        if(name.equals("redstone_wall_torch"))name="redstone_torch";
        if(name.equals("redstone_torch"))state=(entry.getCompoundTag("Properties").getString("lit").equals("false")?Blocks.UNLIT_REDSTONE_TORCH:Blocks.REDSTONE_TORCH).getDefaultState();
        if(name.equals("repeater"))state=(entry.getCompoundTag("Properties").getBoolean("powered")||entry.getCompoundTag("Properties").getString("powered").equals("true")?Blocks.POWERED_REPEATER:Blocks.UNPOWERED_REPEATER).getDefaultState();
        if(name.equals("comparator"))state=(entry.getCompoundTag("Properties").getString("powered").equals("true")?Blocks.POWERED_COMPARATOR:Blocks.UNPOWERED_COMPARATOR).getDefaultState();
        if(name.equals("cave_air")||name.equals("void_air"))name="air";
        if(name.equals("spawner"))name="mob_spawner";
        if(name.equals("terracotta"))name="hardened_clay";
        if(name.equals("water_cauldron"))name="cauldron";
        if(name.equals("wall_torch"))name="torch";
        if(name.equals("soul_wall_torch"))name="soul_torch";
        if(name.equals("short_grass"))state=Blocks.TALLGRASS.getStateFromMeta(1);
        if(name.equals("fern"))state=Blocks.TALLGRASS.getStateFromMeta(2);
        if(name.equals("dandelion"))state=Blocks.YELLOW_FLOWER.getDefaultState();
        String[] flowers={"poppy","blue_orchid","allium","azure_bluet","red_tulip","orange_tulip","white_tulip","pink_tulip","oxeye_daisy"};for(int i=0;i<flowers.length;i++)if(name.equals(flowers[i]))state=Blocks.RED_FLOWER.getStateFromMeta(i);
        for(net.minecraft.item.EnumDyeColor color:net.minecraft.item.EnumDyeColor.values()){String prefix=(color==net.minecraft.item.EnumDyeColor.SILVER?"light_gray":color.getName())+"_";if(name.equals(prefix+"bed"))state=Blocks.BED.getDefaultState();if(name.equals(prefix+"terracotta"))state=Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(color.getMetadata());if(name.equals(prefix+"wool"))state=Blocks.WOOL.getStateFromMeta(color.getMetadata());if(name.equals(prefix+"concrete"))state=Blocks.CONCRETE.getStateFromMeta(color.getMetadata());if(name.equals(prefix+"concrete_powder"))state=Blocks.CONCRETE_POWDER.getStateFromMeta(color.getMetadata());if(name.equals(prefix+"stained_glass"))state=Blocks.STAINED_GLASS.getStateFromMeta(color.getMetadata());if(name.equals(prefix+"stained_glass_pane"))state=Blocks.STAINED_GLASS_PANE.getStateFromMeta(color.getMetadata());if(name.equals(prefix+"carpet"))state=Blocks.CARPET.getStateFromMeta(color.getMetadata());if(name.equals(prefix+"banner"))state=Blocks.STANDING_BANNER.getDefaultState();if(name.equals(prefix+"wall_banner"))state=Blocks.WALL_BANNER.getDefaultState();}
        String[] slabNames={"stone_slab","sandstone_slab","petrified_oak_slab","cobblestone_slab","brick_slab","stone_brick_slab","nether_brick_slab","quartz_slab"};for(int i=0;i<slabNames.length;i++)if(name.equals(slabNames[i])&&!Content.BLOCKS.containsKey(name)){String type=entry.getCompoundTag("Properties").getString("type");state=(type.equals("double")?Blocks.DOUBLE_STONE_SLAB:Blocks.STONE_SLAB).getStateFromMeta(i+(type.equals("top")?8:0));}
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
            if(name.equals(tree.getName()+"_leaves"))state=(tree.getMetadata()<4?Blocks.LEAVES:Blocks.LEAVES2).getStateFromMeta(tree.getMetadata()%4|4);
            if(name.equals(tree.getName()+"_sapling"))state=Blocks.SAPLING.getStateFromMeta(tree.getMetadata());
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
    static IBlockState structure(String identifier,Map<String,String> original,int rotation){
        String name=identifier.isEmpty()?"minecraft:air":identifier;Map<String,String> properties=new HashMap<>(original);int bracket=name.indexOf('[');if(bracket>=0){if(!name.endsWith("]"))throw new IllegalArgumentException("Malformed structure state "+name);for(String entry:name.substring(bracket+1,name.length()-1).split(",")){String[] pair=entry.split("=",2);if(pair.length!=2)throw new IllegalArgumentException("Malformed structure property "+entry);properties.put(pair[0],pair[1]);}name=name.substring(0,bracket);}
        if(name.equals("minecraft:jigsaw")||name.equals("minecraft:structure_void"))return Blocks.AIR.getDefaultState();
        if(name.equals("minecraft:powder_snow"))return PowderSnow.enabled()?Content.block("powder_snow").getDefaultState():Blocks.SNOW.getDefaultState();
        if(name.startsWith("minecraft:potted_"))return Blocks.FLOWER_POT.getDefaultState();
        if(name.equals("minecraft:damaged_anvil"))return Blocks.ANVIL.getDefaultState().withProperty(BlockAnvil.DAMAGE,2).withProperty(BlockAnvil.FACING,net.minecraft.util.EnumFacing.byName(properties.getOrDefault("facing","north"))).withRotation(net.minecraft.util.Rotation.values()[rotation&3]);
        NBTTagCompound entry=new NBTTagCompound(),props=new NBTTagCompound();entry.setString("Name",name);for(Map.Entry<String,String> property:properties.entrySet())props.setString(property.getKey(),property.getValue());entry.setTag("Properties",props);IBlockState state=read(entry);return state==null?null:state.withRotation(net.minecraft.util.Rotation.values()[rotation&3]);
    }
    private static <T extends Comparable<T>> IBlockState apply(IBlockState state, IProperty<T> property, String value) {
        com.google.common.base.Optional<T> parsed = property.parseValue(value);
        return parsed.isPresent() ? state.withProperty(property, parsed.get()) : state;
    }
}
