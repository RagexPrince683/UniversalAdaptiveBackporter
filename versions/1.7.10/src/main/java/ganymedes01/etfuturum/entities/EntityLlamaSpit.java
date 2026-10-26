package ganymedes01.etfuturum.entities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.*;
import net.minecraft.world.World;
public class EntityLlamaSpit extends EntityThrowable {
	public EntityLlamaSpit(World world) { super(world); }
	public EntityLlamaSpit(World world, EntityLivingBase shooter) { super(world, shooter); }
	@Override protected void onImpact(MovingObjectPosition hit) { if (!worldObj.isRemote) { if (hit.entityHit != null) hit.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 1); setDead(); } }
}
