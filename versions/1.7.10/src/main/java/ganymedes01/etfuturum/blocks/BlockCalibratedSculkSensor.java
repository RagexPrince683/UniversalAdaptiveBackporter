package ganymedes01.etfuturum.blocks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
public final class BlockCalibratedSculkSensor extends BlockSculkSensor {
 public BlockCalibratedSculkSensor(){super("calibrated_sculk_sensor","calibrated_sculk_sensor_inactive","calibrated_sculk_sensor_active");}
 @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase entity,ItemStack stack){world.setBlockMetadataWithNotify(x,y,z,MathHelper.floor_double(entity.rotationYaw*4/360+.5)&3,3);}
}
