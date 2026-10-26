package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.UUID;

public class EntityLlama extends EntityAnimal implements IInventory {
	private final InventoryBasic storage = new InventoryBasic("Llama", false, 15);
	private UUID owner;
	private int tameTicks, spitTicks;
	private EntityLlama caravanHead;
	public EntityLlama(World world) { super(world); setSize(.9F, 1.87F); tasks.addTask(0, new EntityAISwimming(this)); tasks.addTask(1, new EntityAIPanic(this, 1.2)); tasks.addTask(2, new EntityAIMate(this, 1)); tasks.addTask(3, new EntityAITempt(this, 1.2, Item.getItemFromBlock(Blocks.hay_block), false)); tasks.addTask(4, new EntityAIFollowParent(this, 1)); tasks.addTask(5, new EntityAIWander(this, .7)); targetTasks.addTask(1, new EntityAIHurtByTarget(this, false)); targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityWolf.class, 40, true, false, entity -> !((EntityWolf) entity).isTamed())); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); dataWatcher.addObject(21, (byte) 1); dataWatcher.addObject(22, (byte) -1); dataWatcher.addObject(23, (byte) 0); }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.175); }
	public int getVariant() { return dataWatcher.getWatchableObjectByte(20) & 3; }
	public int getStrength() { return dataWatcher.getWatchableObjectByte(21); }
	public int getDecor() { return dataWatcher.getWatchableObjectByte(22); }
	public boolean hasChest() { return dataWatcher.getWatchableObjectByte(23) != 0; }
	public boolean isTamed() { return owner != null; }
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { super.onSpawnWithEgg(data); dataWatcher.updateObject(20, (byte) rand.nextInt(4)); dataWatcher.updateObject(21, (byte) (1 + rand.nextInt(rand.nextFloat() < .04F ? 5 : 3))); return data; }
	@Override public boolean isBreedingItem(ItemStack stack) { return isTamed() && stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.hay_block); }
	@Override public EntityAgeable createChild(EntityAgeable other) { EntityLlama child = new EntityLlama(worldObj); child.dataWatcher.updateObject(20, (byte) (rand.nextBoolean() ? getVariant() : ((EntityLlama) other).getVariant())); int strength = 1 + rand.nextInt(Math.max(getStrength(), ((EntityLlama) other).getStrength())); if (rand.nextFloat() < .03F) strength++; child.dataWatcher.updateObject(21, (byte) Math.min(5, strength)); return child; }
	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (held != null && (held.getItem() == Items.wheat || held.getItem() == Item.getItemFromBlock(Blocks.hay_block))) { if (isBreedingItem(held) && !isChild() && getGrowingAge() == 0) return super.interact(player); if (!worldObj.isRemote) { heal(held.getItem() == Items.wheat ? 2 : 10); if (isChild()) setGrowingAge(Math.min(0, getGrowingAge() + 200)); consume(player, held); } return true; }
		if (isTamed() && held != null && held.getItem() == Item.getItemFromBlock(Blocks.chest) && !hasChest()) { if (!worldObj.isRemote) { dataWatcher.updateObject(23, (byte) 1); consume(player, held); } return true; }
		if (isTamed() && held != null && held.getItem() == Item.getItemFromBlock(Blocks.carpet)) { if (!worldObj.isRemote) { if (getDecor() >= 0) entityDropItem(new ItemStack(Blocks.carpet, 1, getDecor()), 0); dataWatcher.updateObject(22, (byte) (held.getItemDamage() & 15)); consume(player, held); } return true; }
		if (isTamed() && hasChest() && player.isSneaking()) { if (!worldObj.isRemote) player.openGui(ganymedes01.etfuturum.EtFuturum.instance, ganymedes01.etfuturum.lib.GUIIDs.LLAMA, worldObj, getEntityId(), 0, 0); return true; }
		if (!isChild() && riddenByEntity == null && !player.isSneaking()) { if (!worldObj.isRemote) player.mountEntity(this); return true; }
		return super.interact(player);
	}
	private void consume(EntityPlayer player, ItemStack stack) { if (!player.capabilities.isCreativeMode && --stack.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote) return;
		if (riddenByEntity instanceof EntityPlayer player && !isTamed() && ++tameTicks >= 100) { if (rand.nextInt(3) == 0) { owner = player.getUniqueID(); func_110163_bv(); worldObj.setEntityState(this, (byte) 7); } else player.mountEntity(null); tameTicks = 0; }
		if (spitTicks > 0) --spitTicks;
		EntityLivingBase target = getAttackTarget();
		if (target != null && target.isEntityAlive() && spitTicks == 0 && getDistanceSqToEntity(target) < 400 && canEntityBeSeen(target)) { EntityLlamaSpit spit = new EntityLlamaSpit(worldObj, this); double dx = target.posX - posX, dz = target.posZ - posZ; spit.setThrowableHeading(dx, target.posY + target.height / 3 - spit.posY + Math.sqrt(dx * dx + dz * dz) * .2, dz, 1.5F, 10); worldObj.spawnEntityInWorld(spit); spitTicks = 40; if (!(target instanceof EntityWolf)) setAttackTarget(null); }
		if (getLeashed()) caravanHead = null;
		else if (ticksExisted % 40 == 0) { caravanHead = null; for (Object object : worldObj.getEntitiesWithinAABB(EntityLlama.class, boundingBox.expand(12, 6, 12))) { EntityLlama candidate = (EntityLlama) object; if (candidate != this && candidate.getEntityId() < getEntityId() && (candidate.getLeashed() || candidate.caravanHead != null) && (caravanHead == null || getDistanceSqToEntity(candidate) < getDistanceSqToEntity(caravanHead))) caravanHead = candidate; } }
		if (caravanHead != null) { if (!caravanHead.isEntityAlive() || getDistanceSqToEntity(caravanHead) > 676) caravanHead = null; else if (ticksExisted % 10 == 0 && getDistanceSqToEntity(caravanHead) > 4) getNavigator().tryMoveToEntityLiving(caravanHead, 1.3); }
	}
	@Override protected void dropFewItems(boolean player, int looting) { int leather = rand.nextInt(3) + rand.nextInt(looting + 1); if (leather > 0) dropItem(Items.leather, leather); if (hasChest()) dropItem(Item.getItemFromBlock(Blocks.chest), 1); if (getDecor() >= 0) entityDropItem(new ItemStack(Blocks.carpet, 1, getDecor()), 0); for (int i = 0; i < storage.getSizeInventory(); i++) if (storage.getStackInSlot(i) != null) { entityDropItem(storage.getStackInSlot(i), 0); storage.setInventorySlotContents(i, null); } }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Variant", getVariant()); tag.setInteger("Strength", getStrength()); tag.setInteger("Decor", getDecor()); tag.setBoolean("ChestedHorse", hasChest()); if (owner != null) tag.setString("OwnerUUID", owner.toString()); NBTTagList list = new NBTTagList(); for (int i = 0; i < 15; i++) if (storage.getStackInSlot(i) != null) { NBTTagCompound entry = storage.getStackInSlot(i).writeToNBT(new NBTTagCompound()); entry.setByte("Slot", (byte) i); list.appendTag(entry); } tag.setTag("Items", list); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); dataWatcher.updateObject(20, (byte) (tag.getInteger("Variant") & 3)); dataWatcher.updateObject(21, (byte) MathHelper.clamp_int(tag.getInteger("Strength"), 1, 5)); dataWatcher.updateObject(22, (byte) MathHelper.clamp_int(tag.hasKey("Decor") ? tag.getInteger("Decor") : -1, -1, 15)); dataWatcher.updateObject(23, (byte) (tag.getBoolean("ChestedHorse") ? 1 : 0)); try { owner = UUID.fromString(tag.getString("OwnerUUID")); } catch (IllegalArgumentException ignored) { owner = null; } NBTTagList list = tag.getTagList("Items", 10); for (int i = 0; i < Math.min(15, list.tagCount()); i++) { NBTTagCompound entry = list.getCompoundTagAt(i); int slot = entry.getByte("Slot") & 255; if (slot < 15) storage.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(entry)); } }
	@Override public int getSizeInventory() { return hasChest() ? getStrength() * 3 : 0; }
	@Override public ItemStack getStackInSlot(int slot) { return storage.getStackInSlot(slot); }
	@Override public ItemStack decrStackSize(int slot, int count) { return storage.decrStackSize(slot, count); }
	@Override public ItemStack getStackInSlotOnClosing(int slot) { return storage.getStackInSlotOnClosing(slot); }
	@Override public void setInventorySlotContents(int slot, ItemStack stack) { storage.setInventorySlotContents(slot, stack); }
	@Override public String getInventoryName() { return hasCustomNameTag() ? getCustomNameTag() : "Llama"; }
	@Override public boolean hasCustomInventoryName() { return hasCustomNameTag(); }
	@Override public int getInventoryStackLimit() { return 64; }
	@Override public void markDirty() { storage.markDirty(); }
	@Override public boolean isUseableByPlayer(EntityPlayer player) { return isEntityAlive() && isTamed() && hasChest() && getDistanceSqToEntity(player) <= 64; }
	@Override public void openInventory() { }
	@Override public void closeInventory() { }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot >= 0 && slot < getSizeInventory(); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.llama.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.llama.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.llama.death"; }
}
