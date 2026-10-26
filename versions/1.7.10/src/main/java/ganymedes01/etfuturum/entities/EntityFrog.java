package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityFrog extends EntityAnimal {
	private boolean pregnant;
	public EntityFrog(World world) {
		super(world); setSize(0.5F, 0.5F);
		tasks.addTask(0, new EntityAISwimming(this)); tasks.addTask(1, new EntityAIPanic(this, 1.5));
		tasks.addTask(2, new EntityAIMate(this, 1)); tasks.addTask(3, new EntityAITempt(this, 1.2, Items.slime_ball, false));
		tasks.addTask(4, new EntityAIAttackOnCollide(this, 1.2, true)); tasks.addTask(5, new EntityAIWander(this, 1)); tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6));
		targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntitySlime.class, 20, true, false, target -> ((EntitySlime) target).getSlimeSize() == 1));
	}
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); }
	public int getVariant() { return dataWatcher.getWatchableObjectByte(20) & 3; }
	public void chooseVariant() {
		net.minecraft.world.biome.BiomeGenBase biome = worldObj.getBiomeGenForCoords(MathHelper.floor_double(posX), MathHelper.floor_double(posZ));
		dataWatcher.updateObject(20, (byte) (biome.temperature < 0.2F ? 2 : net.minecraftforge.common.BiomeDictionary.isBiomeOfType(biome, net.minecraftforge.common.BiomeDictionary.Type.HOT) || worldObj.provider.isHellWorld ? 1 : 0));
	}
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { chooseVariant(); return super.onSpawnWithEgg(data); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2); }
	@Override public boolean canBreatheUnderwater() { return true; }
	@Override public boolean isBreedingItem(ItemStack stack) { return !pregnant && stack != null && stack.getItem() == Items.slime_ball; }
	@Override public EntityAgeable createChild(EntityAgeable other) {
		pregnant = true; setGrowingAge(6000); other.setGrowingAge(6000); resetInLove(); ((EntityFrog) other).resetInLove();
		return null; // Pregnancy replaces native direct child spawning; frogspawn owns hatching.
	}
	@Override public boolean attackEntityAsMob(Entity target) {
		if (!(target instanceof EntitySlime) || ((EntitySlime) target).getSlimeSize() != 1) return false;
		boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), 10);
		if (hit && ((EntitySlime) target).getHealth() <= 0 && target instanceof EntityMagmaCube) {
			ModBlocks light = getVariant() == 0 ? ModBlocks.OCHRE_FROGLIGHT : getVariant() == 1 ? ModBlocks.PEARLESCENT_FROGLIGHT : ModBlocks.VERDANT_FROGLIGHT;
			if (light.isEnabled() && worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot")) entityDropItem(light.newItemStack(), 0.1F);
		}
		return hit;
	}
	@Override public void onLivingUpdate() {
		super.onLivingUpdate();
		if (worldObj.isRemote) return;
		if (onGround && getNavigator().getPath() != null && ticksExisted % 40 == 0) jump();
		if (pregnant && ticksExisted % 40 == 0) {
			int bx = MathHelper.floor_double(posX), by = MathHelper.floor_double(posY), bz = MathHelper.floor_double(posZ);
			for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
				int x = bx + dx, z = bz + dz;
				if (!worldObj.blockExists(x, by, z)) continue;
				for (int y = by - 1; y <= by + 1; y++) if (worldObj.isAirBlock(x, y, z) && worldObj.getBlock(x, y - 1, z).getMaterial() == Material.water) {
					if (ModBlocks.FROGSPAWN.isEnabled() && worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing") && worldObj.setBlock(x, y, z, ModBlocks.FROGSPAWN.get(), 0, 2)) { pregnant = false; return; }
				}
			}
		}
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Variant", getVariant()); tag.setBoolean("Pregnant", pregnant); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataWatcher.updateObject(20, (byte)Math.max(0, Math.min(2, tag.getInteger("Variant")))); pregnant = tag.getBoolean("Pregnant"); }
}
