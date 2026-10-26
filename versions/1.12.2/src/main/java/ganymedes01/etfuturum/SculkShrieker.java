package ganymedes01.etfuturum;
import ganymedes01.etfuturum.entities.EntityWarden;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public final class SculkShrieker extends Block {
    public static final PropertyBool CAN_SUMMON=PropertyBool.create("can_summon"),SHRIEKING=PropertyBool.create("shrieking");
    SculkShrieker(){super(Material.ROCK);setHardness(3);setDefaultState(blockState.getBaseState().withProperty(CAN_SUMMON,false).withProperty(SHRIEKING,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,CAN_SUMMON,SHRIEKING);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(CAN_SUMMON,(meta&8)!=0).withProperty(SHRIEKING,(meta&4)!=0);}
    @Override public int getMetaFromState(IBlockState state){return (state.getValue(CAN_SUMMON)?8:0)|(state.getValue(SHRIEKING)?4:0);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,0,0,1,.5,1);}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return side==EnumFacing.DOWN;}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new SculkShriekerTile();}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public void onEntityWalk(World world,BlockPos pos,Entity entity){if(entity instanceof EntityPlayer)shriek(world,pos,(EntityPlayer)entity);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,java.util.Random random){world.setBlockState(pos,state.withProperty(SHRIEKING,false),2);}
    void shriek(World world,BlockPos pos,EntityPlayer player){if(world.isRemote||!Content.available("sculk_shrieker")||player.capabilities.disableDamage||player.isSneaking()||!world.getBlockState(pos).getValue(CAN_SUMMON)||!Features.enabled("entities.enableModernHostileSpawning"))return;NBTTagCompound data=player.getEntityData();long time=world.getTotalWorldTime();if(data.getLong("EFRShriekCooldown")>time)return;data.setLong("EFRShriekCooldown",time+200);long last=data.getLong("EFRShriekLast");int decayed=last>0?(int)Math.min(4,Math.max(0,time-last)/12000):0,warnings=Math.min(4,Math.max(0,data.getInteger("EFRShriekWarnings")-decayed)+1);data.setLong("EFRShriekLast",time);data.setInteger("EFRShriekWarnings",warnings);world.playSound(null,pos,ModernSounds.get("block.sculk_shrieker.shriek"),SoundCategory.BLOCKS,2,1);world.setBlockState(pos,world.getBlockState(pos).withProperty(SHRIEKING,true),2);world.scheduleUpdate(pos,this,90);for(EntityPlayer observer:world.playerEntities)if(!observer.capabilities.disableDamage&&observer.getDistanceSqToCenter(pos)<=1600)observer.addPotionEffect(new PotionEffect(AquaticEffects.DARKNESS,260));if(warnings<4||!MobRegistry.enabled(EntityWarden.class)||world.getDifficulty()==EnumDifficulty.PEACEFUL||!world.getGameRules().getBoolean("doMobSpawning")||!world.getEntitiesWithinAABB(EntityWarden.class,player.getEntityBoundingBox().grow(48,24,48)).isEmpty())return;for(int attempt=0;attempt<12;attempt++){BlockPos at=pos.add(world.rand.nextInt(11)-5,0,world.rand.nextInt(11)-5);if(!world.isBlockLoaded(at.up(3))||!world.getBlockState(at.down()).isSideSolid(world,at.down(),EnumFacing.UP))continue;EntityWarden warden=new EntityWarden(world);warden.setLocationAndAngles(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);if(world.getCollisionBoxes(warden,warden.getEntityBoundingBox()).isEmpty()&&world.checkNoEntityCollision(warden.getEntityBoundingBox())&&world.spawnEntity(warden))break;}}
}
