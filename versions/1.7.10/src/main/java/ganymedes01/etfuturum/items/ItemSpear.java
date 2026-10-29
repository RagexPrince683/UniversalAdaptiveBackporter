package ganymedes01.etfuturum.items;
import ganymedes01.etfuturum.EtFuturum;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
/** A held thrust uses the server's look ray and relative movement, without changing global player reach. */
public class ItemSpear extends ItemSword {
	private final float thrustDamage;
	public ItemSpear(ToolMaterial material, String name) { super(material); thrustDamage = 3 + material.getDamageVsEntity(); setUnlocalizedName("etfuturum." + name); setTextureName(name); setCreativeTab(EtFuturum.creativeTabItems); }
	@Override public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity target) {
		if (player.worldObj.isRemote) EtFuturum.proxy.spearLunge(player);
		return false;
	}
	@Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.bow; }
	@Override public int getMaxItemUseDuration(ItemStack stack) { return 72000; }
	@Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) { player.setItemInUse(stack, getMaxItemUseDuration(stack)); return stack; }
	@Override public void onUsingTick(ItemStack stack, EntityPlayer player, int remaining) {
		int used = getMaxItemUseDuration(stack) - remaining; if (player.worldObj.isRemote || used < 10 || used % 20 != 10) return;
		Vec3 eye = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ), direction = player.getLookVec(), end = eye.addVector(direction.xCoord * 4.5, direction.yCoord * 4.5, direction.zCoord * 4.5);
		MovingObjectPosition block = player.worldObj.func_147447_a(Vec3.createVectorHelper(eye.xCoord, eye.yCoord, eye.zCoord), end, false, true, false); double distance = block == null ? 4.5 : eye.distanceTo(block.hitVec); EntityLivingBase nearest = null;
		for (Object object : player.worldObj.getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.addCoord(direction.xCoord * 4.5, direction.yCoord * 4.5, direction.zCoord * 4.5).expand(.5, .5, .5))) {
			if (!(object instanceof EntityLivingBase target) || !target.canBeCollidedWith() || target == player.ridingEntity) continue;
			MovingObjectPosition hit = target.boundingBox.expand(.2, .2, .2).calculateIntercept(eye, end); if (hit == null) continue; double length = eye.distanceTo(hit.hitVec); if (length >= 2 && length < distance) { nearest = target; distance = length; }
		}
		if (nearest != null) { double relative = (player.motionX - nearest.motionX) * direction.xCoord + (player.motionY - nearest.motionY) * direction.yCoord + (player.motionZ - nearest.motionZ) * direction.zCoord; if (player.ridingEntity != null) relative += player.ridingEntity.motionX * direction.xCoord + player.ridingEntity.motionZ * direction.zCoord; float damage = thrustDamage + (float) MathHelper.clamp_double(relative * 12, 0, 20); if (nearest.attackEntityFrom(DamageSource.causePlayerDamage(player), damage)) { stack.damageItem(1, player); nearest.addVelocity(direction.xCoord * .3, .1, direction.zCoord * .3); nearest.velocityChanged = true; } }
	}
}
