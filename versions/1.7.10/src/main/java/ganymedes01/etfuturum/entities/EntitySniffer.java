package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import java.util.*;

public class EntitySniffer extends EntityAnimal {
	private final Deque<Long> explored = new ArrayDeque<>();
	private int digCooldown = 1200, digging;
	public EntitySniffer(World world) { super(world); setSize(1.9F, 1.75F); tasks.addTask(0, new EntityAISwimming(this)); tasks.addTask(1, new EntityAIPanic(this, 1.3)); tasks.addTask(2, new EntityAIMate(this, 1)); tasks.addTask(3, new EntityAITempt(this, 1.1, ModItems.TORCHFLOWER_SEEDS.get(), false)); tasks.addTask(4, new EntityAIFollowParent(this, 1)); tasks.addTask(5, new EntityAIWander(this, .6)); }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(14); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.1); }
	public boolean isDigging() { return dataWatcher.getWatchableObjectByte(20) != 0; }
	@Override public boolean isBreedingItem(ItemStack stack) { return stack != null && stack.getItem() == ModItems.TORCHFLOWER_SEEDS.get(); }
	@Override public EntityAgeable createChild(EntityAgeable mate) { ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, ganymedes01.etfuturum.ModBlocks.SNIFFER_EGG.newItemStack(), .1F); setGrowingAge(6000); mate.setGrowingAge(6000); resetInLove(); ((EntityAnimal) mate).resetInLove(); return null; }
	private long location(int x, int y, int z) { return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 4095); }
	@Override public void onLivingUpdate() {
		super.onLivingUpdate(); if (worldObj.isRemote || isChild()) return;
		if (digCooldown > 0) --digCooldown;
		if (digging > 0) {
			getNavigator().clearPathEntity(); motionX = motionZ = 0;
			if (--digging == 0) { int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY) - 1, z = MathHelper.floor_double(posZ); if (worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) { ganymedes01.etfuturum.configuration.ContentAvailability.drop(this, (rand.nextBoolean() ? ModItems.TORCHFLOWER_SEEDS : ModItems.PITCHER_POD).newItemStack(), .1F); explored.addLast(location(x, y, z)); while (explored.size() > 20) explored.removeFirst(); } digCooldown = 9600; dataWatcher.updateObject(20, (byte) 0); }
			return;
		}
		if (digCooldown == 0 && ticksExisted % 40 == 0 && onGround && !isInWater()) {
			int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY) - 1, z = MathHelper.floor_double(posZ); net.minecraft.block.Block ground = worldObj.getBlock(x, y, z);
			if ((ground == Blocks.grass || ground == Blocks.dirt || ground == ganymedes01.etfuturum.ModBlocks.MOSS_BLOCK.get()) && !explored.contains(location(x, y, z))) { digging = 120; dataWatcher.updateObject(20, (byte) 1); }
		}
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("DigCooldown", digCooldown); NBTTagList list = new NBTTagList(); for (Long position : explored) { NBTTagCompound entry = new NBTTagCompound(); entry.setLong("Pos", position); list.appendTag(entry); } tag.setTag("Explored", list); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); digCooldown = tag.hasKey("DigCooldown") ? MathHelper.clamp_int(tag.getInteger("DigCooldown"), 0, 9600) : 1200; explored.clear(); NBTTagList list = tag.getTagList("Explored", 10); for (int i = 0; i < Math.min(20, list.tagCount()); i++) explored.add(list.getCompoundTagAt(i).getLong("Pos")); }

	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.sniffer.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.sniffer.death"; }
}
