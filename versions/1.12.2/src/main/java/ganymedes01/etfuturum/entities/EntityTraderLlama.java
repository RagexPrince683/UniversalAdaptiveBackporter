package ganymedes01.etfuturum.entities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Native taming, carpets, chests, caravan navigation and spit remain owned by Forge's llama. */
public class EntityTraderLlama extends net.minecraft.entity.passive.EntityLlama {
    private int despawnDelay=48000;
    public EntityTraderLlama(World world){super(world);}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(!world.isRemote&&!isTame()&&!(getLeashHolder() instanceof EntityPlayer)&&--despawnDelay<=0)setDead();}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("DespawnDelay",despawnDelay);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);despawnDelay=tag.hasKey("DespawnDelay")?net.minecraft.util.math.MathHelper.clamp(tag.getInteger("DespawnDelay"),0,48000):48000;}
}
