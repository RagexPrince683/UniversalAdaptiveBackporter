package ganymedes01.etfuturum.client;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.*;
import net.minecraftforge.client.MinecraftForgeClient;

/** The plant and its source water participate in their native render passes. */
final class ContainedWaterModel implements IBakedModel {
    private final IBakedModel plant, water;
    ContainedWaterModel(IBakedModel plant, IBakedModel water) { this.plant = plant; this.water = water; }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) { return (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT ? water : plant).getQuads(state, side, seed); }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return plant.isGui3d(); }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return plant.getParticleTexture(); }
    @Override public ItemOverrideList getOverrides() { return plant.getOverrides(); }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return plant.getItemCameraTransforms(); }
}
