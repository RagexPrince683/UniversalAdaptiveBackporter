package ganymedes01.etfuturum.configuration.configs;

import ganymedes01.etfuturum.configuration.ConfigBase;
import ganymedes01.etfuturum.configuration.UabConfiguration;

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
	public static int densityID, breachID, windBurstID, lungeID;

	static final String catEnchants = "enchantments";
	static final String catPotions = "potions";


	public ConfigEnchantsPotions() {
		super("enchantspotions");
	}

	@Override
	protected void syncConfigOptions() {
		dolphinGraceID = potionId("dolphinGraceID");
		darknessID = potionId("darknessID");
		nautilusBreathID = potionId("nautilusBreathID");
		quickChargeID = getInt("quickChargeID", catEnchants, 204, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		multishotID = getInt("multishotID", catEnchants, 205, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		piercingID = getInt("piercingID", catEnchants, 206, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		loyaltyID = getInt("loyaltyID", catEnchants, 207, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		impalingID = getInt("impalingID", catEnchants, 208, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		riptideID = getInt("riptideID", catEnchants, 209, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		channelingID = getInt("channelingID", catEnchants, 210, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		densityID = getInt("densityID", catEnchants, 211, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		breachID = getInt("breachID", catEnchants, 212, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		windBurstID = getInt("windBurstID", catEnchants, 213, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
		lungeID = getInt("lungeID", catEnchants, 214, 0, Short.MAX_VALUE, "Keep stable; occupied IDs fail explicitly.");
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
		levitationID = potionId("levitationID");
		glowingID = potionId("glowingID");
	}

	private static int potionId(String key) {
		UabConfiguration config = UabConfiguration.get();
		net.minecraftforge.common.config.Property property = config.property("enchantspotions", key, -1,
				net.minecraftforge.common.config.Property.Type.INTEGER);
		int value;
		try { value = Integer.parseInt(property.getString().trim()); }
		catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Invalid potion ID for " + key + ": " + property.getString()
					+ ". Restore the matching configuration; saved IDs cannot be remapped automatically.", exception);
		}
		if (property.isList() || value < -1 || value > Short.MAX_VALUE)
			throw new IllegalArgumentException(key + " potion ID " + value + " is outside -1..32767."
					+ " Restore the matching configuration; saved IDs cannot be remapped automatically.");
		// Record the validated identity in the canonical configuration/handshake state.
		return config.integer("enchantspotions", key, -1, -1, Short.MAX_VALUE);
	}

}
