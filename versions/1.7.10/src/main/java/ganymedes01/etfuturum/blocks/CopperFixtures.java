package ganymedes01.etfuturum.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import java.util.Random;

public final class CopperFixtures {
    private CopperFixtures(){}
    public static final class Lantern extends BlockLantern implements ICopperFixture {
        private final Info copper;public Lantern(String name,int stage,boolean wax){super(name,15);copper=new Info("copper_lantern",stage,wax);setTickRandomly(!wax);}@Override public Info copper(){return copper;}
        @Override public void updateTick(World world,int x,int y,int z,Random random){tickDegradation(world,x,y,z,random);}@Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){return tryWaxOnWaxOff(world,x,y,z,player);}
    }
    public static final class Chain extends BlockChain implements ICopperFixture {
        private final Info copper;public Chain(String name,int stage,boolean wax){setNames(name);copper=new Info("copper_chain",stage,wax);setTickRandomly(!wax);}@Override public Info copper(){return copper;}
        @Override public void updateTick(World world,int x,int y,int z,Random random){tickDegradation(world,x,y,z,random);}@Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){return tryWaxOnWaxOff(world,x,y,z,player);}
    }
    public static final class Bars extends BlockPane implements ICopperFixture {
        private final Info copper;public Bars(String name,int stage,boolean wax){super(name,name,Material.iron,true);copper=new Info("copper_bars",stage,wax);setBlockName("etfuturum."+name);setHardness(3);setResistance(6);setHarvestLevel("pickaxe",0);setTickRandomly(!wax);}@Override public Info copper(){return copper;}
        @Override public void updateTick(World world,int x,int y,int z,Random random){tickDegradation(world,x,y,z,random);}@Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){return tryWaxOnWaxOff(world,x,y,z,player);}
    }
    public static final class Rod extends BlockLightningRod {
        private final Info copper;public Rod(String name,int stage,boolean wax){setNames(name);copper=new Info("lightning_rod",stage,wax);setTickRandomly(!wax);}@Override public Info copper(){return copper;}
    }
}
