package ganymedes01.etfuturum.combat;

/** Released Mace damage and armor-effectiveness rules, independent of the native combat adapter. */
public final class MaceRules {
    public static final int DURABILITY = 500;
    private MaceRules() { }
    public static float smashDamage(float distance, int density) {
        if (distance <= 1.5F) return 0;
        float damage = distance <= 3 ? 4 * distance : distance <= 8 ? 12 + 2 * (distance - 3) : 22 + distance - 8;
        return damage + distance * .5F * Math.max(0, Math.min(5, density));
    }
    public static double breach(double beforeArmor, double afterArmor, int level) {
        return afterArmor + Math.max(0, beforeArmor - afterArmor) * .15 * Math.max(0, Math.min(4, level));
    }
    public static double knockback(double distance, float fallDistance, double resistance) {
        return Math.max(0, 3.5 - distance) * .7 * (fallDistance > 5 ? 2 : 1) * Math.max(0, 1 - resistance);
    }
}
