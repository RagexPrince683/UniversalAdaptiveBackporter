package ganymedes01.etfuturum.client;

import java.util.*;
import ganymedes01.etfuturum.UAB;
import ganymedes01.etfuturum.configuration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.*;

public final class ConfigGuiFactory implements IModGuiFactory {
    @Override public void initialize(Minecraft minecraft) { }
    @Override public boolean hasConfigGui() { return true; }
    @Override public GuiScreen createConfigGui(GuiScreen parent) { return new GuiConfig(parent, elements(), UAB.ID, false, false, "Universal Adaptive Backporter"); }
    @Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return Collections.emptySet(); }
    private static List<IConfigElement> elements() {
        List<IConfigElement> result = new ArrayList<>();
        net.minecraftforge.common.config.Configuration config = UabConfiguration.get().nativeConfig();
        List<String> categories = new ArrayList<>(FeatureRegistry.UPDATES.keySet()); categories.remove("general"); categories.add(0, "general");
        for (String category : categories) if (config.hasCategory(category) && (!config.getCategory(category).isEmpty() || !config.getCategory(category).getChildren().isEmpty()))
            result.add(new DummyConfigElement.DummyCategoryElement(FeatureRegistry.UPDATES.get(category), "uab.config." + category, new ConfigElement(config.getCategory(category)).getChildElements()));
        return result;
    }
}
