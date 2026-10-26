package ganymedes01.etfuturum.configuration.configs;

import ganymedes01.etfuturum.configuration.ConfigBase;

import java.io.File;

public class ConfigEnchantsPotions extends ConfigBase {

	public static boolean enableFrostWalker;
	public static boolean enableMending;
	public static boolean enableSwiftSneak;
	public static boolean enableSoulSpeed;
	public static int soulSpeedID;
	public static int mendingID;
	public static int frostWalkerID;
	public static int swiftSneakID;
	public static int levitationID;
	public static int glowingID;
	public static int darknessID, nautilusBreathID, dolphinGraceID;
	public static int quickChargeID, multishotID, piercingID, loyaltyID, impalingID, riptideID, channelingID;

	static final String catEnchants = "enchantments";
	static final String catPotions = "potions";


	public ConfigEnchantsPotions() {
		super("enchantspotions");
	}

	@Override
	protected void syncConfigOptions() {
		String allocation = " -1 uses the persistent automatic allocation; existing explicit IDs are imported and preserved. Keep config/legacy-potion-ids.properties with saves and identical on clients/server.";
		dolphinGraceID = getInt("dolphinGraceID", catPotions, -1, -1, Short.MAX_VALUE, "Dolphins temporarily reduce horizontal water drag." + allocation);
		darknessID = getInt("darknessID", catPotions, -1, -1, Short.MAX_VALUE, "Warden darkness." + allocation);
		nautilusBreathID = getInt("nautilusBreathID", catPotions, -1, -1, Short.MAX_VALUE, "Breath of the Nautilus pauses oxygen consumption without replenishing air." + allocation);
		quickChargeID = getInt("quickChargeID", catEnchants, 204, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		multishotID = getInt("multishotID", catEnchants, 205, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		piercingID = getInt("piercingID", catEnchants, 206, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		loyaltyID = getInt("loyaltyID", catEnchants, 207, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		impalingID = getInt("impalingID", catEnchants, 208, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		riptideID = getInt("riptideID", catEnchants, 209, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		channelingID = getInt("channelingID", catEnchants, 210, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		//enchants
		enableSoulSpeed = getBoolean("soulSpeed", catEnchants, true, "Soul Speed is acquired from piglin bartering and bastion loot. It boosts movement on soul sand/soil and wears enchanted boots.");
		soulSpeedID = getInt("soulSpeedID", catEnchants, 203, 0, Short.MAX_VALUE, "Keep this ID stable in existing saves. Collisions fail explicitly.");
		enableFrostWalker = getBoolean("frostWalker", catEnchants, true, "");
		frostWalkerID = getInt("frostWalkerID", catEnchants, 200, 0, Short.MAX_VALUE, "");
		enableMending = getBoolean("mending", catEnchants, true, "");
		mendingID = getInt("mendingID", catEnchants, 201, 0, Short.MAX_VALUE, "");
		enableSwiftSneak = getBoolean("swiftSneak", catEnchants, true, "");
		swiftSneakID = getInt("swiftSneakID", catEnchants, 202, 0, Short.MAX_VALUE, "");

		//potions
		levitationID = getInt("levitationID", catPotions, -1, -1, Short.MAX_VALUE, "Tied to Shulkers being enabled." + allocation);
		glowingID = getInt("glowingID", catPotions, -1, -1, Short.MAX_VALUE, allocation);
	}

}
