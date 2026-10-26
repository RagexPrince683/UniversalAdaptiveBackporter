package ganymedes01.etfuturum.entities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
public class EntityStatusArrow extends EntityArrow {
	public int effect, duration;
	public EntityStatusArrow(World world) { super(world); }
	public EntityStatusArrow(World world, EntityLivingBase shooter, EntityLivingBase target, int effect, int duration) { super(world, shooter, target, 1.6F, 6); this.effect = effect; this.duration = duration; setDamage(2); }
	@Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("StatusEffect", effect); tag.setInteger("StatusDuration", duration); }
	@Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); effect = tag.getInteger("StatusEffect"); duration = net.minecraft.util.MathHelper.clamp_int(tag.getInteger("StatusDuration"), 0, 1200); }
}
