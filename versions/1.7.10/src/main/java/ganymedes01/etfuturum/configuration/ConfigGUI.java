package ganymedes01.etfuturum.configuration;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import ganymedes01.etfuturum.Tags;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ConfigGUI extends GuiConfig {

	public ConfigGUI(GuiScreen parent) {
		super(parent, getElements(), Tags.MOD_ID, false, false, "Universal Adaptive Backporter");
	}

	@SuppressWarnings("rawtypes")
	private static List<IConfigElement> getElements() {
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		Configuration config = UabConfiguration.get().nativeConfig();
		for (String category : FeatureRegistry.UPDATES.keySet()) {
			if (config.hasCategory(category) && (!config.getCategory(category).isEmpty() || !config.getCategory(category).getChildren().isEmpty())) list.add(new ConfigElement(config.getCategory(category)));
		}
		return list;
	}

	@Override
	protected void actionPerformed(net.minecraft.client.gui.GuiButton button) {
		super.actionPerformed(button);
		if (button.id == 2000) ConfigBase.reloadClient();
	}
}
