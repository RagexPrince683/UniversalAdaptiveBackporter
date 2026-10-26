package ganymedes01.etfuturum.client.sound;

import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.client.EndFlashState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class EndFlashSound extends MovingSound {
	private int age;

	public EndFlashSound() {
		super(new ResourceLocation(Tags.MC_ASSET_VER + ":weather.end_flash"));
		this.volume = 0.0F;
		this.field_147666_i = ISound.AttenuationType.NONE;
		updatePosition();
	}

	@Override
	public void update() {
		updatePosition();
		if (++age == EndFlashState.SOUND_DELAY_IN_TICKS) this.volume = 1.0F;
	}

	private void updatePosition() {
		Entity camera = Minecraft.getMinecraft().renderViewEntity;
		if (camera == null) return;
		double pitch = Math.toRadians(EndFlashState.INSTANCE.getXAngle());
		double yaw = Math.toRadians(EndFlashState.INSTANCE.getYAngle());
		double horizontal = Math.sin(pitch) * 10.0D;
		this.xPosF = (float) (camera.posX + Math.cos(yaw) * horizontal);
		this.yPosF = (float) (camera.posY + Math.cos(pitch) * 10.0D);
		this.zPosF = (float) (camera.posZ + Math.sin(yaw) * horizontal);
	}
}
