package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.tileentities.TileEntityCampfire;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.*;
import java.util.Random;

/** Local contained-water state only; bits 0..1 facing, bit 2 unlit, bit 3 wet. */
public final class BlockCampfire extends BaseBlock {
    public final boolean soul;
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
    public BlockCampfire(boolean soul) { super(Material.wood); this.soul=soul;setNames(soul?"soul_campfire":"campfire");setHardness(2);setLightOpacity(0);setBlockBounds(0,0,0,1,.4375F,1);setStepSound(soundTypeWood); }
    public static boolean lit(int meta) { return (meta&12)==0; }
    @Override public int getLightValue(IBlockAccess world,int x,int y,int z){return lit(world.getBlockMetadata(x,y,z))?(soul?10:15):0;}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public int getRenderType(){return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK;}
    @Override public int damageDropped(int meta){return soul?0:1;}
    @Override public Item getItemDropped(int meta,Random random,int fortune){return soul?Item.getItemFromBlock(ganymedes01.etfuturum.ModBlocks.SOUL_SOIL.get()):Items.coal;}
    @Override public int quantityDropped(Random random){return soul?1:2;}
    @Override public boolean canSilkHarvest(World world,EntityPlayer player,int x,int y,int z,int meta){return true;}
    @Override public int getMobilityFlag(){return 1;}
    @Override public boolean hasTileEntity(int meta){return true;}
    @Override public TileEntity createTileEntity(World world,int meta){return new TileEntityCampfire();}
    @Override public int onBlockPlaced(World world,int x,int y,int z,int side,float hx,float hy,float hz,int meta){return world.getBlock(x,y,z).getMaterial()==Material.water?12:0;}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){world.setBlockMetadataWithNotify(x,y,z,(world.getBlockMetadata(x,y,z)&12)|(MathHelper.floor_double(placer.rotationYaw*4/360+.5)&3),3);}
    @Override public void onEntityCollidedWithBlock(World world,int x,int y,int z,Entity entity){int meta=world.getBlockMetadata(x,y,z);if(!world.isRemote&&!lit(meta)&&(meta&8)==0&&entity instanceof net.minecraft.entity.projectile.EntityArrow&&entity.isBurning()&&(((net.minecraft.entity.projectile.EntityArrow)entity).shootingEntity instanceof EntityPlayer||world.getGameRules().getGameRuleBooleanValue("mobGriefing")))world.setBlockMetadataWithNotify(x,y,z,meta&~4,3);if(lit(meta)&&entity instanceof EntityLivingBase&&ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.frostWalker,((EntityLivingBase)entity).getEquipmentInSlot(1))==0)entity.attackEntityFrom(DamageSource.inFire,soul?2:1);}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){
        ItemStack held=player.getHeldItem();if(held==null||!player.canPlayerEdit(x,y,z,side,held))return false;
        int meta=world.getBlockMetadata(x,y,z);if((meta&8)!=0&&held.getItem()==Items.bucket){if(!world.isRemote){world.setBlockMetadataWithNotify(x,y,z,(meta&3)|4,3);if(!player.capabilities.isCreativeMode){ItemStack water=new ItemStack(Items.water_bucket);if(--held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,water);else if(!player.inventory.addItemStackToInventory(water))player.dropPlayerItemWithRandomChoice(water,false);}}return true;}boolean light=(meta&8)==0&&!lit(meta)&&(held.getItem()==Items.flint_and_steel||held.getItem()==Items.fire_charge),extinguish=lit(meta)&&held.getItem() instanceof ItemSpade,fill=(meta&8)==0&&held.getItem()==Items.water_bucket&&!world.provider.isHellWorld;
        if(light||extinguish||fill){if(!world.isRemote){world.setBlockMetadataWithNotify(x,y,z,light?meta&~4:fill?meta|12:meta|4,3);if(!player.capabilities.isCreativeMode){if(held.getItem()==Items.flint_and_steel||extinguish)held.damageItem(1,player);else if(fill)player.inventory.setInventorySlotContents(player.inventory.currentItem,new ItemStack(Items.bucket));else if(--held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}}return true;}
        TileEntity tile=world.getTileEntity(x,y,z);return tile instanceof TileEntityCampfire&&((TileEntityCampfire)tile).insert(player,held);
    }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,net.minecraft.block.Block neighbor){if(world.isRemote)return;for(net.minecraftforge.common.util.ForgeDirection d:net.minecraftforge.common.util.ForgeDirection.VALID_DIRECTIONS)if(world.getBlock(x+d.offsetX,y+d.offsetY,z+d.offsetZ).getMaterial()==Material.water){world.setBlockMetadataWithNotify(x,y,z,world.getBlockMetadata(x,y,z)|12,3);break;}}
    @Override public void breakBlock(World world,int x,int y,int z,net.minecraft.block.Block block,int meta){TileEntity tile=world.getTileEntity(x,y,z);if(tile instanceof TileEntityCampfire)((TileEntityCampfire)tile).dropContents();super.breakBlock(world,x,y,z,block,meta);world.removeTileEntity(x,y,z);}
    @Override public boolean removedByPlayer(World world,EntityPlayer player,int x,int y,int z,boolean harvest){boolean wet=(world.getBlockMetadata(x,y,z)&8)!=0;return world.setBlock(x,y,z,wet?Blocks.water:Blocks.air,0,3);}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public int getRenderBlockPass(){return 1;}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public boolean canRenderInPass(int pass){return pass==0||pass==1;}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas){models=new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[8];for(int off=0;off<2;off++)for(int r=0;r<4;r++)models[off*4+r]=new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas,(soul?"soul_campfire":"campfire")+(off==1?"_off":""),r);blockIcon=models[0].particle;}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta){return models[(meta&3)|(lit(meta)?0:4)];}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void randomDisplayTick(World world,int x,int y,int z,Random random){if(lit(world.getBlockMetadata(x,y,z))){if(random.nextInt(3)==0)ganymedes01.etfuturum.client.particle.CampfireSmoke.spawn(world,x+.5,y+.7,z+.5,world.getBlock(x,y-1,z)==Blocks.hay_block);if(random.nextInt(10)==0)world.playSound(x+.5,y+.5,z+.5,"fire.fire",.5F,1,false);}}
}
