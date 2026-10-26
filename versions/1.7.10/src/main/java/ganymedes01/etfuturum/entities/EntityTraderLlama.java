package ganymedes01.etfuturum.entities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
public class EntityTraderLlama extends EntityLlama {
	private int despawnDelay = 48000;
	public EntityTraderLlama(World world) { super(world); }
	@Override public void onLivingUpdate() { super.onLivingUpdate(); if (!worldObj.isRemote && !isTamed() && !(getLeashedToEntity() instanceof net.minecraft.entity.player.EntityPlayer) && --despawnDelay <= 0) setDead(); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("DespawnDelay", despawnDelay); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); despawnDelay = tag.hasKey("DespawnDelay") ? net.minecraft.util.MathHelper.clamp_int(tag.getInteger("DespawnDelay"), 0, 48000) : 48000; }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.llama.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.llama.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.llama.death"; }
}
