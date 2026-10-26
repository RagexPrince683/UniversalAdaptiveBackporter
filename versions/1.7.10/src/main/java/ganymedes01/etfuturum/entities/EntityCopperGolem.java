package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.world.MobChestIndex;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityCopperGolem extends EntityGolem {
	private ChunkCoordinates destination;
	private int searchCooldown, oxidationTicks = 6000;
	public EntityCopperGolem(World world) { super(world); setSize(.7F, 1.1F); tasks.addTask(0, new EntityAISwimming(this)); tasks.addTask(6, new EntityAIWander(this, .7)); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); dataWatcher.addObject(21, (byte) 0); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(12); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.2); }
	public int getOxidation() { return dataWatcher.getWatchableObjectByte(20); }
	public void setOxidation(int value) { dataWatcher.updateObject(20, (byte) MathHelper.clamp_int(value, 0, 3)); }
	public boolean isWaxed() { return dataWatcher.getWatchableObjectByte(21) != 0; }
	public void wax(boolean value) { dataWatcher.updateObject(21, (byte) (value ? 1 : 0)); }
	@Override public boolean interact(EntityPlayer player) { ItemStack held = player.getHeldItem(); if (held != null && held.getItem() == ModItems.HONEYCOMB.get() && !isWaxed()) { if (!worldObj.isRemote) { wax(true); if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); } return true; } if (held != null && held.getItem() instanceof ItemAxe && (isWaxed() || getOxidation() > 0)) { if (!worldObj.isRemote) { if (isWaxed()) wax(false); else setOxidation(getOxidation() - 1); oxidationTicks = 6000; held.damageItem(1, player); } return true; } return super.interact(player); }
	private boolean accepts(IInventory inventory, ItemStack held, boolean matching) { for (int i = 0; i < inventory.getSizeInventory(); i++) { ItemStack current = inventory.getStackInSlot(i); if (current == null && !matching && inventory.isItemValidForSlot(i, held) || current != null && current.isItemEqual(held) && ItemStack.areItemStackTagsEqual(current, held) && current.stackSize < Math.min(current.getMaxStackSize(), inventory.getInventoryStackLimit())) return true; } return false; }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (!isWaxed() && --oxidationTicks <= 0) { oxidationTicks = 6000 + rand.nextInt(6000); if (rand.nextInt(3) == 0) setOxidation(getOxidation() + 1); }
		if (getOxidation() == 3) { getNavigator().clearPathEntity(); int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ); if (ModBlocks.COPPER_GOLEM_STATUE.isEnabled() && worldObj.isAirBlock(x, y, z) && worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing") && worldObj.setBlock(x, y, z, ModBlocks.COPPER_GOLEM_STATUE.get(), isWaxed() ? 4 : 0, 3)) { if (getHeldItem() != null) { entityDropItem(getHeldItem(), 0); setCurrentItemOrArmor(0, null); } setDead(); } return; }
		if (searchCooldown > 0) { --searchCooldown; return; }
		if (destination == null) {
			double nearest = Double.MAX_VALUE; ItemStack held = getHeldItem();
			for (ChunkCoordinates position : MobChestIndex.nearby(worldObj, posX, posY, posZ)) {
				if (!worldObj.blockExists(position.posX, position.posY, position.posZ)) continue; TileEntity tile = worldObj.getTileEntity(position.posX, position.posY, position.posZ); if (!(tile instanceof IInventory inventory) || !MobChestIndex.chest(tile)) continue;
				boolean copper = tile instanceof ganymedes01.etfuturum.tileentities.TileEntityCopperChest;
				if (held == null) { if (!copper) continue; boolean stocked = false; for (int i = 0; i < inventory.getSizeInventory(); i++) if (inventory.getStackInSlot(i) != null) { stocked = true; break; } if (!stocked) continue; }
				else if (copper || !accepts(inventory, held, false)) continue;
				double distance = getDistanceSq(position.posX + .5, position.posY, position.posZ + .5); if (held != null && accepts(inventory, held, true)) distance *= .1;
				if (distance < nearest) { nearest = distance; destination = position; }
			}
			if (destination == null) { searchCooldown = 100; return; }
		}
		if (getDistanceSq(destination.posX + .5, destination.posY, destination.posZ + .5) > 6) {
			if (ticksExisted % 20 == 0 && !getNavigator().tryMoveToXYZ(destination.posX + .5, destination.posY, destination.posZ + .5, 1)) { destination = null; searchCooldown = 100; }
			return;
		}
		if (worldObj.blockExists(destination.posX, destination.posY, destination.posZ)) {
			TileEntity tile = worldObj.getTileEntity(destination.posX, destination.posY, destination.posZ);
			if (tile instanceof IInventory inventory && MobChestIndex.chest(tile)) {
				ItemStack held = getHeldItem();
				if (held == null && tile instanceof ganymedes01.etfuturum.tileentities.TileEntityCopperChest) { for (int i = 0; i < inventory.getSizeInventory(); i++) if (inventory.getStackInSlot(i) != null) { setCurrentItemOrArmor(0, inventory.decrStackSize(i, Math.min(16, inventory.getStackInSlot(i).stackSize))); inventory.markDirty(); break; } }
				else if (held != null && !(tile instanceof ganymedes01.etfuturum.tileentities.TileEntityCopperChest)) for (int i = 0; i < inventory.getSizeInventory() && held.stackSize > 0; i++) { if (!inventory.isItemValidForSlot(i, held)) continue; ItemStack current = inventory.getStackInSlot(i); int maximum = Math.min(held.getMaxStackSize(), inventory.getInventoryStackLimit()); if (current == null) { ItemStack placed = held.copy(); placed.stackSize = Math.min(maximum, held.stackSize); inventory.setInventorySlotContents(i, placed); held.stackSize -= placed.stackSize; } else if (current.isItemEqual(held) && ItemStack.areItemStackTagsEqual(current, held)) { int moved = Math.min(held.stackSize, Math.max(0, maximum - current.stackSize)); current.stackSize += moved; held.stackSize -= moved; } inventory.markDirty(); } if (held != null && held.stackSize <= 0) setCurrentItemOrArmor(0, null);
			}
		}
		destination = null; searchCooldown = 40;
	}
	@Override protected void dropFewItems(boolean player, int looting) { if (getHeldItem() != null) { entityDropItem(getHeldItem(), 0); setCurrentItemOrArmor(0, null); } if (ModItems.COPPER_INGOT.isEnabled()) entityDropItem(ModItems.COPPER_INGOT.newItemStack(1 + rand.nextInt(3)), 0); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Oxidation", getOxidation()); tag.setBoolean("Waxed", isWaxed()); tag.setInteger("OxidationTicks", oxidationTicks); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); setOxidation(tag.getInteger("Oxidation")); wax(tag.getBoolean("Waxed")); oxidationTicks = tag.hasKey("OxidationTicks") ? MathHelper.clamp_int(tag.getInteger("OxidationTicks"), 1, 12000) : 6000; }

	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.copper_golem.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.copper_golem.death"; }
}

