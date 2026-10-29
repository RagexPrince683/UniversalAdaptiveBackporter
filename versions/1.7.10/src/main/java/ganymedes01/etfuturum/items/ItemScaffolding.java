package ganymedes01.etfuturum.items;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
/** Column and bridge placement delegates the final edit to native ItemBlock protection hooks. */
public final class ItemScaffolding extends ItemBlock {
    public ItemScaffolding(Block block){super(block);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float a,float b,float c){
        if(world.getBlock(x,y,z)!=field_150939_a)return super.onItemUse(stack,player,world,x,y,z,side,a,b,c);
        ForgeDirection direction=player.isSneaking()?ForgeDirection.getOrientation(side):side==1?ForgeDirection.getOrientation(new int[]{3,4,2,5}[MathHelper.floor_double(player.rotationYaw*4/360+.5)&3]):ForgeDirection.UP;
        for(int step=0;step<world.getHeight();step++){
            x+=direction.offsetX;y+=direction.offsetY;z+=direction.offsetZ;
            if(y<0||y>=world.getHeight()||!world.blockExists(x,y,z)||direction.offsetY==0&&step>=6)return false;
            Block target=world.getBlock(x,y,z);if(target==field_150939_a)continue;if(!target.isReplaceable(world,x,y,z))return false;
            return super.onItemUse(stack,player,world,x,y,z,1,a,b,c);
        }return false;
    }
}
