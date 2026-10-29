package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.*;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.*;

/** Oxidation identities are separate blocks so native orientation/power metadata stays intact. */
public interface ICopperFixture extends IDegradable {
    final class Info {public final String family;public final int stage;public final boolean wax;public Info(String family,int stage,boolean wax){this.family=family;this.stage=stage;this.wax=wax;}}
    Info copper();
    @Override default int getCopperMeta(int metadata){return copper().stage+(copper().wax?8:0);}
    @Override default int getFinalCopperMeta(IBlockAccess access,int x,int y,int z,int copper,int metadata){return metadata;}
    @Override default Block getCopperBlockFromMeta(int copper){String name=ModernAdditions.copperName(copper().family,copper%4,copper>=8);return name.equals("lightning_rod")?ModBlocks.LIGHTNING_ROD.get():name.equals("copper_chest")?ModBlocks.COPPER_CHEST.get():ModernAdditions.block(name);}
    @Override default void setCopperBlock(Block replacement,int metadata,World world,int x,int y,int z){if(replacement==null||world.isRemote)return;TileEntity old=world.getTileEntity(x,y,z);NBTTagCompound saved=null;if(old!=null){saved=new NBTTagCompound();old.writeToNBT(saved);}world.setBlock(x,y,z,replacement,metadata,3);TileEntity tile=world.getTileEntity(x,y,z);if(tile!=null&&saved!=null&&old.getClass()==tile.getClass()){tile.readFromNBT(saved);tile.markDirty();}if(replacement instanceof BlockLightningRod&&(metadata&8)!=0)world.scheduleBlockUpdate(x,y,z,replacement,8);}
}
