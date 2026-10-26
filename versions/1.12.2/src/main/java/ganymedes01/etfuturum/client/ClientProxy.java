package ganymedes01.etfuturum.client;

import java.util.*;
import ganymedes01.etfuturum.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import ganymedes01.etfuturum.entities.*;

public final class ClientProxy extends CommonProxy {
    @Override public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        GeneratedResources.install();
        ModernMobRenderer.register();
        MinecraftForge.EVENT_BUS.register(new MobVisualEffects());
        RenderingRegistry.registerEntityRenderingHandler(EntityCod.class, manager -> new FishRenderer(manager, 0));
        RenderingRegistry.registerEntityRenderingHandler(EntitySalmon.class, manager -> new FishRenderer(manager, 1));
        RenderingRegistry.registerEntityRenderingHandler(EntityTropicalFish.class, manager -> new FishRenderer(manager, 2));
        RenderingRegistry.registerEntityRenderingHandler(EntityPufferfish.class, manager -> new FishRenderer(manager, 10));
        RenderingRegistry.registerEntityRenderingHandler(EntityPhantom.class, PhantomRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityDrowned.class, DrownedRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTrident.class, TridentRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityDolphin.class,manager->new AquaticAnimalRenderer(manager,7));
        RenderingRegistry.registerEntityRenderingHandler(EntityTurtle.class,manager->new AquaticAnimalRenderer(manager,9));
        RenderingRegistry.registerEntityRenderingHandler(EntityAxolotl.class,manager->new AquaticAnimalRenderer(manager,8));
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowSquid.class,GlowSquidRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingDripstone.class,FallingDripstoneRenderer::new);
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(DecoratedPotTile.class,new DecoratedPotRenderer());
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(BrushableTile.class,new BrushableRenderer());
        RenderingRegistry.registerEntityRenderingHandler(EntityPiglin.class,manager->new NetherMobRenderer(manager,"piglin"));
        RenderingRegistry.registerEntityRenderingHandler(EntityPiglinBrute.class,manager->new NetherMobRenderer(manager,"piglin_brute"));
        RenderingRegistry.registerEntityRenderingHandler(EntityHoglin.class,manager->new NetherMobRenderer(manager,"hoglin"));
        RenderingRegistry.registerEntityRenderingHandler(EntityZoglin.class,manager->new NetherMobRenderer(manager,"zoglin"));
        RenderingRegistry.registerEntityRenderingHandler(EntityStrider.class,manager->new NetherMobRenderer(manager,"strider"));
        RenderingRegistry.registerEntityRenderingHandler(EntityCrossbowBolt.class,manager->new net.minecraft.client.renderer.entity.RenderArrow<EntityCrossbowBolt>(manager){@Override protected ResourceLocation getEntityTexture(EntityCrossbowBolt entity){return new ResourceLocation("minecraft","textures/entity/projectiles/arrow.png");}});
    }
    @SubscribeEvent public void models(ModelRegistryEvent event) {
        for(Block block:Content.BLOCKS.values()) {
            if(block instanceof AquaticPlant)ModelLoader.setCustomStateMapper(block,aquaticMapper(block));
            else if(block instanceof BubbleColumn)ModelLoader.setCustomStateMapper(block,ignored->Collections.emptyMap());
        }
        Content.ITEMS.values().forEach(item -> ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory")));
        ModelLoader.setCustomModelResourceLocation(Content.item("eyeblossom"),1,new ModelResourceLocation(new ResourceLocation(UAB.ID,"open_eyeblossom"),"inventory"));
        for(int level=1;level<16;level++)ModelLoader.setCustomModelResourceLocation(Content.item("light"),level,new ModelResourceLocation(new ResourceLocation(UAB.ID,String.format(Locale.ROOT,"light_%02d",level)),"inventory"));
    }
    private net.minecraft.client.renderer.block.statemap.IStateMapper aquaticMapper(Block block) {
        net.minecraft.client.renderer.block.statemap.StateMap.Builder builder=new net.minecraft.client.renderer.block.statemap.StateMap.Builder().ignore(BlockLiquid.LEVEL);
        if(block instanceof CoralPlant&&!((CoralPlant)block).shape.equals("_wall_fan"))builder.ignore(CoralPlant.FACING);
        return builder.build();
    }
    @Override public void init() {
        Content.item("trident").setTileEntityItemStackRenderer(new TridentStackRenderer());
        Content.item("decorated_pot").setTileEntityItemStackRenderer(new PotStackRenderer());
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state,world,pos,tint)->{if(world!=null&&pos!=null&&world.getTileEntity(pos) instanceof PotionCauldronTile)return net.minecraft.potion.PotionUtils.getColor(((PotionCauldronTile)world.getTileEntity(pos)).potion);return 0x3F76E4;},Content.block("potion_cauldron"));
        for(Block block:Content.BLOCKS.values())if(block instanceof AquaticPlant&&((AquaticPlant)block).submerged||block instanceof BubbleColumn)Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tint) -> world == null || pos == null ? 0x3F76E4 : world.getBiome(pos).getWaterColor(),block);
    }
    @SubscribeEvent public void bake(net.minecraftforge.client.event.ModelBakeEvent event) {
        try {
            net.minecraftforge.client.model.IModel model = net.minecraftforge.client.model.ModelLoaderRegistry.getModel(new ResourceLocation(UAB.ID, "block/contained_water"));
            net.minecraft.client.renderer.block.model.IBakedModel water = model.bake(model.getDefaultState(), net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK, location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
            for(Block block:Content.BLOCKS.values())if(block instanceof AquaticPlant&&((AquaticPlant)block).submerged)for (ModelResourceLocation key : new HashSet<>(aquaticMapper(block).putStateModelLocations(block).values())) {
                event.getModelRegistry().putObject(key, new ContainedWaterModel(event.getModelRegistry().getObject(key), water));
            }
            net.minecraftforge.client.model.IModel hand = net.minecraftforge.client.model.ModelLoaderRegistry.getModel(new ResourceLocation(UAB.ID, "item/trident_in_hand"));
            net.minecraft.client.renderer.block.model.IBakedModel held = hand.bake(hand.getDefaultState(), net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM, location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
            ModelResourceLocation trident = new ModelResourceLocation(Content.item("trident").getRegistryName(), "inventory");
            event.getModelRegistry().putObject(trident, new HeldTridentModel(event.getModelRegistry().getObject(trident), held));
        } catch (Exception failure) { throw new IllegalStateException("UAB could not bake its client models", failure); }
    }
    @SubscribeEvent public void configurationChanged(net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent event) {
        if (UAB.ID.equals(event.getModID())) {
            ganymedes01.etfuturum.configuration.UabConfiguration.get().reloadClient();
            ganymedes01.etfuturum.configuration.UabConfiguration.get().finish();
        }
    }
    @SubscribeEvent public void netherFog(net.minecraftforge.client.event.EntityViewRenderEvent.FogColors event){
        NetherFog.apply(event);
    }
    @Override public Object smithingGui(EntityPlayer player, World world, BlockPos pos) { return new SmithingGui(player, world, pos); }
    @Override public Object cookingGui(EntityPlayer player, CookingTile tile) { return new CookingGui(player.inventory, tile); }
    @Override public Object workstationGui(WorkstationContainer container) { return new WorkstationGui(container); }
}
