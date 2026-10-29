package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.*;
import ganymedes01.etfuturum.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.ForgeDirection;

public final class BlockRespawnAnchor extends BaseBlock {
    @SideOnly(Side.CLIENT) private IIcon[] sides;@SideOnly(Side.CLIENT) private IIcon top,off,bottom;
    public BlockRespawnAnchor(){super(Material.rock);setNames("respawn_anchor");setHardness(50);setResistance(1200);setHarvestLevel("pickaxe",3);}
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas){sides=new IIcon[5];for(int i=0;i<5;i++)sides[i]=atlas.registerIcon("respawn_anchor_side"+i);top=atlas.registerIcon("respawn_anchor_top");off=atlas.registerIcon("respawn_anchor_top_off");bottom=atlas.registerIcon("respawn_anchor_bottom");blockIcon=sides[0];}
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side,int meta){int charges=Math.max(0,Math.min(4,meta));return side==0?bottom:side==1?charges==0?off:top:sides[charges];}
    @Override public int damageDropped(int meta){return 0;}
    @Override public int getMobilityFlag(){return 2;}
    @Override public int getLightValue(IBlockAccess world,int x,int y,int z){return Math.min(4,world.getBlockMetadata(x,y,z))*15/4;}
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World world,int x,int y,int z,int side){return Math.min(4,world.getBlockMetadata(x,y,z))*15/4;}
    public static boolean charge(World world,int x,int y,int z,int count){if(!(world.getBlock(x,y,z) instanceof BlockRespawnAnchor)||!world.setBlockMetadataWithNotify(x,y,z,Math.max(0,Math.min(4,count)),3))return false;world.func_147451_t(x,y,z);return true;}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float a,float b,float c){
        ItemStack held=player.getHeldItem();if(!world.canMineBlock(player,x,y,z)||!player.canPlayerEdit(x,y,z,side,held))return false;int charges=Math.max(0,Math.min(4,world.getBlockMetadata(x,y,z)));
        if(held!=null&&held.getItem()==Item.getItemFromBlock(Blocks.glowstone)&&charges<4){if(!world.isRemote&&charge(world,x,y,z,charges+1)){if(!player.capabilities.isCreativeMode&&--held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);player.inventory.markDirty();world.playSoundEffect(x+.5,y+.5,z+.5,Tags.MC_ASSET_VER+":block.respawn_anchor.charge",1,1);}return true;}
        if(charges==0)return false;if(world.isRemote)return true;
        if(world.provider.isHellWorld){if(RespawnAnchors.bind(player,world,new ChunkCoordinates(x,y,z))){player.addChatMessage(new ChatComponentTranslation("tile.etfuturum.respawn_anchor.set"));world.playSoundEffect(x+.5,y+.5,z+.5,Tags.MC_ASSET_VER+":block.respawn_anchor.set_spawn",1,1);}}
        else if(world.setBlockToAir(x,y,z))world.newExplosion(null,x+.5,y+.5,z+.5,5,true,true);
        return true;
    }
    public static void dispenser(){final Item glowstone=Item.getItemFromBlock(Blocks.glowstone);final IBehaviorDispenseItem previous=(IBehaviorDispenseItem)BlockDispenser.dispenseBehaviorRegistry.getObject(glowstone);BlockDispenser.dispenseBehaviorRegistry.putObject(glowstone,(IBehaviorDispenseItem)(source,stack)->{World world=source.getWorld();ForgeDirection side=ForgeDirection.getOrientation(source.getBlockMetadata()&7);int x=source.getXInt()+side.offsetX,y=source.getYInt()+side.offsetY,z=source.getZInt()+side.offsetZ;if(world.getBlock(x,y,z) instanceof BlockRespawnAnchor&&world.getBlockMetadata(x,y,z)<4&&charge(world,x,y,z,world.getBlockMetadata(x,y,z)+1)){stack.stackSize--;world.playSoundEffect(x+.5,y+.5,z+.5,Tags.MC_ASSET_VER+":block.respawn_anchor.charge",1,1);return stack;}return previous.dispense(source,stack);});}
}
