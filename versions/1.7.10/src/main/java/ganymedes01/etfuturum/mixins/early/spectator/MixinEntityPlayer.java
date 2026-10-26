package ganymedes01.etfuturum.mixins.early.spectator;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import ganymedes01.etfuturum.api.spectator.ISpectatorInfo;
import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import ganymedes01.etfuturum.entities.EntityNewBoatWithChest;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayer.class, priority = 1100)
public abstract class MixinEntityPlayer extends EntityLivingBase implements ISpectatorInfo {
	@Unique private static final int ETFU_SPECTATOR_WATCHER = 29;
	@Unique private static final int ETFU_SPECTATOR_EFFECT_DURATION = 400;
	@Unique private static final int ETFU_SPECTATOR_EFFECT_REFRESH = 240;

	@Shadow public InventoryPlayer inventory;

	@Shadow public abstract EntityItem dropPlayerItemWithRandomChoice(ItemStack itemStackIn, boolean p_71019_2_);

	@Shadow
	protected int flyToggleTimer;

	@Shadow
	public PlayerCapabilities capabilities;

	@Shadow
	public abstract void sendPlayerAbilities();

	public MixinEntityPlayer(World p_i1595_1_) {
		super(p_i1595_1_);
	}

	@Inject(method = "entityInit", at = @At("TAIL"))
	private void addSpectatorWatcher(CallbackInfo ci) {
		dataWatcher.addObject(ETFU_SPECTATOR_WATCHER, (byte) 0);
	}

	@Inject(method = "setGameType", at = @At("HEAD"))
	private void dropCarriedItem(WorldSettings.GameType gameType, CallbackInfo ci) {
		if(gameType == SpectatorUtils.SPECTATOR_GAMETYPE) {
			ItemStack stack = inventory.getItemStack(); // Item in cursor
			// Tries to add ItemStack to inventory, else drops it on the ground.
			// This is to prevent spectators from having a cursor item.
			if(stack != null) {
				if(!inventory.addItemStackToInventory(stack)) {
					dropPlayerItemWithRandomChoice(stack, true);
				}
			}
		}
	}

	@Inject(method = "onLivingUpdate", at = @At(value = "HEAD"))
	private void updateSpectator(CallbackInfo ci) {
		if (!worldObj.isRemote) {
			dataWatcher.updateObject(ETFU_SPECTATOR_WATCHER, (byte) (etfu$isServerSpectator() ? 1 : 0));
		}
		etfu$spectatorThisTick = SpectatorUtils.isSpectator((EntityPlayer) (Object) this);
		if (etfu$checkDismountFollowing(etfu$spectatorThisTick)) {
			etfu$followEntity = null;
		}

		if (etfu$spectatorThisTick) {
			//Manage these states while in spectator since we want these states to be forced to specific values in spectator.
			noClip = true;
			onGround = false;
			setInvisible(true);
			if (!worldObj.isRemote) {
				etfu$maintainSpectatorEffect(Potion.nightVision);
				etfu$maintainSpectatorEffect(Potion.invisibility);
			}

			if (ridingEntity != null) {
				dismountEntity(ridingEntity);
				ridingEntity = null;
			}

			if (etfu$followEntity != null) {
				etfu$followEntity((EntityPlayer) (Object) this, etfu$followEntity);
			}

			if (!worldObj.isRemote) {
				if (etfu$followEntity != null) {
					capabilities.allowFlying = capabilities.isFlying = false;
					flyToggleTimer = 7;
				} else {
					capabilities.allowFlying = capabilities.isFlying = true;
					flyToggleTimer = 0;
				}
				sendPlayerAbilities();
			}
		}
	}

	@Unique
    private boolean etfu$checkDismountFollowing(boolean spectator) {
		// Is this player able to continue following the entity? (Not sneaking and is still a spectator)
		if(isSneaking() || !spectator) {
			return true;
		}
		// Is the following entity dead and if it isn't, is it not a spectator?
		return etfu$followEntity != null && (etfu$followEntity.isDead || SpectatorUtils.isSpectator(etfu$followEntity));
	}

	@Inject(method = "onLivingUpdate", at = @At(value = "TAIL"))
	private void updateWasSpectating(CallbackInfo ci) {
		if(etfu$prevSpectator && !etfu$spectatorThisTick) {
			// Do it this way so we don't manage these states for non-spectators, so mods can change it freely.
			// We only need to hold down the fort when the player is actually a spectator.
			noClip = false;
			setInvisible(false);
			if (!worldObj.isRemote) {
				etfu$removeOwnedSpectatorEffect(Potion.nightVision);
				etfu$removeOwnedSpectatorEffect(Potion.invisibility);
				EntityPlayerMP player = (EntityPlayerMP) (Object) this;
				player.theItemInWorldManager.getGameType().configurePlayerCapabilities(capabilities);
				if (!capabilities.allowFlying) capabilities.isFlying = false;
				sendPlayerAbilities();
			}
		}
		etfu$prevSpectator = etfu$spectatorThisTick;
	}

	@Unique
    private static void etfu$followEntity(EntityPlayer player, Entity entity) {
		player.setPosition(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
		player.setRotation(entity.rotationYaw, entity.rotationPitch);
		if(entity instanceof EntityLivingBase living) {
			player.cameraPitch = living.cameraPitch;
			player.setRotationYawHead(living.getRotationYawHead());
			player.rotationYaw = living.getRotationYawHead();
		} else {
			player.setRotationYawHead(entity.rotationYaw);
		}

		player.motionX = entity.motionX;
		player.motionY = entity.motionY;
		player.motionZ = entity.motionZ;
		if (player.isSprinting()) {
			player.setSprinting(false);
		}
	}

	@ModifyReturnValue(method = "getEquipmentInSlot", at = @At(value = "RETURN"))
		public ItemStack getNullEquipmentIfSpectator(ItemStack original) {
        return etfu$isSpectator() ? null : original;
    }

	@ModifyReturnValue(method = "isCurrentToolAdventureModeExempt", at = @At(value = "RETURN"))
	public boolean invalidateSpectatorTools(boolean original) {
		return !etfu$isSpectator() && original;
	}

	@Inject(method = "getBreakSpeed(Lnet/minecraft/block/Block;ZIIII)F", cancellable = true, remap = false,at = @At(value = "HEAD"))
	private void cancelBreakSpeed(Block p_146096_1_, boolean p_146096_2_, int meta, int x, int y, int z, CallbackInfoReturnable<Float> cir) {
		if(etfu$isSpectator()) {
			cir.setReturnValue(0F);
		}
	}

	@Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "HEAD"), cancellable = true)
	private void setFollowing(Entity targetEntity, CallbackInfo ci) {
		if (etfu$isSpectator()) {
			if(etfu$followEntity == null) {
				etfu$followEntity = targetEntity;
			}
			ci.cancel();
		}
	}

	@Inject(method = "interactWith", at = @At(value = "HEAD"), cancellable = true)
    private void cancelInteract(Entity target, CallbackInfoReturnable<Boolean> cir) {
		if (SpectatorUtils.isSpectator(this)) {
			if (!worldObj.isRemote && !(target instanceof EntityPlayer)) {
				if (target instanceof IInvBasic || target instanceof IInventory) {
					etfu$openInv((EntityPlayer) (Object) this, target);
				}
			}
			cir.setReturnValue(false);
		}
	}

	@Unique
    private static void etfu$openInv(EntityPlayer player, Entity target) {
		boolean prevSneaking = player.isSneaking();
		//Special cases for interacting with entities that perform a different action (eg sit)
		//So we do things like make the game think the player is sneaking so it opens the inventory without having the spectator sneak.
		if (target instanceof EntityNewBoatWithChest) {
			player.setSneaking(true); //We need to be sneaking to open the GUI
		} else if (target instanceof EntityHorse) {
			if (((EntityHorse) target).isAdultHorse() && ((EntityHorse) target).isTame()) {
				player.setSneaking(true); //We need to be sneaking to open the GUI
			} else {
				//You can't access the inventory of a non-tamed horse so we skip this one.
				return;
			}
		}
		target.interactFirst(player);
		player.setSneaking(prevSneaking);
	}

	@Unique
	private boolean etfu$prevSpectator = false;
	@Unique
	private boolean etfu$spectatorThisTick = false;
	@Unique
	private Entity etfu$followEntity = null;
	@Unique private PotionEffect etfu$ownedNightVision;
	@Unique private PotionEffect etfu$ownedInvisibility;

	@Unique
	private boolean etfu$isServerSpectator() {
		return (Object) this instanceof net.minecraft.entity.player.EntityPlayerMP player
				&& player.theItemInWorldManager.getGameType() == SpectatorUtils.SPECTATOR_GAMETYPE;
	}

	@Unique
	private void etfu$maintainSpectatorEffect(Potion potion) {
		PotionEffect current = getActivePotionEffect(potion);
		PotionEffect owned = potion == Potion.nightVision ? etfu$ownedNightVision : etfu$ownedInvisibility;
		if (current == owned && current != null
				&& (current.getAmplifier() != 0 || current.getDuration() > ETFU_SPECTATOR_EFFECT_DURATION)) {
			// A stronger/longer application merged into vanilla's mutable PotionEffect; it no longer belongs to us.
			if (potion == Potion.nightVision) etfu$ownedNightVision = null;
			else etfu$ownedInvisibility = null;
			return;
		}
		if (current != null && current != owned) return;
		if (current == null || current.getDuration() <= ETFU_SPECTATOR_EFFECT_REFRESH) {
			addPotionEffect(new PotionEffect(potion.id, ETFU_SPECTATOR_EFFECT_DURATION, 0, true));
			current = getActivePotionEffect(potion);
			if (potion == Potion.nightVision) etfu$ownedNightVision = current;
			else etfu$ownedInvisibility = current;
		}
	}

	@Unique
	private void etfu$removeOwnedSpectatorEffect(Potion potion) {
		PotionEffect owned = potion == Potion.nightVision ? etfu$ownedNightVision : etfu$ownedInvisibility;
		if (owned != null && getActivePotionEffect(potion) == owned) removePotionEffect(potion.id);
		if (potion == Potion.nightVision) etfu$ownedNightVision = null;
		else etfu$ownedInvisibility = null;
	}

	@Override
	public boolean etfu$isSpectator() {
		if (!worldObj.isRemote) return etfu$isServerSpectator();
		return dataWatcher.getWatchableObjectByte(ETFU_SPECTATOR_WATCHER) != 0;
	}

	@Override
	public boolean etfu$wasSpectator() {
		return etfu$prevSpectator;
	}

	@Override
	public Entity etfu$spectatingEntity() {
		return etfu$followEntity;
	}
}
