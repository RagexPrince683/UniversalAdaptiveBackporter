package ganymedes01.etfuturum.lib;

import ganymedes01.etfuturum.Tags;
import net.minecraft.launchwrapper.Launch;

public class Reference {
	public static final String DEPENDENCIES =
			"required-after:Forge@[10.13.4.1614,);" +
					"after:Thaumcraft@[4.2.3.5,);" +
					"after:TwilightForest;" +
					"after:HardcoreEnderExpansion;" +
					"after:bluepower;after:MineTweaker3;" +
					"after:TConstruct;" +
					"after:IronChest;" +
					"after:BiomesOPlenty;" +
					"after:witchery;";
	public static final boolean TESTING = Boolean.getBoolean("etfuturum.testing");
	public static final boolean DEV_ENVIRONMENT = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	public static final String VERSION_URL = System.getProperty("uab.versionUrl", System.getProperty("etfuturum.versionUrl",
			"https://github.com/RagexPrince683/Et-Futurum-Requiem-Reforged/releases/latest/download/uab-update.json"));

	public static final String ITEM_BLOCK_TEXTURE_PATH = Tags.MOD_ID + ":";
	public static final String ARMOUR_TEXTURE_PATH = ITEM_BLOCK_TEXTURE_PATH + "textures/models/armor/";
	public static final String ENTITY_TEXTURE_PATH = ITEM_BLOCK_TEXTURE_PATH + "textures/entities/";


	@SuppressWarnings("ConstantValue")
	public static final boolean SNAPSHOT_BUILD = Boolean.parseBoolean(Tags.SNAPSHOT_BUILD);
	@SuppressWarnings("ConstantValue")
	public static final boolean STABLE_BUILD = Boolean.parseBoolean(Tags.STABLE_BUILD);
}
