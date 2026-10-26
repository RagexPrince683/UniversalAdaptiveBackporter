package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityEvoker extends EntityIllager {
	private int spellCooldown, summonCooldown, casting;
	public EntityEvoker(World world) { super(world); tasks.addTask(1, new EntityAIAvoidEntity(this, EntityPlayer.class, 8, .6, 1)); }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (spellCooldown > 0) --spellCooldown; if (summonCooldown > 0) --summonCooldown;
		EntityLivingBase target = getAttackTarget(); if (target == null || !target.isEntityAlive() || !canEntityBeSeen(target)) return;
		if (casting > 0) { getNavigator().clearPathEntity(); if (--casting > 0) return; castFangs(target); }
		if (summonCooldown == 0 && worldObj.getEntitiesWithinAABB(EntityVex.class, boundingBox.expand(16, 8, 16)).size() < 8) {
			summonCooldown = 340;
			for (int i = 0; i < 3; i++) { EntityVex vex = new EntityVex(worldObj); vex.setLocationAndAngles(posX + rand.nextInt(5) - 2, posY + 1, posZ + rand.nextInt(5) - 2, 0, 0); vex.setOwner(this); vex.setAttackTarget(target); vex.setLifetime(600 + rand.nextInt(1800)); worldObj.spawnEntityInWorld(vex); }
		} else if (spellCooldown == 0) { spellCooldown = 100; casting = 20; }
	}
	private void castFangs(EntityLivingBase target) {
		double angle = Math.atan2(target.posZ - posZ, target.posX - posX);
		for (int i = 0; i < 16; i++) {
			double x = posX + Math.cos(angle) * (i + 1) * 1.25, z = posZ + Math.sin(angle) * (i + 1) * 1.25;
			int bx = MathHelper.floor_double(x), bz = MathHelper.floor_double(z), top = MathHelper.floor_double(Math.max(target.posY, posY) + 1), low = Math.max(0, MathHelper.floor_double(Math.min(target.posY, posY)) - 2);
			for (int y = top; y >= low; y--) { if (!worldObj.blockExists(bx, y, bz)) break; if (worldObj.getBlock(bx, y - 1, bz).isNormalCube() && !worldObj.getBlock(bx, y, bz).isNormalCube()) { worldObj.spawnEntityInWorld(new EntityEvokerFangs(worldObj, this, x, y, z, i * 2)); break; } }
		}
	}
	@Override protected void dropFewItems(boolean player, int looting) { super.dropFewItems(player, looting); if (ModItems.TOTEM_OF_UNDYING.isEnabled()) entityDropItem(ModItems.TOTEM_OF_UNDYING.newItemStack(), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("SpellCooldown", spellCooldown); tag.setInteger("SummonCooldown", summonCooldown); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); spellCooldown = MathHelper.clamp_int(tag.getInteger("SpellCooldown"), 0, 100); summonCooldown = MathHelper.clamp_int(tag.getInteger("SummonCooldown"), 0, 340); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.evoker.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.evoker.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.evoker.death"; }
}
