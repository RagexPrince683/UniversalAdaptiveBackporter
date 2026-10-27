package ganymedes01.etfuturum;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) { }
    public void init() { }
    public void damageDirection(float yaw,net.minecraftforge.fml.common.network.simpleimpl.MessageContext context){}
    public boolean holdingLight(){return false;}
    public void honeyDrip(net.minecraft.world.World world,double x,double y,double z){}
    public void lightMarker(net.minecraft.world.World world,double x,double y,double z,int level){}
    public void beeNectar(net.minecraft.world.World world,double x,double y,double z){}
    public Object smithingGui(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) { return null; }
    public Object cookingGui(net.minecraft.entity.player.EntityPlayer player, CookingTile tile) { return null; }
    public boolean storageOpen(net.minecraft.inventory.IInventory inventory){return false;}
    public Object storageGui(net.minecraft.entity.player.EntityPlayer player,net.minecraft.inventory.IInventory inventory){return null;}
    public Object workstationGui(WorkstationContainer container) { return null; }
}
