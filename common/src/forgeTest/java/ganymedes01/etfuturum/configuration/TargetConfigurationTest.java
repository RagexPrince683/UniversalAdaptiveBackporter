package ganymedes01.etfuturum.configuration;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraftforge.common.config.Configuration;

public final class TargetConfigurationTest {
    private static int assertions;
    private static void check(boolean value, String message) { assertions++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        String target = System.getProperty("uab.test.target", "1.12.2");
        Class<?> injection = Class.forName(target.equals("1.12.2") ? "net.minecraftforge.fml.relauncher.FMLInjectionData" : "cpw.mods.fml.relauncher.FMLInjectionData");
        java.lang.reflect.Field home = injection.getDeclaredField("minecraftHome"); home.setAccessible(true); home.set(null, new File(System.getProperty("java.io.tmpdir")));
        File directory = Files.createTempDirectory("uab-112-configuration-").toFile();
        File file = new File(directory, "uab.cfg");
        Configuration old = new Configuration(file);
        old.get("update_1_14.world_save_thumbnails", "enabled", false).set(false);
        old.get("update_1_9.note_block_notes", "enabled", true).set(false);
        old.get("update_1_11.recipe_for_totem", "enabled", true).set(true);
        old.get("update_1_11", "enabled", false).set(false);
        old.get("update_1_9.book_page_turn", "enabled", true).set(false);
        old.get("update_1_14.book_page_turn", "enabled", true).set(true);
        old.save(); byte[] before = Files.readAllBytes(file.toPath());
        UabConfiguration.open(directory, true, target);
        UabConfiguration config = UabConfiguration.get();
        check(config.bool("mixins", "worldSaveThumbnails", false) == target.equals("1.12.2"), "native thumbnails cannot be disabled by legacy setting");
        check(!config.bool("blocksitems", "newBoatEntityBlacklistAsWhitelist", false), "native rowing does not force the custom rider whitelist on");
        check(!config.bool("sounds", "noteBlockNotes", true), "mixed-version control retains explicit disable");
        check(!config.bool("functions", "enableRecipeForTotem", true), "old disabled update keeps moved custom recipe disabled");
        check(config.bool("sounds", "bookPageTurn", true), "new location takes precedence over stale old location");
        check(Arrays.equals(before, Files.readAllBytes(new File(file + ".catalog-v2.bak").toPath())), "category backup preserves exact bytes");
        check(!config.nativeConfig().hasCategory("update_1_9.note_block_notes"), "stale setting removed after migration");
        check(!config.nativeConfig().hasKey("update_1_9.world_save_thumbnails", "enabled") || config.nativeConfig().getCategory("update_1_9.world_save_thumbnails").get("enabled").getString().equals("false"), "native preference retained without reinterpretation");
        config.finish();
        UabConfiguration.open(directory, true, target);
        check(!UabConfiguration.get().bool("sounds", "noteBlockNotes", true), "migrated disable persists on restart");
        check(Arrays.equals(before, Files.readAllBytes(new File(file + ".catalog-v2.bak").toPath())), "restart never overwrites backup");
        File complete = Files.createTempDirectory("uab-all-category-migrations-").toFile();
        Configuration all = new Configuration(new File(complete, "uab.cfg"));
        Map<String, String> migrated = new LinkedHashMap<>();
        for (FeatureRegistry.Option option : FeatureRegistry.options()) if (!option.previousLocation.isEmpty()) {
            int dot = option.previousLocation.lastIndexOf('.');
            all.get(option.previousLocation.substring(0, dot), option.previousLocation.substring(dot + 1), "retained-" + migrated.size()).set("retained-" + migrated.size());
            migrated.put(option.id, "retained-" + migrated.size());
        }
        all.get("general.unrelated", "keep", "untouched"); all.save();
        UabConfiguration.open(complete, true, target);
        Configuration result = UabConfiguration.get().nativeConfig();
        for (Map.Entry<String, String> entry : migrated.entrySet()) {
            int dot = entry.getKey().lastIndexOf('.');
            check(result.hasKey(entry.getKey().substring(0, dot), entry.getKey().substring(dot + 1)), "moved location exists: " + entry.getKey());
            check(result.getCategory(entry.getKey().substring(0, dot)).get(entry.getKey().substring(dot + 1)).getString().equals(entry.getValue()), "moved value retained: " + entry.getKey());
        }
        check(result.getCategory("general.unrelated").get("keep").getString().equals("untouched"), "unrelated settings retained");
        File lists = Files.createTempDirectory("uab-list-category-migration-").toFile();
        Configuration oldList = new Configuration(new File(lists, "uab.cfg"));
        String[] bans = {"example:container", "example:other_item"};
        oldList.get("update_1_9.shulker_bans", "value", bans); oldList.save();
        UabConfiguration.open(lists, true, target);
        String[] actual = UabConfiguration.get().property("functions", "shulkerBans", new String[0], net.minecraftforge.common.config.Property.Type.STRING).getStringList();
        check(Arrays.equals(bans, actual), "moved storage policy retains its list and order");
        UabConfiguration.get().finish(); UabConfiguration.open(lists, true, target);
        check(Arrays.equals(bans, UabConfiguration.get().property("functions", "shulkerBans", new String[0], net.minecraftforge.common.config.Property.Type.STRING).getStringList()), "moved list survives restart");
        File appearanceDirectory=Files.createTempDirectory("uab-animal-appearance-").toFile();
        UabConfiguration.open(appearanceDirectory,true,target);config=UabConfiguration.get();
        check(!config.bool("content","spring_animal_renders",true),"new client keeps native animal appearances even when caller fallback is true");
        check(config.bool("content","spring_to_life",true),"appearance default does not disable Spring gameplay");
        config.finish();String gameplay=config.handshake();
        FeatureRegistry.Option appearance=FeatureRegistry.option("content","spring_animal_renders");
        config.nativeConfig().get(appearance.category,appearance.field,false).set(true);config.reloadClient();
        check(config.bool("content","spring_animal_renders",false),"animal appearance override can be explicitly enabled");
        check(gameplay.equals(config.handshake()),"client appearance choice does not change server handshake");
        config.finish();UabConfiguration.open(appearanceDirectory,true,target);
        check(UabConfiguration.get().bool("content","spring_animal_renders",false),"explicit opt-in persists across restart");
        File appearanceServer=Files.createTempDirectory("uab-animal-appearance-server-").toFile();
        UabConfiguration.open(appearanceServer,false,target);config=UabConfiguration.get();config.bool("content","spring_animal_renders",true);config.finish();
        check(!config.nativeConfig().hasCategory(appearance.category),"dedicated server omits client appearance option");
        File independent=Files.createTempDirectory("uab-independent-generation-netherite-").toFile();
        Configuration controls=new Configuration(new File(independent,"uab.cfg"));
        controls.get("update_1_21_9.copper_age","copper_tools",true).set(false);
        controls.get("update_1_21_9.copper_age","copper_armor",true).set(false);
        controls.get("update_1_16.netherite","netherite_tools",true).set(false);
        controls.get("update_1_16.netherite","netherite_armor",true).set(false);
        controls.get("update_1_16.netherite","netherite_resources",true).set(false);
        controls.get("update_1_16.netherite","netherite_storage",true).set(false);
        controls.get("update_1_17.dripstone","dripstoneChunkChance",4).set(0);
        controls.get("update_1_13.kelp","kelpAttempts",12).set(0);
        controls.save();UabConfiguration.open(independent,false,target);config=UabConfiguration.get();
        GenerationSettings generation=new GenerationSettings();
        check(!CopperSettings.enabled("copper_pickaxe")&&!CopperSettings.enabled("copper_sword")&&!CopperSettings.enabled("copper_chestplate"),"copper player tools and armor can be disabled independently");
        check(CopperSettings.enabled("copper_ingot")&&CopperSettings.enabled("copper_block")&&CopperSettings.enabled("copper_horse_armor")&&CopperSettings.enabled("copper_lantern"),"copper resources, building blocks, fixtures and horse armor remain available");
        check(generation.dripstoneChance==0&&generation.kelpAttempts==0,"generation can be disabled independently of content");
        check(!NetheriteSettings.enabled("netherite_pickaxe")&&!NetheriteSettings.enabled("netherite_chestplate"),"overlapping native gear can be disabled");
        check(!NetheriteSettings.enabled("netherite_ingot")&&!NetheriteSettings.enabled("netherite_block"),"resource and storage acquisition can be disabled");
        check(NetheriteSettings.enabled("ancient_debris")&&NetheriteSettings.enabled("netherite_stairs")&&NetheriteSettings.enabled("netherite_spear")&&NetheriteSettings.enabled("netherite_horse_armor"),"independent netherite progression and extra equipment remain available");
        config.finish();UabConfiguration.open(independent,false,target);generation=new GenerationSettings();
        check(generation.dripstoneChance==0&&generation.kelpAttempts==0&&!NetheriteSettings.enabled("netherite_pickaxe"),"independent settings persist through restart");
        File kelpDirectory=Files.createTempDirectory("uab-independent-kelp-").toFile();
        Configuration kelp=new Configuration(new File(kelpDirectory,"uab.cfg"));
        kelp.get("update_1_13.kelp","enabled",false).set(false);
        kelp.get("general.compatibility.bop","enabled",false).set(false);kelp.save();
        UabConfiguration.open(kelpDirectory,false,target);config=UabConfiguration.get();
        check(!config.bool("content","kelp",true),"UAB kelp plants can be disabled");
        check(config.bool("content","dried_kelp",true)&&config.bool("content","dried_kelp_block",true),"dried kelp survives plant disable");
        check(!config.bool("modcompat","provider_bop",true)&&config.bool("modcompat","provider_undergroundbiomes",true),"providers disable independently");
        config.finish();UabConfiguration.open(kelpDirectory,false,target);config=UabConfiguration.get();
        check(!config.bool("content","kelp",true)&&config.bool("content","dried_kelp",true),"kelp split persists on restart");
        System.out.println("UAB " + target + " category migration: " + assertions + " assertions");
    }
}
