package ganymedes01.etfuturum.client;

import java.util.List;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Flat inventory icon and native three-dimensional hand rendering use released transforms. */
final class HeldTridentModel implements IBakedModel {
    private final IBakedModel model, hand;
    private final boolean builtIn;
    HeldTridentModel(IBakedModel icon, IBakedModel hand) { this(icon, new HeldTridentModel(hand, null, true), false); }
    private HeldTridentModel(IBakedModel model, IBakedModel hand, boolean builtIn) { this.model = model; this.hand = hand; this.builtIn = builtIn; }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) { return model.getQuads(state, side, seed); }
    @Override public boolean isAmbientOcclusion() { return model.isAmbientOcclusion(); }
    @Override public boolean isGui3d() { return builtIn || model.isGui3d(); }
    @Override public boolean isBuiltInRenderer() { return builtIn; }
    @Override public TextureAtlasSprite getParticleTexture() { return model.getParticleTexture(); }
    @Override public ItemOverrideList getOverrides() { return model.getOverrides(); }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return model.getItemCameraTransforms(); }
    @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType transform) {
        boolean held = transform == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND || transform == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND || transform == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND || transform == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
        IBakedModel selected = held && hand != null ? hand : this;
        IBakedModel transforms = held && hand != null ? ((HeldTridentModel) hand).model : model;
        return Pair.of(selected, transforms.handlePerspective(transform).getRight());
    }
}
