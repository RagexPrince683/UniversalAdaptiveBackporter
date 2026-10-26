package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/** Server-owned gold admiration and conversion. No item/entity/world work runs on a network thread. */
public class EntityPiglin extends EntityMob implements IRangedAttackMob {
	private int admirationTicks;
	private int unsafeTicks;
	private int angerTicks;
	private UUID angerTarget;

	public EntityPiglin(World world) {
		super(world);
		setSize(0.6F, 1.95F);
		getNavigator().setAvoidsWater(true);
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, new EntityAIAvoidEntity(this, EntityPigZombie.class, 6, 1, 1.2));
		tasks.addTask(1, new EntityAIAvoidEntity(this, EntityPlayer.class, 6, 1, 1.2) {
			@Override public boolean shouldExecute() { return isChild() && super.shouldExecute(); }
		});
		tasks.addTask(2, new EntityAIAttackOnCollide(this, 1, false) { @Override public boolean shouldExecute() { return !hasCrossbow() && !isChild() && super.shouldExecute(); } });
		tasks.addTask(2, new EntityAIArrowAttack(this, 1, 40, 8) { @Override public boolean shouldExecute() { return hasCrossbow() && !isChild() && super.shouldExecute(); } });
		targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityHoglin.class, 80, true, false, entity -> !isChild() && !isBrute() && !((EntityHoglin) entity).isChild()));
		tasks.addTask(5, new EntityAIWander(this, 0.7));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8));
		tasks.addTask(7, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true) {
			@Override public boolean shouldExecute() { return !isChild() && super.shouldExecute(); }
		});
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true, false,
				new IEntitySelector() {
					@Override public boolean isEntityApplicable(Entity entity) {
						return !isChild() && admirationTicks == 0 && (isBrute() || !wearsGold((EntityPlayer) entity));
					}
				}));
		setCurrentItemOrArmor(0, new ItemStack(isBrute() ? Items.golden_axe : Items.golden_sword));
	}

	public boolean isAdmiring() { return (dataWatcher.getWatchableObjectByte(17) & 1) != 0; }
	public boolean isAttacking() { return (dataWatcher.getWatchableObjectByte(17) & 2) != 0; }
	public boolean isBrute() { return false; }
	private boolean hasCrossbow() { return getHeldItem() != null && getHeldItem().getItem() == ModItems.CROSSBOW.get(); }
	@Override public void attackEntityWithRangedAttack(EntityLivingBase target, float distance) { if (!worldObj.isRemote && canEntityBeSeen(target)) { net.minecraft.entity.projectile.EntityArrow arrow = new net.minecraft.entity.projectile.EntityArrow(worldObj, this, target, 1.6F, 6); arrow.setDamage(3); worldObj.spawnEntityInWorld(arrow); playSound("random.bow", 1, .8F); } }
	@Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(16, (byte) 0); dataWatcher.addObject(17, (byte) 0); }
	@Override public boolean isChild() { return dataWatcher.getWatchableObjectByte(16) != 0; }
	@Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		IEntityLivingData result = super.onSpawnWithEgg(data);
		if (!isBrute() && rand.nextBoolean()) setCurrentItemOrArmor(0, ModItems.CROSSBOW.newItemStack());
		if (!isBrute() && rand.nextInt(5) == 0) dataWatcher.updateObject(16, (byte) 1);
		return result;
	}

	@Override protected boolean isAIEnabled() { return true; }

	@Override protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(isBrute() ? 50 : 16);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.35);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(isBrute() ? 7 : 5);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16);
	}

	public static boolean wearsGold(EntityPlayer player) {
		for (ItemStack stack : player.inventory.armorInventory) {
			if (stack != null && stack.getItem() instanceof ItemArmor
					&& ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.GOLD) return true;
		}
		return false;
	}

	public void angerAt(EntityPlayer player) {
		if (!player.capabilities.disableDamage && !ganymedes01.etfuturum.api.spectator.SpectatorUtils.isSpectator(player)) {
			angerTicks = 600;
			angerTarget = player.getUniqueID();
			admirationTicks = 0;
			setAttackTarget(isChild() ? null : player);
		}
	}

	@Override public boolean interact(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if (!isBrute() && !isChild() && admirationTicks == 0 && angerTicks == 0 && held != null && held.getItem() == Items.gold_ingot) {
			if (!worldObj.isRemote) {
				if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				beginAdmiration();
			}
			return true;
		}
		return super.interact(player);
	}

	private void beginAdmiration() {
		admirationTicks = 120;
		setAttackTarget(null);
		getNavigator().clearPathEntity();
	}

	@Override public void onLivingUpdate() {
		super.onLivingUpdate();
		if (worldObj.isRemote) return;
		dataWatcher.updateObject(17, (byte) ((admirationTicks > 0 ? 1 : 0) | (getAttackTarget() != null ? 2 : 0)));
		if (isChild() && width > 0.3F) setSize(0.3F, 0.975F);
		if (angerTicks > 0) {
			angerTicks--;
			if (!isChild() && angerTarget != null && ticksExisted % 20 == 0 && getAttackTarget() == null) for (Object value : worldObj.playerEntities) {
				EntityPlayer player = (EntityPlayer) value;
				if (player.getUniqueID().equals(angerTarget) && !player.capabilities.disableDamage && getDistanceSqToEntity(player) < 256) { setAttackTarget(player); break; }
			}
		} else angerTarget = null;
		if (!isBrute() && angerTicks == 0 && getAttackTarget() instanceof EntityPlayer && wearsGold((EntityPlayer) getAttackTarget())) setAttackTarget(null);
		if (admirationTicks > 0) {
			getNavigator().clearPathEntity();
			setAttackTarget(null);
			if (--admirationTicks == 0) entityDropItem(barter(), 0.5F);
		} else if (!isBrute() && !isChild() && angerTicks == 0 && ticksExisted % 20 == 0 && worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
			List<EntityItem> nearby = worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(1, 0.5, 1));
			for (EntityItem item : nearby) {
				ItemStack stack = item.getEntityItem();
				if (!item.isDead && stack != null && stack.getItem() == Items.gold_ingot) {
					if (stack.stackSize == 1) item.setDead();
					else { stack = stack.copy(); stack.stackSize--; item.setEntityItemStack(stack); }
					beginAdmiration();
					break;
				}
			}
		}
		unsafeTicks = worldObj.provider.isHellWorld ? 0 : unsafeTicks + 1;
		if (unsafeTicks >= 300) {
			EntityPigZombie zombie = new EntityPigZombie(worldObj);
			zombie.copyLocationAndAnglesFrom(this);
			zombie.setChild(isChild());
			if (hasCustomNameTag()) zombie.setCustomNameTag(getCustomNameTag());
			if (getHeldItem() != null) zombie.setCurrentItemOrArmor(0, getHeldItem().copy());
			zombie.func_110163_bv();
			if (worldObj.spawnEntityInWorld(zombie)) setDead();
		}
	}

	private ItemStack barter() {
		// Supported vanilla entries retain their relative weights; disabled EFR items are omitted.
		ItemStack[] entries = {
			new ItemStack(Items.ender_pearl, 2 + rand.nextInt(3)), new ItemStack(Items.string, 3 + rand.nextInt(7)),
			new ItemStack(Items.quartz, 5 + rand.nextInt(8)), new ItemStack(Blocks.obsidian),
			new ItemStack(Items.fire_charge), new ItemStack(Items.leather, 2 + rand.nextInt(3)),
			new ItemStack(Blocks.soul_sand, 2 + rand.nextInt(7)), new ItemStack(Items.netherbrick, 2 + rand.nextInt(7)),
			new ItemStack(Blocks.gravel, 8 + rand.nextInt(9)), new ItemStack(Items.potionitem, 1, 8195),
			new ItemStack(Items.potionitem, 1, 16387), new ItemStack(Items.potionitem, 1, 0)};
		int[] weights = {10, 20, 20, 40, 40, 40, 40, 40, 40, 8, 8, 10};
		int total = 0;
		for (int weight : weights) total += weight;
		int roll = rand.nextInt(total + (ModBlocks.CRYING_OBSIDIAN.isEnabled() ? 40 : 0) + (ModBlocks.BLACKSTONE.isEnabled() ? 40 : 0) + (ModItems.NUGGET_IRON.isEnabled() ? 10 : 0) + (ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions.enableSoulSpeed ? 13 : 0));
		for (int i = 0; i < entries.length; i++) if ((roll -= weights[i]) < 0) return entries[i];
		if (ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions.enableSoulSpeed && (roll -= 13) < 0) {
			int level = 1 + rand.nextInt(3);
			if (roll < -8) return Items.enchanted_book.getEnchantedItemStack(new net.minecraft.enchantment.EnchantmentData(ganymedes01.etfuturum.ModEnchantments.soulSpeed, level));
			ItemStack boots = new ItemStack(Items.iron_boots); boots.addEnchantment(ganymedes01.etfuturum.ModEnchantments.soulSpeed, level); return boots;
		}
		if (ModItems.NUGGET_IRON.isEnabled() && (roll -= 10) < 0) return ModItems.NUGGET_IRON.newItemStack(10 + rand.nextInt(27));
		if (ModBlocks.CRYING_OBSIDIAN.isEnabled() && (roll -= 40) < 0) return ModBlocks.CRYING_OBSIDIAN.newItemStack(1 + rand.nextInt(3));
		return ModBlocks.BLACKSTONE.newItemStack(8 + rand.nextInt(9));
	}

	@Override public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean hit = super.attackEntityFrom(source, amount);
		if (hit && !worldObj.isRemote && source.getEntity() instanceof EntityPlayer) {
			ganymedes01.etfuturum.core.handlers.NetherContentEvents.anger(worldObj, posX, posY, posZ, (EntityPlayer) source.getEntity());
		}
		return hit;
	}

	@Override public boolean getCanSpawnHere() {
		return worldObj.provider.isHellWorld && worldObj.difficultySetting != EnumDifficulty.PEACEFUL
				&& worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
				&& !worldObj.isAnyLiquid(boundingBox);
	}

	@Override public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setInteger("AdmirationTicks", admirationTicks);
		tag.setInteger("UnsafeTicks", unsafeTicks);
		tag.setInteger("GoldAngerTicks", angerTicks);
		tag.setBoolean("IsBaby", isChild());
		if (angerTarget != null) tag.setString("AngerTarget", angerTarget.toString());
	}
	@Override public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		admirationTicks = Math.max(0, Math.min(120, tag.getInteger("AdmirationTicks")));
		unsafeTicks = Math.max(0, Math.min(300, tag.getInteger("UnsafeTicks")));
		angerTicks = Math.max(0, Math.min(600, tag.getInteger("GoldAngerTicks")));
		dataWatcher.updateObject(16, (byte) (tag.getBoolean("IsBaby") ? 1 : 0));
		try { angerTarget = tag.hasKey("AngerTarget") ? UUID.fromString(tag.getString("AngerTarget")) : null; } catch (IllegalArgumentException ignored) { angerTarget = null; }
	}

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.piglin.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.piglin.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.piglin.death"; }
}
