package ganymedes01.etfuturum.entities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Separate egg/command identity; the existing guardian owns elder AI, rendering and drops. */
public final class EntityElderGuardian extends EntityGuardian {
    public EntityElderGuardian(World world) { super(world); setElder(true); }
    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        float health = getHealth(); setElder(true); setHealth(health);
    }
}
