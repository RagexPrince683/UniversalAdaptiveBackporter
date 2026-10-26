package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.UUID;

public class EntityAllay extends EntityCreature {
	private UUID owner;
	private ItemStack collected;
	private EntityItem wanted;
	private int duplicationCooldown, deliveryTicks, noteTicks;
	private int noteX, noteY, noteZ;
	public EntityAllay(World world) { super(world); setSize(.35F, .6F); }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected boolean canDespawn() { return false; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.1); }
	public boolean isDancing() { return dataWatcher.getWatchableObjectByte(20) != 0; }
	public void hearNote(int x, int y, int z) { noteX = x; noteY = y; noteZ = z; noteTicks = 600; }
	@Override protected void fall(float distance) { }
	@Override public boolean attackEntityFrom(DamageSource source, float damage) { return !(source.getEntity() instanceof EntityPlayer player && owner != null && owner.equals(player.getUniqueID())) && super.attackEntityFrom(source, damage); }
	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (held != null && held.getItem() == ModItems.AMETHYST_SHARD.get() && isDancing() && duplicationCooldown == 0) {
			if (!worldObj.isRemote) { EntityAllay copy = new EntityAllay(worldObj); copy.copyLocationAndAnglesFrom(this); if (worldObj.spawnEntityInWorld(copy)) { duplicationCooldown = copy.duplicationCooldown = 6000; consume(player, held); worldObj.setEntityState(this, (byte) 7); } } return true;
		}
		if (getHeldItem() == null && held != null) { if (!worldObj.isRemote) { ItemStack sample = held.copy(); sample.stackSize = 1; setCurrentItemOrArmor(0, sample); owner = player.getUniqueID(); consume(player, held); } return true; }
		if (held == null && getHeldItem() != null) { if (!worldObj.isRemote) { ItemStack sample = getHeldItem(); setCurrentItemOrArmor(0, null); if (!player.inventory.addItemStackToInventory(sample)) player.dropPlayerItemWithRandomChoice(sample, false); if (collected != null) { entityDropItem(collected, 0); collected = null; } owner = null; } return true; }
		return super.interact(player);
	}
	private void consume(EntityPlayer player, ItemStack held) { if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (duplicationCooldown > 0) --duplicationCooldown; if (noteTicks > 0) --noteTicks; if (deliveryTicks > 0) --deliveryTicks;
		if (ticksExisted % 20 == 0) { heal(1); wanted = null;
			if (getHeldItem() != null && (collected == null || collected.stackSize < collected.getMaxStackSize()) && worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) for (Object object : worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(16, 8, 16))) { EntityItem item = (EntityItem) object; ItemStack stack = item.getEntityItem(); if (stack != null && item.delayBeforeCanPickup <= 0 && stack.getItem() == getHeldItem().getItem() && stack.getItemDamage() == getHeldItem().getItemDamage() && (collected == null || ItemStack.areItemStackTagsEqual(collected, stack)) && (wanted == null || getDistanceSqToEntity(item) < getDistanceSqToEntity(wanted))) wanted = item; }
		}
		if (ticksExisted % 40 == 0) { boolean dancing = false; int bx = MathHelper.floor_double(posX), by = MathHelper.floor_double(posY), bz = MathHelper.floor_double(posZ); search: for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) for (int dy = -2; dy <= 2; dy++) { int x = bx + dx, y = by + dy, z = bz + dz; if (worldObj.blockExists(x, y, z) && worldObj.getBlock(x, y, z) == Blocks.jukebox && worldObj.getBlockMetadata(x, y, z) > 0) { dancing = true; break search; } } dataWatcher.updateObject(20, (byte) (dancing ? 1 : 0)); }
	}
	@Override public void moveEntityWithHeading(float strafe, float forward) {
		if (worldObj.isRemote) return;
		EntityPlayer player = owner == null ? null : worldObj.func_152378_a(owner);
		double x = player == null ? posX + Math.sin(ticksExisted * .03) : player.posX, y = player == null ? posY + Math.sin(ticksExisted * .02) * .2 : player.posY + 1.5, z = player == null ? posZ + Math.cos(ticksExisted * .03) : player.posZ;
		if (noteTicks > 0) { x = noteX + .5; y = noteY + 1.2; z = noteZ + .5; }
		if (wanted != null && wanted.isEntityAlive()) {
			x = wanted.posX; y = wanted.posY + .2; z = wanted.posZ;
			if (getDistanceSqToEntity(wanted) < 1.5) { ItemStack stack = wanted.getEntityItem(); int count = Math.min(stack.stackSize, collected == null ? stack.getMaxStackSize() : collected.getMaxStackSize() - collected.stackSize); if (count > 0) { if (collected == null) { collected = stack.copy(); collected.stackSize = count; } else collected.stackSize += count; ItemStack remainder = stack.copy(); remainder.stackSize -= count; if (remainder.stackSize <= 0) wanted.setDead(); else wanted.setEntityItemStack(remainder); } wanted = null; }
		}
		else if (collected != null && deliveryTicks == 0 && (player != null || noteTicks > 0) && getDistanceSq(x, y, z) < 9) { EntityItem item = entityDropItem(collected, .2F); if (item != null) item.delayBeforeCanPickup = 60; collected = null; deliveryTicks = 60; }
		if (!isDancing() && worldObj.blockExists(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z))) { double dx = x - posX, dy = y - posY, dz = z - posZ, length = Math.sqrt(dx * dx + dy * dy + dz * dz); if (length > 1) { motionX += (dx / length * .25 - motionX) * .15; motionY += (dy / length * .25 - motionY) * .15; motionZ += (dz / length * .25 - motionZ) * .15; rotationYaw = (float) (Math.atan2(dz, dx) * 180 / Math.PI) - 90; renderYawOffset = rotationYaw; } }
		if (isCollidedHorizontally) motionY += .05;
		moveEntity(motionX, motionY, motionZ); motionX *= .9; motionY *= .9; motionZ *= .9;
	}
	@Override protected void dropFewItems(boolean player, int looting) { if (collected != null) { entityDropItem(collected, 0); collected = null; } }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); if (owner != null) tag.setString("LikedPlayer", owner.toString()); if (collected != null) tag.setTag("Inventory", collected.writeToNBT(new NBTTagCompound())); tag.setInteger("DuplicationCooldown", duplicationCooldown); tag.setInteger("NoteTicks", noteTicks); tag.setInteger("NoteX", noteX); tag.setInteger("NoteY", noteY); tag.setInteger("NoteZ", noteZ); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); try { owner = UUID.fromString(tag.getString("LikedPlayer")); } catch (IllegalArgumentException ignored) { owner = null; } collected = tag.hasKey("Inventory", 10) ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Inventory")) : null; duplicationCooldown = MathHelper.clamp_int(tag.getInteger("DuplicationCooldown"), 0, 6000); noteTicks = MathHelper.clamp_int(tag.getInteger("NoteTicks"), 0, 600); noteX = tag.getInteger("NoteX"); noteY = tag.getInteger("NoteY"); noteZ = tag.getInteger("NoteZ"); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.allay.ambient_without_item"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.allay.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.allay.death"; }
}
