package ganymedes01.etfuturum.client.renderer.entity;

import ganymedes01.etfuturum.entities.EntityNewSnowGolem;
import net.minecraft.client.renderer.entity.RenderSnowMan;
import net.minecraft.entity.monster.EntitySnowman;

public class NewSnowGolemRenderer extends RenderSnowMan {

	@Override
	protected void renderEquippedItems(EntitySnowman entity, float partialTickTime) {
		if (((EntityNewSnowGolem) entity).hasPumpkin())
			super.renderEquippedItems(entity, partialTickTime);
	}
}