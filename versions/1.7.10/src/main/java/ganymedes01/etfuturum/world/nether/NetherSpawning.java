package ganymedes01.etfuturum.world.nether;

import ganymedes01.etfuturum.entities.EntityStrider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.EnumHelper;
import java.util.Collections;
import java.util.List;

public final class NetherSpawning {
	public static EnumCreatureType STRIDERS;
	public static final List<BiomeGenBase.SpawnListEntry> SPAWNS = ganymedes01.etfuturum.configuration.SpawnSettings.list(new BiomeGenBase.SpawnListEntry(EntityStrider.class, 60, 1, 2));
	private NetherSpawning() { }
	public static void init() { if (STRIDERS == null) STRIDERS = EnumHelper.addCreatureType("EFR_STRIDER", EntityStrider.class, 15, Material.lava, true, true); }
}
