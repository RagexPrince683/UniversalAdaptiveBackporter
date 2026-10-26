package ganymedes01.etfuturum.client;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.configuration.configs.ConfigFunctions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// Inspired by ResourceManagerHelper in fabric-resource-loader-v0

public abstract class BuiltInResourcePack extends AbstractResourcePack {

	private static final Splitter entryNameSplitter = Splitter.on('/').omitEmptyStrings().limit(5);

	private final String modid;
	private final String id;
	protected boolean resourcesEnabled = true;
	protected boolean langEnabled = true;

	/**
	 * <p>Register a built-in resource pack. This is a resource pack located in the JAR at {@code "resourcepacks/<id>"}.
	 *
	 * <p>The resource pack is "invisible", it will not show up in the resource pack GUI.
	 *
	 * @param id The name of the resource pack.
	 */
	public static BuiltInResourcePack register(String id) {
		BuiltInResourcePack rp = BuiltInResourcePack.of(Loader.instance().activeModContainer().getSource(), Loader.instance().activeModContainer().getModId(), id).setResourcesEnabled();
		if (rp.isAllEnabled()) {
			inject(rp);
		}
		return rp;
	}

	private static BuiltInResourcePack of(File file, String modid, String id) {
		if (file.isDirectory()) {
			return new BuiltInFolderResourcePack(file, modid, id);
		} else {
			return new BuiltInFileResourcePack(file, modid, id);
		}
	}

	public BuiltInResourcePack(File file, String modid, String id) {
		super(file);
		this.modid = modid;
		this.id = id;
	}

	@Override
	public String getPackName() {
		return modid + "/" + id;
	}

	@Override
	public IMetadataSection getPackMetadata(IMetadataSerializer p_135058_1_, String p_135058_2_) throws IOException {
		return null;
	}

	@Override
	public BufferedImage getPackImage() throws IOException {
		return null;
	}

	protected String getRootPath() {
		return "resourcepacks/" + id + "/";
	}

	protected void addNamespaceIfLowerCase(Set<String> set, String ns) {
		if (!ns.equals(ns.toLowerCase())) {
			this.logNameNotLowercase(ns);
		} else {
			set.add(ns);
		}
	}

	public BuiltInResourcePack setResourcesEnabled() {
		this.resourcesEnabled = ConfigFunctions.enableNewTextures;
		this.langEnabled = ConfigFunctions.enableLangReplacements;
		return this;
	}

	private boolean isAllEnabled() {
		return resourcesEnabled || langEnabled;
	}

	protected boolean isResourceEnabled(String name) {
		return name.endsWith(".lang") ? langEnabled : resourcesEnabled;
	}

	protected InputStream filterLang(InputStream input) throws IOException {
		List<String> ignoredKeys = Lists.newArrayList();
		if (!ConfigBlocksItems.enableDyedBeds) {
			ignoredKeys.add("item.bed.name");
			ignoredKeys.add("tile.bed.name");
		}
		if (!ConfigBlocksItems.enableVanillaSigns) {
			ignoredKeys.add("item.sign.name");
			ignoredKeys.add("tile.sign.name");
		}
		if (!ConfigBlocksItems.enableVanillaDoors) {
			ignoredKeys.add("item.doorWood.name");
			ignoredKeys.add("tile.doorWood.name");
		}
		if (!ConfigBlocksItems.enableVanillaTrapdoors) ignoredKeys.add("tile.trapdoor.name");
		if (!ConfigBlocksItems.enableVanillaFences) {
			ignoredKeys.add("tile.fence.name");
			ignoredKeys.add("tile.fenceGate.name");
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("#") || line.length() == 0) continue;
			boolean ignored = false;
			for (String key : ignoredKeys) {
				if (line.startsWith(key)) {
					ignored = true;
					break;
				}
			}
			if (!ignored) output.write((line + "\n").getBytes());
		}
		reader.close();
		return new ByteArrayInputStream(output.toByteArray());
	}

	private static void inject(IResourcePack resourcePack) {
		List<IResourcePack> defaultResourcePacks = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
		defaultResourcePacks.add(resourcePack);
		IResourceManager resMan = Minecraft.getMinecraft().getResourceManager();
		if (resMan instanceof SimpleReloadableResourceManager) {
			((SimpleReloadableResourceManager) resMan).reloadResourcePack(resourcePack);
		}
	}

	private static class BuiltInFileResourcePack extends BuiltInResourcePack {

		private final ZipFile zipFile;

		public BuiltInFileResourcePack(File file, String modid, String id) {
			super(file, modid, id);
			try {
				this.zipFile = new ZipFile(file);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Set<String> getResourceDomains() {
			Set<String> domains = new HashSet<>();

			Enumeration<? extends ZipEntry> en = zipFile.entries();
			while (en.hasMoreElements()) {
				ZipEntry entry = en.nextElement();
				if (entry.getName().startsWith(getRootPath() + "assets/")) {
					List<String> nameParts = Lists.newArrayList(entryNameSplitter.split(entry.getName()));
					if (nameParts.size() > 3) {
						addNamespaceIfLowerCase(domains, nameParts.get(3));
					}
				}
			}
			return domains;
		}

		@Override
		protected InputStream getInputStreamByName(String name) throws IOException {
			InputStream input = zipFile.getInputStream(zipFile.getEntry(getRootPath() + name));
			return name.endsWith(".lang") ? filterLang(input) : input;
		}

		@Override
		protected boolean hasResourceName(String name) {
			return isResourceEnabled(name) && zipFile.getEntry(getRootPath() + name) != null;
		}

	}

	private static class BuiltInFolderResourcePack extends BuiltInResourcePack {

		public BuiltInFolderResourcePack(File file, String modid, String id) {
			super(file, modid, id);
		}

		@Override
		public Set<String> getResourceDomains() {
			Set<String> domains = new HashSet<>();

			File assetsDir = new File(this.resourcePackFile, getRootPath() + "assets/");
			if (assetsDir.isDirectory()) {
				File[] files = assetsDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
				for (File file : files) {
					addNamespaceIfLowerCase(domains, file.getName());
				}
			}

			return domains;
		}

		@Override
		protected InputStream getInputStreamByName(String name) throws IOException {
			InputStream input = new BufferedInputStream(Files.newInputStream(new File(this.resourcePackFile, getRootPath() + "/" + name).toPath()));
			return name.endsWith(".lang") ? filterLang(input) : input;
		}

		@Override
		protected boolean hasResourceName(String name) {
			return isResourceEnabled(name) && new File(this.resourcePackFile, getRootPath() + "/" + name).isFile();
		}

	}

}
