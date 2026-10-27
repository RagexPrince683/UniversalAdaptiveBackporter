package ganymedes01.etfuturum;

import ganymedes01.etfuturum.entities.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.BiomeDictionary;
import java.util.*;
/** Released tower and bounded surrounding pieces, each placed only during its own chunk's first population. */
public final class OutpostStructures extends WorldSavedData {
	private static final String[] NAMES = {"watchtower", "feature_cage_with_allays", "feature_tent1", "feature_logs", "feature_targets"};
	
	private final Map<Long, Piece> pending = new HashMap<>();
	public OutpostStructures(String name) { super(name); }
	private static OutpostStructures get(World world) {
		OutpostStructures data = (OutpostStructures) world.getPerWorldStorage().getOrLoadData(OutpostStructures.class, "etfuturum_outpost_pieces");
		if (data == null) { data = new OutpostStructures("etfuturum_outpost_pieces"); world.getPerWorldStorage().setData("etfuturum_outpost_pieces", data); }
		return data;
	}
	private static StructureTemplate template(int type){return StructureTemplate.get("pillager_outpost/"+NAMES[type]);}
    public static void populate(World world,Random random,int cx,int cz){if(!Features.enabled("entities.enableOutposts")||!StructureTerrain.overworld(world)||!world.getWorldInfo().isMapFeaturesEnabled())return;populatePending(world,random,cx,cz);if(random.nextInt(600)!=0)return;BlockPos center=new BlockPos(cx*16+7,0,cz*16+7);net.minecraft.world.biome.Biome biome=world.getBiome(center);if(!BiomeDictionary.hasType(biome,BiomeDictionary.Type.PLAINS)&&!BiomeDictionary.hasType(biome,BiomeDictionary.Type.SAVANNA)&&!BiomeDictionary.hasType(biome,BiomeDictionary.Type.SANDY)&&!BiomeDictionary.hasType(biome,BiomeDictionary.Type.FOREST))return;BlockPos floor=world.getTopSolidOrLiquidBlock(center);if(floor.getY()<60||floor.getY()>230||world.getVillageCollection().getNearestVillage(floor,80)!=null)return;tower(world,random,cx,cz);}

	public static void populatePending(World world, Random random, int cx, int cz) {
		OutpostStructures data = get(world); Piece piece = data.pending.remove(ChunkPos.asLong(cx, cz));
		if (piece != null) { place(world, new Random(piece.seed), cx, cz, piece.type, piece.rotation); data.markDirty(); }
	}
	public static boolean tower(World world, Random random, int cx, int cz) {
		if (RaidProgress.get(world).isOutpost(cx * 16, cz * 16)) return false;
		int rotation = random.nextInt(4);
		if (!place(world, random, cx, cz, 0, rotation)) return false;
		RaidProgress.get(world).addOutpost(cx, cz);
		OutpostStructures data = get(world);
		int[][] offsets = {{2,0},{0,2},{-2,0},{0,-2}};
		for (int i = 0; i < offsets.length; i++) {
			int direction = (i + rotation) & 3, x = cx + offsets[direction][0], z = cz + offsets[direction][1];
			// Previously populated neighbors are never changed, even if an outpost is discovered beside them.
			if (!StructureTerrain.fresh(world,new StructureBoundingBox(x*16,0,z*16,x*16+15,255,z*16+15))) continue;
			long key = ChunkPos.asLong(x, z);
			if (!data.pending.containsKey(key)) data.pending.put(key, new Piece(i + 1, random.nextInt(4), random.nextLong()));
		}
		data.markDirty(); return true;
	}
	private static boolean place(World world, Random random, int cx, int cz, int type, int rotation) {
		StructureTemplate template = template(type);if(!template.available()||template.sx>16||template.sz>16||template.sy>32)return false; int width = (rotation & 1) == 0 ? template.sx : template.sz, depth = (rotation & 1) == 0 ? template.sz : template.sx;
		int x = cx * 16 + (16 - width) / 2, z = cz * 16 + (16 - depth) / 2;
		if (!world.isBlockLoaded(new BlockPos(cx*16,64,cz*16))) return false;
		int y = world.getTopSolidOrLiquidBlock(new BlockPos(x+width/2,0,z+depth/2)).getY()-1;
		if (y < 59 || y + template.sy >= world.getActualHeight()) return false;
		for (int dx = 0; dx < width; dx++) for (int dz = 0; dz < depth; dz++) {
			int ground = world.getTopSolidOrLiquidBlock(new BlockPos(x+dx,0,z+dz)).getY()-1;
			if (Math.abs(ground - y) > 3 || world.getBlockState(new BlockPos(x+dx,ground,z+dz)).getMaterial().isLiquid()) return false;
		}
		StructureBoundingBox box=template.box(0,0,0,rotation);int ox=x-box.minX,oz=z-box.minZ;
        StructureBoundingBox footprint=template.box(ox,y,oz,rotation);if(StructureReservations.vanilla(world,footprint))return false;
        for(int i=0;i<template.blocks.tagCount();i++){NBTTagCompound entry=template.blocks.getCompoundTagAt(i);BlockPos local=StructureTemplate.position(entry,rotation);if(local.getY()!=0||template.names.get(entry.getInteger("state")).equals("minecraft:air"))continue;BlockPos base=local.add(ox,y,oz);for(int dy=1;dy<=4&&!world.getBlockState(base.down(dy)).getMaterial().isSolid();dy++)world.setBlockState(base.down(dy),Blocks.COBBLESTONE.getDefaultState(),18);}
        template.place(world,ox,y,oz,rotation,new StructureBoundingBox(cx*16,0,cz*16,cx*16+15,255,cz*16+15),world.getSeed(),"minecraft:empty");
		if (type == 0&&MobRegistry.enabled(EntityPillager.class)) for (int i = 0; i < 3; i++) { EntityPillager pillager = new EntityPillager(world); pillager.setCaptain(i == 0); mob(world, pillager, x + width / 2 + i - 1, y + 14, z + depth / 2); }
		if (type == 1&&MobRegistry.enabled(EntityAllay.class)) for (int i = 0; i < 2; i++) mob(world, new EntityAllay(world), x + width / 2, y + 1, z + depth / 2);
		return true;
	}
	private static void mob(World world, EntityLiving mob, int x, int y, int z) {
		mob.setLocationAndAngles(x + .5, y, z + .5, world.rand.nextFloat() * 360, 0);
		if (!world.getCollisionBoxes(mob,mob.getEntityBoundingBox()).isEmpty()) return;
		mob.onInitialSpawn(world.getDifficultyForLocation(mob.getPosition()),null);mob.enablePersistence();world.spawnEntity(mob);
	}
	@Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		NBTTagList pieces = new NBTTagList();
		for (Map.Entry<Long, Piece> entry : pending.entrySet()) { NBTTagCompound piece = new NBTTagCompound(); piece.setLong("Chunk", entry.getKey()); piece.setInteger("Type", entry.getValue().type); piece.setInteger("Rotation", entry.getValue().rotation); piece.setLong("Seed", entry.getValue().seed); pieces.appendTag(piece); }
		tag.setTag("Pieces", pieces);return tag;
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		pending.clear(); NBTTagList pieces = tag.getTagList("Pieces", 10);
		for (int i = 0; i < pieces.tagCount(); i++) { NBTTagCompound piece = pieces.getCompoundTagAt(i); int type = piece.getInteger("Type"); if (type > 0 && type < NAMES.length) pending.put(piece.getLong("Chunk"), new Piece(type, piece.getInteger("Rotation") & 3, piece.getLong("Seed"))); }
	}
	private static final class Piece { final int type, rotation; final long seed; Piece(int type, int rotation, long seed) { this.type = type; this.rotation = rotation; this.seed = seed; } }
}
