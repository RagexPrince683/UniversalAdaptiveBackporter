package ganymedes01.etfuturum.entities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
public class EntityBogged extends EntityStatusSkeleton {
    private static final DataParameter<Boolean> SHEARED=EntityDataManager.createKey(EntityBogged.class,DataSerializers.BOOLEAN);
    public EntityBogged(World world){super(world);}
    @Override protected void entityInit(){super.entityInit();dataManager.register(SHEARED,false);}
    @Override protected Potion effect(){return MobEffects.POISON;}
    @Override protected int duration(){return 100;}
    @Override protected String kind(){return "bogged";}
    public boolean isSheared(){return dataManager.get(SHEARED);}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);if(held.getItem()==Items.SHEARS&&!isSheared()){if(!world.isRemote){dataManager.set(SHEARED,true);entityDropItem(new ItemStack(Blocks.RED_MUSHROOM),0);entityDropItem(new ItemStack(Blocks.BROWN_MUSHROOM),0);held.damageItem(1,player);}return true;}return super.processInteract(player,hand);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("Sheared",isSheared());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);dataManager.set(SHEARED,tag.getBoolean("Sheared"));}
}
