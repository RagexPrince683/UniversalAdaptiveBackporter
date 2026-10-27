package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=TileEntityShulkerBox.class,remap=false)
public abstract class TieredShulkerMixin extends net.minecraft.tileentity.TileEntityLockableLoot implements TieredShulker {
 @Unique private StorageTier uab$tier=StorageTier.WOOD;
 @Unique private int[] uab$slots;
 @Override public StorageTier uab$tier(){return uab$tier;}
 @Override public NBTTagCompound getUpdateTag(){NBTTagCompound tag=super.getUpdateTag();tag.setByte("Type",(byte)uab$tier.shulkerId());tag.setBoolean("UABStorageUpdate",true);tag.setString("CustomName",hasCustomName()?getName():"");if(uab$tier==StorageTier.CRYSTAL&&getLootTable()==null)IronStorage.preview(tag,getItems());return tag;}
 @Inject(method={"update","func_73660_a"},at=@At("RETURN"),remap=false)
 private void uab$crystal(CallbackInfo ci){IronStorage.syncCrystal((TileEntityShulkerBox)(Object)this);}
 @Inject(method={"loadFromNbt","func_190586_e"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$load(NBTTagCompound tag,CallbackInfo ci){StorageTier next=StorageTier.shulker(tag.getByte("Type"));if(tag.getBoolean("UABStorageUpdate")&&world!=null&&world.isRemote&&next==uab$tier&&UAB.proxy.storageOpen((TileEntityShulkerBox)(Object)this)){setCustomName(tag.getString("CustomName"));ci.cancel();return;}uab$tier=next;uab$slots=null;}
 @Inject(method={"saveToNbt","func_190580_f"},at=@At("RETURN"),remap=false)
 private void uab$save(NBTTagCompound tag,CallbackInfoReturnable<NBTTagCompound> ci){if(uab$tier!=StorageTier.WOOD)ci.getReturnValue().setByte("Type",(byte)uab$tier.shulkerId());}
 @Inject(method={"getSizeInventory","func_70302_i_"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$size(CallbackInfoReturnable<Integer> ci){if(uab$tier!=null&&uab$tier!=StorageTier.WOOD)ci.setReturnValue(uab$tier.size);}
 @Inject(method={"getSlotsForFace","func_180463_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$slots(EnumFacing side,CallbackInfoReturnable<int[]> ci){if(uab$tier!=StorageTier.WOOD){if(uab$slots==null){uab$slots=new int[uab$tier.size];for(int i=0;i<uab$slots.length;i++)uab$slots[i]=i;}ci.setReturnValue(uab$slots);}}
 @Inject(method={"createContainer","func_174876_a"},at=@At("HEAD"),cancellable=true,remap=false)
 private void uab$container(InventoryPlayer inventory,EntityPlayer player,CallbackInfoReturnable<Container> ci){if(uab$tier!=StorageTier.WOOD){TileEntityShulkerBox tile=(TileEntityShulkerBox)(Object)this;tile.fillWithLoot(player);ci.setReturnValue(new StorageContainer(inventory,tile,player));}}
}
