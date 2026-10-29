package ganymedes01.etfuturum.blocks;

import java.util.*;
import cpw.mods.fml.relauncher.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.*;
import net.minecraftforge.common.util.ForgeDirection;

/** Support distance in metadata, with native one-way collision and falling entities. */
public final class BlockScaffolding extends BaseBlock {
    @SideOnly(Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
    public BlockScaffolding(){super(Material.wood);setNames("scaffolding");setHardness(0);setLightOpacity(0);setStepSound(soundTypeWood);}
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas){models=new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[]{new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas,"scaffolding_stable"),new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas,"scaffolding_unstable")};blockIcon=models[0].particle;}
    @SideOnly(Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta){return models[(meta&8)==0?0:1];}
    @Override public int getRenderType(){return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK;}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public boolean isSideSolid(IBlockAccess world,int x,int y,int z,ForgeDirection side){return false;}
    public int distance(IBlockAccess world,int x,int y,int z){Block below=world.getBlock(x,y-1,z);int distance=below==this?world.getBlockMetadata(x,y-1,z)&7:below.isSideSolid(world,x,y-1,z,ForgeDirection.UP)?0:7;for(ForgeDirection side:ForgeDirection.VALID_DIRECTIONS)if(side.offsetY==0&&world.getBlock(x+side.offsetX,y,z+side.offsetZ)==this)distance=Math.min(distance,(world.getBlockMetadata(x+side.offsetX,y,z+side.offsetZ)&7)+1);return Math.min(7,distance);}
    private int supported(IBlockAccess world,int x,int y,int z){int distance=distance(world,x,y,z);return distance|(distance>0&&world.getBlock(x,y-1,z)!=this?8:0);}
    @Override public int onBlockPlaced(World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ,int meta){return supported(world,x,y,z);}
    @Override public int damageDropped(int meta){return 0;}
    @Override public void onBlockAdded(World world,int x,int y,int z){schedule(world,x,y,z);}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block block){schedule(world,x,y,z);}
    private void schedule(World world,int x,int y,int z){if(!world.isRemote)world.scheduleBlockUpdate(x,y,z,this,1);}
    @Override public void updateTick(World world,int x,int y,int z,Random random){
        if(world.isRemote)return;
        if(!world.checkChunksExist(x-1,Math.max(0,y-1),z-1,x+1,y,z+1)){world.scheduleBlockUpdate(x,y,z,this,20);return;}
        int previous=world.getBlockMetadata(x,y,z),next=supported(world,x,y,z);
        if((next&7)==7){if((previous&7)<7){dropBlockAsItem(world,x,y,z,previous,0);world.setBlockToAir(x,y,z);return;}EntityFallingBlock falling=new EntityFallingBlock(world,x+.5,y+.5,z+.5,this,previous);if(!world.spawnEntityInWorld(falling))world.scheduleBlockUpdate(x,y,z,this,20);}
        else if(previous!=next)world.setBlockMetadataWithNotify(x,y,z,next,3);
    }
    @Override public boolean isLadder(IBlockAccess world,int x,int y,int z,EntityLivingBase entity){return !entity.isSneaking();}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z){return AxisAlignedBB.getBoundingBox(x,y+.875,z,x+1,y+1,z+1);}
    @Override public void addCollisionBoxesToList(World world,int x,int y,int z,AxisAlignedBB box,List result,Entity entity){
        if(entity!=null&&entity.isSneaking())return;double feet=entity==null?Double.POSITIVE_INFINITY:entity.boundingBox.minY;
        AxisAlignedBB plate=feet>=y+1-.001?getCollisionBoundingBoxFromPool(world,x,y,z):(world.getBlockMetadata(x,y,z)&8)!=0&&feet>=y+.125-.001?AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+.125,z+1):null;
        if(plate!=null&&box.intersectsWith(plate))result.add(plate);
    }
    @Override public void onEntityCollidedWithBlock(World world,int x,int y,int z,Entity entity){if(!(entity instanceof EntityLivingBase)||entity instanceof net.minecraft.entity.player.EntityPlayer&&((net.minecraft.entity.player.EntityPlayer)entity).capabilities.isFlying)return;entity.fallDistance=0;entity.motionX=Math.max(-.15,Math.min(.15,entity.motionX));entity.motionZ=Math.max(-.15,Math.min(.15,entity.motionZ));boolean jumping=entity instanceof ganymedes01.etfuturum.entities.RiderInput&&((ganymedes01.etfuturum.entities.RiderInput)entity).etfuturum$isJumping();entity.motionY=entity.isSneaking()?-.15:jumping?.2:Math.max(-.15,entity.motionY);}
    @Override public void onFallenUpon(World world,int x,int y,int z,Entity entity,float distance){entity.fallDistance=0;}
}
