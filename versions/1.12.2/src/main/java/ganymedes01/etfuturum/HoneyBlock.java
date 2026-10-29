package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class HoneyBlock extends Block {
    HoneyBlock(){super(Material.CLAY);setHardness(0);setSoundType(SoundType.SLIME);setLightOpacity(0);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public boolean isStickyBlock(IBlockState state){return true;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.TRANSLUCENT;}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.0625,0,.0625,.9375,.9375,.9375);}
    @Override public void onFallenUpon(World world,BlockPos pos,Entity entity,float distance){entity.fall(distance,.2F);}
    @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity){if(!entity.onGround&&entity.posY<pos.getY()+.9375-1E-7&&entity.motionY<-.08){double reach=.4375+entity.width*.5;if(Math.abs(pos.getX()+.5-entity.posX)+1E-7>reach||Math.abs(pos.getZ()+.5-entity.posZ)+1E-7>reach){if(entity.motionY<-.13){double scale=-.05/entity.motionY;entity.motionX*=scale;entity.motionZ*=scale;}entity.motionY=-.05;entity.fallDistance=0;}}if(entity.onGround){entity.motionX*=.4;entity.motionZ*=.4;}}
}
