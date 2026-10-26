package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

public final class ModernFlower extends BlockBush {
    final boolean wither;
    ModernFlower(boolean wither){this.wither=wither;setHardness(0);setSoundType(SoundType.PLANT);}
    @Override public boolean canBlockStay(World world,BlockPos pos,IBlockState state){Block soil=world.getBlockState(pos.down()).getBlock();return wither&&(soil==net.minecraft.init.Blocks.NETHERRACK||soil==net.minecraft.init.Blocks.SOUL_SAND||soil==Content.BLOCKS.get("soul_soil"))||super.canBlockStay(world,pos,state);}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return world.isAirBlock(pos)&&canBlockStay(world,pos,getDefaultState());}
    @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity){if(wither&&!world.isRemote&&world.getDifficulty()!=EnumDifficulty.PEACEFUL&&entity instanceof EntityLivingBase)((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.WITHER,40));}
}
