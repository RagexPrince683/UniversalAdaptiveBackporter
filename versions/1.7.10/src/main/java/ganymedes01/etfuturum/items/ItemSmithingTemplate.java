package ganymedes01.etfuturum.items;

/** A consumed recipe ingredient; pattern identities are also used in equipment NBT. */
public final class ItemSmithingTemplate extends BaseItem {
	public final String pattern;
	public ItemSmithingTemplate(String pattern) {
		super(pattern.equals("netherite_upgrade") ? "netherite_upgrade_smithing_template" : pattern + "_armor_trim_smithing_template");
		this.pattern = pattern;
	}
}
