package ganymedes01.etfuturum.client;

import net.minecraft.client.model.*;
import net.minecraft.util.ResourceLocation;

final class TridentModel {
    static final ResourceLocation TEXTURE = new ResourceLocation("etfuturum", "textures/entity/trident.png");
    private final ModelRenderer root;
    TridentModel() {
        ModelBase base = new ModelBase() { }; base.textureWidth = base.textureHeight = 32;
        root = new ModelRenderer(base, 0, 6); root.addBox(-.5F, 2, -.5F, 1, 25, 1);
        ModelRenderer cross = new ModelRenderer(base, 4, 0); cross.addBox(-1.5F, 0, -.5F, 3, 2, 1); root.addChild(cross);
        for (int x : new int[]{-2, 0, 2}) { ModelRenderer spike = new ModelRenderer(base, x == 0 ? 0 : 4, x == 0 ? 0 : 3); spike.addBox(x - .5F, x == 0 ? -4 : -3, -.5F, 1, 4, 1); root.addChild(spike); }
    }
    void render() { root.render(.0625F); }
}
