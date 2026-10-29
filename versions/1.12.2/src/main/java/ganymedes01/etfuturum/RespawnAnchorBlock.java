package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.dispenser.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.*;

/** Native block states and interactions; respawn integration is owned by RespawnAnchors. */
public final class RespawnAnchorBlock extends Block {
    public static final PropertyInteger CHARGES=PropertyInteger.create("charges",0,4);
    RespawnAnchorBlock(){super(Material.ROCK);setHardness(50);setResistance(1200);setHarvestLevel("pickaxe",3);setDefaultState(blockState.getBaseState().withProperty(CHARGES,0));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,CHARGES);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(CHARGES,Math.min(4,meta&7));}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(CHARGES);}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public net.minecraft.block.material.EnumPushReaction getPushReaction(IBlockState state){return net.minecraft.block.material.EnumPushReaction.BLOCK;}
    @Override public int getLightValue(IBlockState state,IBlockAccess world,BlockPos pos){return state.getValue(CHARGES)*15/4;}
    @Override public boolean hasComparatorInputOverride(IBlockState state){return true;}
    @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos){return state.getValue(CHARGES)*15/4;}
    public static boolean charge(World world,BlockPos pos,int count){IBlockState state=world.getBlockState(pos);return state.getBlock() instanceof RespawnAnchorBlock&&world.setBlockState(pos,state.withProperty(CHARGES,Math.max(0,Math.min(4,count))),3);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){
        ItemStack held=player.getHeldItem(hand);if(!world.isBlockModifiable(player,pos)||!player.canPlayerEdit(pos,side,held))return false;
        int charges=state.getValue(CHARGES);Item glowstone=Item.getItemFromBlock(Blocks.GLOWSTONE);
        if(hand==EnumHand.MAIN_HAND&&held.getItem()!=glowstone&&player.getHeldItemOffhand().getItem()==glowstone&&charges<4)return false;
        if(held.getItem()==glowstone&&charges<4){if(!world.isRemote&&charge(world,pos,charges+1)){if(!player.capabilities.isCreativeMode)held.shrink(1);player.inventory.markDirty();world.playSound(null,pos,ModernSounds.get("block.respawn_anchor.charge"),SoundCategory.BLOCKS,1,1);}return true;}
        if(charges==0)return false;if(world.isRemote)return true;
        if(world.provider.isNether()){if(RespawnAnchors.bind(player,world,pos)){player.sendMessage(new TextComponentTranslation("tile.etfuturum.respawn_anchor.set"));world.playSound(null,pos,ModernSounds.get("block.respawn_anchor.set_spawn"),SoundCategory.BLOCKS,1,1);}}
        else if(world.setBlockToAir(pos))world.newExplosion(null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,5,true,true);
        return true;
    }
    static void dispenser(){final Item glowstone=Item.getItemFromBlock(Blocks.GLOWSTONE);final IBehaviorDispenseItem previous=BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(glowstone);BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(glowstone,(source,stack)->{World world=source.getWorld();BlockPos pos=source.getBlockPos().offset(source.getBlockState().getValue(BlockDispenser.FACING));IBlockState state=world.getBlockState(pos);if(state.getBlock() instanceof RespawnAnchorBlock&&state.getValue(CHARGES)<4&&charge(world,pos,state.getValue(CHARGES)+1)){stack.shrink(1);world.playSound(null,pos,ModernSounds.get("block.respawn_anchor.charge"),SoundCategory.BLOCKS,1,1);return stack;}return previous.dispense(source,stack);});}
}
