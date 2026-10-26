package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.blocks.BlockCreakingHeart;
import ganymedes01.etfuturum.tileentities.TileEntityCreakingHeart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityCreaking extends EntityMob {
	private int heartX, heartY = -1, heartZ;
	private boolean watched;
	public EntityCreaking(World world) {
		super(world); setSize(0.9F, 2.7F); isImmuneToFire = true; experienceValue = 0; func_110163_bv();
		tasks.addTask(1, new EntityAIAttackOnCollide(this, 1, true));
		tasks.addTask(6, new EntityAIWander(this, 0.7));
		targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true));
	}
	public void bind(int x, int y, int z) { heartX = x; heartY = y; heartZ = z; }
	@Override public boolean canBePushed() { return false; }
	public boolean belongsTo(int x, int y, int z) { return heartX == x && heartY == y && heartZ == z; }
	@Override protected boolean isAIEnabled() { return true; }
	@Override protected void applyEntityAttributes() {
		super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(1); getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.4);
	}
	@Override public void onLivingUpdate() {
		if (!worldObj.isRemote && ticksExisted % 5 == 0) {
			if (heartY >= 0) {
				if (!worldObj.blockExists(heartX, heartY, heartZ)) { motionX = motionZ = 0; return; }
				if (!BlockCreakingHeart.isActive(worldObj, heartX, heartY, heartZ) || getDistanceSq(heartX + 0.5, heartY + 0.5, heartZ + 0.5) > 32 * 32) { setDead(); return; }
			}
			watched = false;
			for (Object value : worldObj.playerEntities) {
				EntityPlayer player = (EntityPlayer) value;
				if (player.isDead || ganymedes01.etfuturum.api.spectator.SpectatorUtils.isSpectator(player) || getDistanceSqToEntity(player) > 32 * 32) continue;
				if (player.getEquipmentInSlot(4) != null && player.getEquipmentInSlot(4).getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.pumpkin)) continue;
				Vec3 toward = Vec3.createVectorHelper(posX - player.posX, posY + height * 0.75 - player.posY - player.getEyeHeight(), posZ - player.posZ);
				double distance = toward.lengthVector();
				if (distance > 0 && player.getLook(1).dotProduct(toward.normalize()) > 1 - 0.5 / distance && player.canEntityBeSeen(this)) { watched = true; break; }
			}
		}
		super.onLivingUpdate();
	}
	@Override protected void updateAITasks() {
		if (watched) { getNavigator().clearPathEntity(); motionX = motionZ = 0; setAttackTarget(null); }
		else super.updateAITasks();
	}
	@Override public boolean attackEntityFrom(DamageSource source, float amount) {
		if (heartY < 0 || source == DamageSource.outOfWorld) return super.attackEntityFrom(source, amount);
		if (!worldObj.isRemote && worldObj.blockExists(heartX, heartY, heartZ)) {
			TileEntity tile = worldObj.getTileEntity(heartX, heartY, heartZ);
			if (tile instanceof TileEntityCreakingHeart && source.getEntity() instanceof EntityPlayer) ((TileEntityCreakingHeart) tile).createResin();
		}
		return false;
	}
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("HeartX", heartX); tag.setInteger("HeartY", heartY); tag.setInteger("HeartZ", heartZ); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); heartX = tag.getInteger("HeartX"); heartY = tag.hasKey("HeartY") ? tag.getInteger("HeartY") : -1; heartZ = tag.getInteger("HeartZ"); }
}
