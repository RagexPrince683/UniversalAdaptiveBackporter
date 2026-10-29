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
    @Override public Object crafterGui(CrafterContainer container) { return new CrafterGui(container); }
    @Override public void spearLunge(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        net.minecraft.item.ItemStack stack = player.getHeldItemMainhand();
        if (player == mc.player && mc.currentScreen == null && stack.getItem() instanceof ganymedes01.etfuturum.SpearItem
                && ModEnchantments.level(ModEnchantments.lunge, stack) > 0) SpearLunge.request();
    }
    @SubscribeEvent public void spearAir(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getHand() == net.minecraft.util.EnumHand.MAIN_HAND) spearLunge(event.getEntityPlayer());
    }

    @Override public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        CompassView.initialize();
        MinecraftForge.EVENT_BUS.register(new SpyglassView());
        MinecraftForge.EVENT_BUS.register(new SpringAnimalView());
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(HangingSigns.HangingTile.class,new HangingSignRenderer());
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(BellBlock.BellTile.class,new BellRenderer());
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(CampfireTile.class,new StorageDisplayRenderer());
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(BookStorage.Storage.class,new StorageDisplayRenderer());
        ShulkerTooltip.limit();
        MinecraftForge.EVENT_BUS.register(new ClientAtmosphere());
        GeneratedResources.install();
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(TrialSpawnerTile.class,new net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer());
        ModernMobRenderer.register();
        RenderingRegistry.registerEntityRenderingHandler(EntityNewBoat.class,NewBoatRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityNewBoatWithChest.class,NewBoatRenderer::new);
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(WoodSigns.SignTile.class,new WoodSignRenderer());
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(LecternTile.class,new LecternRenderer());
        MinecraftForge.EVENT_BUS.register(new BoatClientEvents());

        RenderingRegistry.registerEntityRenderingHandler(EntityGoat.class,GoatRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBee.class,BeeRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFox.class,FoxRenderer::new);
        MinecraftForge.EVENT_BUS.register(new BeeClientEffects());
        MinecraftForge.EVENT_BUS.register(new ganymedes01.etfuturum.client.renderer.ArmorTrimRenderer());
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
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(CopperChestTile.class,new CopperBlockRenderer(false));
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(CopperStatueTile.class,new CopperBlockRenderer(true));
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
        ModelLoader.setCustomModelResourceLocation(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.END_PORTAL),0,new ModelResourceLocation(new ResourceLocation("minecraft","end_portal"),"inventory"));
        for(Block block:Content.BLOCKS.values()) {
            if(block instanceof LegacyDaylightDetector)ModelLoader.setCustomStateMapper(block,new net.minecraft.client.renderer.block.statemap.StateMap.Builder().ignore(BlockDaylightDetector.POWER).build());
            else if(block instanceof AquaticPlant)ModelLoader.setCustomStateMapper(block,aquaticMapper(block));
            else if(block instanceof BubbleColumn)ModelLoader.setCustomStateMapper(block,ignored->Collections.emptyMap());
        }
        Content.ITEMS.values().forEach(item -> ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory")));
        for(Block block:Content.BLOCKS.values())if(block instanceof ModdedOreBlock)for(int meta=0;meta<16;meta++)ModelLoader.setCustomModelResourceLocation(net.minecraft.item.Item.getItemFromBlock(block),meta,new ModelResourceLocation(new ResourceLocation(UAB.ID,((ModdedOreBlock)block).variant(meta)),"inventory"));
        for(net.minecraft.item.Item item:Content.ITEMS.values())if(item instanceof VariantItem)for(int variant=1;variant<((VariantItem)item).variants.length;variant++)ModelLoader.setCustomModelResourceLocation(item,variant,new ModelResourceLocation(new ResourceLocation(UAB.ID,((VariantItem)item).variants[variant]),"inventory"));
        for(int horn=1;horn<8;horn++)ModelLoader.setCustomModelResourceLocation(Content.item("goat_horn"),horn,new ModelResourceLocation(Content.item("goat_horn").getRegistryName(),"inventory"));
        for(int pose=1;pose<8;pose++)ModelLoader.setCustomModelResourceLocation(Content.item("copper_golem_statue"),pose,new ModelResourceLocation(Content.item("copper_golem_statue").getRegistryName(),"inventory"));
        ModelLoader.setCustomModelResourceLocation(Content.item("eyeblossom"),1,new ModelResourceLocation(new ResourceLocation(UAB.ID,"open_eyeblossom"),"inventory"));
        for(int level=1;level<16;level++)ModelLoader.setCustomModelResourceLocation(Content.item("light"),level,new ModelResourceLocation(new ResourceLocation(UAB.ID,String.format(Locale.ROOT,"light_%02d",level)),"inventory"));
    }
    private net.minecraft.client.renderer.block.statemap.IStateMapper aquaticMapper(Block block) {
        net.minecraft.client.renderer.block.statemap.StateMap.Builder builder=new net.minecraft.client.renderer.block.statemap.StateMap.Builder().ignore(BlockLiquid.LEVEL);
        if(block instanceof CoralPlant&&!((CoralPlant)block).shape.equals("_wall_fan"))builder.ignore(CoralPlant.FACING);
        return builder.build();
    }
    @Override public void springState(SpringAnimals.State state,net.minecraftforge.fml.common.network.simpleimpl.MessageContext context){Minecraft mc=Minecraft.getMinecraft();mc.addScheduledTask(()->{if(mc.world!=null&&mc.getConnection()==context.getClientHandler())SpringAnimalView.receive(state);});}
    @Override public void damageDirection(float yaw,net.minecraftforge.fml.common.network.simpleimpl.MessageContext context){Minecraft mc=Minecraft.getMinecraft();mc.addScheduledTask(()->{if(mc.player!=null&&mc.getConnection()==context.getClientHandler()&&Features.enabled("functions.enableAttackedAtYawFix"))mc.player.attackedAtYaw=yaw;});}
    @Override public boolean holdingLight(){EntityPlayer player=Minecraft.getMinecraft().player;return player!=null&&(player.getHeldItemMainhand().getItem()==Content.item("light")||player.getHeldItemOffhand().getItem()==Content.item("light"));}
    @Override public void honeyDrip(World world,double x,double y,double z){Minecraft.getMinecraft().effectRenderer.addEffect(new ganymedes01.etfuturum.client.particle.CustomDripFX(world,x,y,z,ModernSounds.DOMAIN+":block.beehive.drip",0xFF8E6D14,false));}
    @Override public void lightMarker(World world,double x,double y,double z,int level){Minecraft.getMinecraft().effectRenderer.addEffect(new LightMarkerParticle(world,x,y,z,level));}
    @Override public void beeNectar(net.minecraft.world.World world,double x,double y,double z){Minecraft.getMinecraft().effectRenderer.addEffect(new BeeNectarParticle(world,x,y,z));}
    @Override public void init() {
        net.minecraft.client.renderer.color.BlockColors colors=Minecraft.getMinecraft().getBlockColors();for(String name:SpringPlants.PLANTS){Block plant=Content.block(name);if(plant instanceof SpringPatch&&((SpringPatch)plant).litter){colors.registerBlockColorHandler((state,world,pos,tint)->world!=null&&pos!=null?DryFoliageColors.color(world.getBiome(pos).getTemperature(pos),world.getBiome(pos).getRainfall()):DryFoliageColors.color(.5,1),plant);Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack,tint)->DryFoliageColors.color(.5,1),net.minecraft.item.Item.getItemFromBlock(plant));}else if(plant instanceof SpringPatch||plant instanceof SpringPlant&&(((SpringPlant)plant).kind.equals("bush")||((SpringPlant)plant).kind.equals("firefly_bush"))){colors.registerBlockColorHandler((state,world,pos,tint)->world!=null&&pos!=null?net.minecraft.world.biome.BiomeColorHelper.getGrassColorAtPos(world,pos):0x79c05a,plant);Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack,tint)->0x79c05a,net.minecraft.item.Item.getItemFromBlock(plant));}}

        if(net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance.getRenderer(net.minecraft.tileentity.TileEntityFlowerPot.class)==null)net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(net.minecraft.tileentity.TileEntityFlowerPot.class,new FlowerPotRenderer());
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(net.minecraft.tileentity.TileEntityShulkerBox.class,new TieredShulkerRenderer());
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(UndyedShulkerTile.class,new TieredShulkerRenderer());
        Content.item("shulker_box").setTileEntityItemStackRenderer(new TieredShulkerStackRenderer(Content.block("shulker_box")));
        for(net.minecraft.item.EnumDyeColor color:net.minecraft.item.EnumDyeColor.values()){Block block=BlockShulkerBox.getBlockByColor(color);net.minecraft.item.Item.getItemFromBlock(block).setTileEntityItemStackRenderer(new TieredShulkerStackRenderer(block));}
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(BarrelTile.class,new CrystalStorageRenderer());
        Content.item("trident").setTileEntityItemStackRenderer(new TridentStackRenderer());
        for(java.util.Map.Entry<String,net.minecraft.block.Block> entry:Content.BLOCKS.entrySet())if(entry.getValue() instanceof CopperChestBlock)Content.item(entry.getKey()).setTileEntityItemStackRenderer(new CopperStackRenderer(false));
        Content.item("copper_golem_statue").setTileEntityItemStackRenderer(new CopperStackRenderer(true));
        Content.item("decorated_pot").setTileEntityItemStackRenderer(new PotStackRenderer());
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state,world,pos,tint)->{if(world!=null&&pos!=null&&world.getTileEntity(pos) instanceof PotionCauldronTile)return net.minecraft.potion.PotionUtils.getColor(((PotionCauldronTile)world.getTileEntity(pos)).potion);return 0x3F76E4;},Content.block("potion_cauldron"));
        for(Block block:Content.BLOCKS.values())if(block instanceof SpecialOres.Family){Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state,world,pos,tint)->SpecialOres.color(block,block.getMetaFromState(state)),block);Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack,tint)->SpecialOres.color(block,stack.getMetadata()),net.minecraft.item.Item.getItemFromBlock(block));}
        for(Block block:Content.BLOCKS.values())if(block instanceof BubbleColumn)Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tint) -> world == null || pos == null ? 0x3F76E4 : world.getBiome(pos).getWaterColor(),block);
    }
    @SubscribeEvent public void bake(net.minecraftforge.client.event.ModelBakeEvent event) {
        try {
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
    @Override public boolean storageOpen(net.minecraft.inventory.IInventory inventory){EntityPlayer player=Minecraft.getMinecraft().player;return player!=null&&player.openContainer!=player.inventoryContainer&&!player.openContainer.inventorySlots.isEmpty()&&player.openContainer.inventorySlots.get(0).inventory==inventory;}
    @Override public Object storageGui(net.minecraft.entity.player.EntityPlayer player,net.minecraft.inventory.IInventory inventory){return new StorageGui(player,inventory);}
    @Override public Object workstationGui(WorkstationContainer container) { return new WorkstationGui(container); }
    @Override public Object lecternGui(LecternContainer container) { return new LecternGui(container); }
}
