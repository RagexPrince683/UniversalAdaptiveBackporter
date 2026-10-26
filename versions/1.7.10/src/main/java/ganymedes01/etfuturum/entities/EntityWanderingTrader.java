package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.*;
import net.minecraft.village.*;
import net.minecraft.world.World;

public class EntityWanderingTrader extends EntityVillager {
	private int despawnDelay = 48000;
	private MerchantRecipeList offers;
	public EntityWanderingTrader(World world) { super(world, 0); }
	@Override public boolean isMating() { return false; }
	@Override public EntityVillager createChild(EntityAgeable mate) { return null; }
	@Override public MerchantRecipeList getRecipes(EntityPlayer player) {
		if (offers == null) {
			offers = new MerchantRecipeList();
			net.minecraft.item.Item[] items = {net.minecraft.item.Item.getItemFromBlock(Blocks.sapling), net.minecraft.item.Item.getItemFromBlock(Blocks.red_flower), net.minecraft.item.Item.getItemFromBlock(Blocks.sand), Items.dye, Items.reeds, Items.slime_ball};
			for (int i = 0; i < 5; i++) { int choice = rand.nextInt(items.length); offers.add(new MerchantRecipe(new ItemStack(Items.emerald, 1 + rand.nextInt(4)), new ItemStack(items[choice], choice == 2 ? 8 : 1, choice == 0 ? rand.nextInt(6) : choice == 3 ? rand.nextInt(16) : 0))); }
			offers.add(new MerchantRecipe(new ItemStack(Items.emerald, 5), new ItemStack(Items.water_bucket)));
		}
		return offers;
	}
	@Override public void useRecipe(MerchantRecipe recipe) { recipe.incrementToolUses(); }
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote) { if (!isTrading() && --despawnDelay <= 0) setDead(); if (!worldObj.isDaytime()) { if (!isPotionActive(Potion.invisibility)) addPotionEffect(new PotionEffect(Potion.invisibility.id, 24000, 0)); } else removePotionEffect(Potion.invisibility.id); } }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("DespawnDelay", despawnDelay); if (offers != null) tag.setTag("WanderingOffers", offers.getRecipiesAsTags()); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); despawnDelay = tag.hasKey("DespawnDelay") ? net.minecraft.util.MathHelper.clamp_int(tag.getInteger("DespawnDelay"), 0, 48000) : 48000; if (tag.hasKey("WanderingOffers", 10)) offers = new MerchantRecipeList(tag.getCompoundTag("WanderingOffers")); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.wandering_trader.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.wandering_trader.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.wandering_trader.death"; }
}
