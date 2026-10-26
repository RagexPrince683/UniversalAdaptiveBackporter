package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class AquaticItem extends ItemBlock {
    AquaticItem(Block block) { super(block); }
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos clicked,EnumHand hand,EnumFacing side,float x,float y,float z) {
        ItemStack stack=player.getHeldItem(hand);IBlockState existing=world.getBlockState(clicked);
        if(stack.isEmpty())return EnumActionResult.FAIL;
        if(block instanceof SeaPickle&&existing.getBlock() instanceof SeaPickle&&existing.getValue(SeaPickle.PICKLES)<4&&player.canPlayerEdit(clicked,side,stack)) {
            if(!world.isRemote){world.setBlockState(clicked,existing.withProperty(SeaPickle.PICKLES,existing.getValue(SeaPickle.PICKLES)+1),3);if(!player.capabilities.isCreativeMode)stack.shrink(1);}return EnumActionResult.SUCCESS;
        }
        if(side==EnumFacing.DOWN||stack.isEmpty()||!existing.isSideSolid(world,clicked,side))return EnumActionResult.FAIL;
        BlockPos pos=clicked.offset(side);
        if(pos.getY()<1||pos.getY()>=world.getHeight()||!player.canPlayerEdit(pos,side,stack)||!(world.isAirBlock(pos)||AquaticPlant.sourceWater(world,pos)))return EnumActionResult.FAIL;
        boolean wet=AquaticPlant.sourceWater(world,pos);Block target;
        if(block instanceof Seagrass){if(side!=EnumFacing.UP||!wet||!block.canPlaceBlockAt(world,pos))return EnumActionResult.FAIL;target=block;}
        else if(block instanceof SeaPickle) { if(side!=EnumFacing.UP)return EnumActionResult.FAIL;target=Content.block(wet?"sea_pickle_underwater":"sea_pickle"); }
        else {
            CoralPlant coral=(CoralPlant)block;if(side!=EnumFacing.UP&&!coral.shape.equals("_fan"))return EnumActionResult.FAIL;
            target=Content.block((coral.dead?"dead_":"")+coral.color+"_coral"+(side==EnumFacing.UP?coral.shape:"_wall_fan")+(wet?"_underwater":""));
        }
        IBlockState state=target.getDefaultState();if(target instanceof CoralPlant&&side!=EnumFacing.UP)state=state.withProperty(CoralPlant.FACING,side);
        if(!world.isRemote&&world.setBlockState(pos,state,3)){world.playSound(null,pos,target.getSoundType().getPlaceSound(),SoundCategory.BLOCKS,1,1);if(!player.capabilities.isCreativeMode)stack.shrink(1);}
        return EnumActionResult.SUCCESS;
    }
}
