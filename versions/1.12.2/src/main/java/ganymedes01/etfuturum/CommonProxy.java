package ganymedes01.etfuturum;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) { }
    public void init() { }
    public Object smithingGui(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) { return null; }
    public Object cookingGui(net.minecraft.entity.player.EntityPlayer player, CookingTile tile) { return null; }
    public Object workstationGui(WorkstationContainer container) { return null; }
}
