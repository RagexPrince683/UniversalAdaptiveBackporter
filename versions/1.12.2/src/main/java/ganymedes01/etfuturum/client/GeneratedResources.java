package ganymedes01.etfuturum.client;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import javax.imageio.ImageIO;
import com.google.gson.*;
import ganymedes01.etfuturum.*;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Adapts released model geometry and textures to the native registry and model format. */
final class GeneratedResources implements IResourcePack {
    private final ZipFile archive;
    private final ModernAudio audio;
    private final Map<String, byte[]> generated = new HashMap<>();
    GeneratedResources(File file) throws Exception { archive = new ZipFile(file); audio=new ModernAudio(Minecraft.getMinecraft().gameDir); }
    @SuppressWarnings("unchecked") static void install() {
        try {
            File file = ModernAssets.prepare(Minecraft.getMinecraft().gameDir);
            List<IResourcePack> packs = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
            GeneratedResources pack = new GeneratedResources(file);
            VanillaOverrides overrides=new VanillaOverrides();
            packs.add(overrides);packs.add(pack);
            // Forge retains the startup refresh list before pre-initialization runs.
            List<IResourcePack> startup = ReflectionHelper.getPrivateValue(net.minecraftforge.fml.client.FMLClientHandler.class, net.minecraftforge.fml.client.FMLClientHandler.instance(), "resourcePackList");
            if (startup != packs) {startup.add(overrides);startup.add(pack);}
            // The first reload already populated the per-domain managers before pre-init.
            ((net.minecraft.client.resources.SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).reloadResourcePack(overrides);
            ((net.minecraft.client.resources.SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).reloadResourcePack(pack);
            Minecraft.getMinecraft().getLanguageManager().onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());
        } catch (Exception failure) { throw new IllegalStateException("UAB could not prepare its Minecraft 1.21.11 resources; supply -Duab.assets=<client jar> for offline use", failure); }
    }
    private static String assetName(String name) {
        if(name.equals("mangrove_oak_boat"))return "mangrove_boat";
        if(name.startsWith("item_sign_"))return name.substring(10)+"_sign";
        if(name.startsWith("wall_sign_"))return name.substring(10)+"_wall_sign";
        if(name.startsWith("sign_"))return name.substring(5)+"_sign";
        if(name.endsWith("_record"))return "music_disc_"+(name.equals("five_record")?"5":name.substring(0,name.length()-7));
        if(name.endsWith("_underwater"))return name.substring(0,name.length()-11);
        if (name.endsWith("_double_slab")) return name.replace("_double_slab", "_slab");
        if (name.endsWith("_bricks_stairs")) return name.replace("_bricks_stairs", "_brick_stairs");
        if (name.endsWith("_tiles_stairs")) return name.replace("_tiles_stairs", "_tile_stairs");
        if (name.endsWith("_planks_stairs")) return name.replace("_planks_stairs", "_stairs");
        if (name.equals("chain")) return "iron_chain";
        if (name.equals("mangrove_sapling")) return "mangrove_propagule";
        return name;
    }
    private String modernTexture(String path) {
        if(path.equals("textures/entity/ghast/ghastling.png"))path="textures/entity/ghast/happy_ghast_baby.png";
        if(path.equals("textures/mob_effect/nautilus_breath.png"))path="textures/mob_effect/breath_of_the_nautilus.png";
        if (path.equals("textures/models/armor/netherite_layer_1.png")) path = "textures/entity/equipment/humanoid/netherite.png";
        if (path.equals("textures/models/armor/netherite_layer_2.png")) path = "textures/entity/equipment/humanoid_leggings/netherite.png";
        if(path.equals("textures/models/armor/turtle_layer_1.png"))path="textures/entity/equipment/humanoid/turtle_scute.png";
        return "assets/minecraft/" + path.replace("textures/blocks/", "textures/block/").replace("textures/items/", "textures/item/");
    }
    private InputStream language(String path) {
        if (!path.startsWith("lang/") || !path.endsWith(".lang")) return null;
        String language = path.substring(5, path.length() - 5);
        int underscore = language.indexOf('_');
        String legacy = underscore < 0 ? language : language.substring(0, underscore) + language.substring(underscore).toUpperCase(Locale.ROOT);
        byte[] cached = generated.get(path); if (cached != null) return new ByteArrayInputStream(cached);
        InputStream original = GeneratedResources.class.getResourceAsStream("/assets/etfuturum/lang/" + legacy + ".lang");
        if (original == null) return null;
        try (InputStream input = original; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; int length; while ((length = input.read(buffer)) != -1) bytes.write(buffer, 0, length);
            String text = new String(bytes.toByteArray(), StandardCharsets.UTF_8); StringBuilder merged = new StringBuilder(text).append('\n');
            Set<String> existing = new HashSet<>(); for (String line : text.split("\\r?\\n")) if (line.contains("=")) existing.add(line.substring(0, line.indexOf('=')));
            JsonObject modern = read("lang/" + language + ".json"); if (modern == null) modern = read("lang/en_us.json");
            if (modern != null) {
                for (Map.Entry<String, Block> block : Content.BLOCKS.entrySet()) appendName(merged, existing, modern, block.getValue().getTranslationKey() + ".name", "block.minecraft." + assetName(block.getKey()));
                for (Map.Entry<String, net.minecraft.item.Item> item : Content.ITEMS.entrySet()){appendName(merged, existing, modern, item.getValue().getTranslationKey() + ".name", "item.minecraft." + assetName(item.getKey()));if(item.getValue() instanceof ModernRecord){String track=((ModernRecord)item.getValue()).track;appendName(merged,existing,modern,"item.record."+track+".desc","jukebox_song.minecraft."+track);}if(item.getValue() instanceof VariantItem)for(String variant:((VariantItem)item.getValue()).variants)appendName(merged,existing,modern,"item.etfuturum."+variant+".name","item.minecraft."+variant);}
            }
            cached = merged.toString().getBytes(StandardCharsets.UTF_8); generated.put(path, cached); return new ByteArrayInputStream(cached);
        } catch (IOException failure) { throw new IllegalStateException("Cannot adapt UAB language " + path, failure); }
    }
    private static void appendName(StringBuilder output, Set<String> existing, JsonObject names, String key, String modernKey) {
        if (!existing.contains(key) && names.has(modernKey)) { output.append(key).append('=').append(names.get(modernKey).getAsString()).append('\n'); existing.add(key); }
    }
    @Override public InputStream getInputStream(ResourceLocation location) throws IOException {
        if(location.getNamespace().equals(ModernSounds.DOMAIN))return audio.open(location.getPath());
        String path = location.getPath();
        byte[] data = generated.get(path);
        if (data == null) {
            JsonElement json = generate(path);
            if (json != null) { data = json.toString().getBytes(StandardCharsets.UTF_8); generated.put(path, data); }
        }
        if (data != null) return new ByteArrayInputStream(data);
        InputStream lang = language(path); if (lang != null) return lang;
        ZipEntry entry = archive.getEntry(modernTexture(path));
        if (entry != null) return archive.getInputStream(entry);
        throw new FileNotFoundException(location.toString());
    }
    private JsonObject read(String path) {
        InputStream bundled = GeneratedResources.class.getResourceAsStream("/assets/etfuturum/" + path);
        if (bundled != null) try (Reader reader = new InputStreamReader(bundled, StandardCharsets.UTF_8)) { return new JsonParser().parse(reader).getAsJsonObject(); }
        catch (IOException failure) { throw new IllegalStateException("Cannot read native model " + path, failure); }
        ZipEntry entry = archive.getEntry("assets/minecraft/" + path);
        if (entry == null) return null;
        try (Reader reader = new InputStreamReader(archive.getInputStream(entry), StandardCharsets.UTF_8)) { return new JsonParser().parse(reader).getAsJsonObject(); }
        catch (IOException failure) { throw new IllegalStateException("Cannot read released model " + path, failure); }
    }
    private static String reference(String value, boolean texture, boolean variant) {
        if (value.startsWith("#")) return value;
        String path = value.startsWith("minecraft:") ? value.substring(10) : value;
        if (path.contains(":")) return value;
        if (path.startsWith("builtin/")) return "minecraft:" + path;
        if (texture) {
            path = path.replaceFirst("^block/", "blocks/").replaceFirst("^item/", "items/");
            if (!path.contains("/")) path = "blocks/" + path;
        }
        else if (variant && path.startsWith("block/")) path = path.substring(6);
        return "etfuturum:" + path;
    }
    private static JsonElement rewrite(JsonElement element, String key) {
        if (element.isJsonObject()) {
            JsonObject result = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals("textures")) {
                    JsonObject textures = new JsonObject();
                    JsonObject source = entry.getValue().getAsJsonObject();
                    for (Map.Entry<String, JsonElement> texture : source.entrySet()) {
                        String value = texture.getValue().getAsString(); Set<String> visited = new HashSet<>();
                        while (value.startsWith("#") && source.has(value.substring(1)) && visited.add(value)) value = source.get(value.substring(1)).getAsString();
                        textures.addProperty(texture.getKey(), reference(value, true, false));
                    }
                    result.add("textures", textures);
                } else result.add(entry.getKey(), rewrite(entry.getValue(), entry.getKey()));
            }
            // Flattened retained models contain resolved face paths. The 1.12 loader
            // resolves face strings as texture-table keys, so intern those paths.
            if(result.has("elements")){
                JsonObject textures=result.has("textures")?result.getAsJsonObject("textures"):new JsonObject();
                Map<String,String> direct=new LinkedHashMap<>();
                for(JsonElement part:result.getAsJsonArray("elements"))if(part.getAsJsonObject().has("faces"))for(Map.Entry<String,JsonElement> side:part.getAsJsonObject().getAsJsonObject("faces").entrySet()){
                    JsonObject face=side.getValue().getAsJsonObject();if(!face.has("texture"))continue;String value=face.get("texture").getAsString();if(value.startsWith("#"))continue;
                    String mapped=reference(value,true,false),slot=direct.get(mapped);if(slot==null){slot="uab_direct_"+direct.size();while(textures.has(slot))slot+="_";direct.put(mapped,slot);textures.addProperty(slot,mapped);}face.addProperty("texture","#"+slot);
                }
                result.add("textures",textures);
            }
            return result;
        }
        if (element.isJsonArray()) { JsonArray result = new JsonArray(); for (JsonElement item : element.getAsJsonArray()) result.add(rewrite(item, key)); return result; }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() && (key.equals("parent") || key.equals("model"))) return new JsonPrimitive(reference(element.getAsString(), false, key.equals("model")));
        return element;
    }
    @SuppressWarnings({"rawtypes", "unchecked"}) private static SortedMap<String, String> properties(IBlockState state) {
        SortedMap<String, String> result = new TreeMap<>();
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) result.put(entry.getKey().getName(), ((IProperty) entry.getKey()).getName(entry.getValue()));
        if(state.getBlock() instanceof AquaticPlant)result.remove("level");
        if(state.getBlock() instanceof CoralPlant&&!((CoralPlant)state.getBlock()).shape.equals("_wall_fan"))result.remove("facing");
        return result;
    }
    private static String variantKey(Map<String, String> values) { List<String> parts = new ArrayList<>(); values.forEach((key, value) -> parts.add(key + "=" + value)); return parts.isEmpty() ? "normal" : String.join(",", parts); }
    private static JsonObject model(String name) { JsonObject model = new JsonObject(); model.addProperty("model", "etfuturum:" + name); return model; }
    private JsonObject blockstates(String name, Block block) {
        if(name.equals("raw_adamantium_block"))return read("blockstates/"+name+".json");
        if(block instanceof BarrelBlock&&!name.equals("barrel"))return read("blockstates/"+name+".json");
        if(block instanceof ModdedOreBlock){JsonObject result=new JsonObject(),variants=new JsonObject();for(int meta=0;meta<16;meta++)variants.add("variant="+meta,model(((ModdedOreBlock)block).variant(meta)));result.add("variants",variants);return result;}
        if(name.equals("rose")||name.equals("old_gravel")||block instanceof LegacyDaylightDetector){JsonObject result=new JsonObject(),variants=new JsonObject();variants.add("normal",model(name));result.add("variants",variants);return result;}
        if(block instanceof MossCarpet){JsonObject variants=new JsonObject(),result=new JsonObject();variants.add("normal",model(assetName(name)));result.add("variants",variants);return result;}
        if(block instanceof BookStorage&&!((BookStorage)block).books){JsonObject result=new JsonObject(),variants=new JsonObject();for(IBlockState state:block.getBlockState().getValidStates()){JsonObject selected=model(name+(state.getValue(BookStorage.POWERED)?"_unconnected":"_unpowered"));selected.addProperty("y",(state.getValue(BookStorage.FACING).getHorizontalIndex()+2)%4*90);variants.add(variantKey(properties(state)),selected);}result.add("variants",variants);return result;}
        JsonObject released = read("blockstates/" + assetName(name) + ".json");
        if (released != null && released.has("multipart")) {
            if(name.equals("resin_clump")){JsonObject adapted=new JsonObject();JsonArray faces=new JsonArray();for(JsonElement element:released.getAsJsonArray("multipart")){JsonObject face=rewrite(element,"").getAsJsonObject();if(face.getAsJsonObject("apply").has("x")&&face.getAsJsonObject("apply").get("x").getAsInt()==90)continue;if(face.has("when")){JsonObject condition=face.getAsJsonObject("when");if(condition.has("down")&&condition.get("down").getAsString().equals("true"))continue;condition.remove("down");}faces.add(face);}adapted.add("multipart",faces);return adapted;}
            if (!(block instanceof BlockWall)) return rewrite(released, "").getAsJsonObject();
            JsonArray pieces = new JsonArray();
            for (JsonElement piece : released.getAsJsonArray("multipart")) {
                JsonObject part = rewrite(piece, "").getAsJsonObject(); boolean keep = true;
                if (part.has("when")) {
                    JsonObject conditions = part.getAsJsonObject("when");
                    for (Map.Entry<String, JsonElement> condition : conditions.entrySet()) {
                        String value = condition.getValue().getAsString();
                        if (value.equals("tall")) keep = false;
                        else if (value.equals("low")) condition.setValue(new JsonPrimitive("true"));
                        else if (value.equals("none")) condition.setValue(new JsonPrimitive("false"));
                    }
                }
                if (keep) pieces.add(part);
            }
            JsonObject wall = new JsonObject(); wall.add("multipart", pieces); return wall;
        }
        JsonObject variants = new JsonObject(), result = new JsonObject();
        for (IBlockState state : block.getBlockState().getValidStates()) {
            SortedMap<String, String> values = properties(state);
            JsonElement selected = null;
            if(block instanceof AncientCrop){AncientCrop crop=(AncientCrop)block;selected=model(crop.pitcher?"pitcher_crop_"+(state.getValue(AncientCrop.UPPER)?"top":"bottom")+"_stage_"+Math.max(state.getValue(AncientCrop.UPPER)?3:0,crop.stage(state)):state.getValue(BlockCrops.AGE)*3/8>=2?"torchflower":"torchflower_crop_stage"+(state.getValue(BlockCrops.AGE)*3/8));}
            else if(block instanceof Eyeblossom)selected=model(state.getValue(Eyeblossom.OPEN)?"open_eyeblossom":"closed_eyeblossom");
            else if(name.equals("pitcher_plant"))selected=model(state.getValue(BlockDoublePlant.HALF)==BlockDoublePlant.EnumBlockHalf.UPPER?"pitcher_plant_top":"pitcher_plant_bottom");
            else if (name.equals("kelp")) selected = model(state.getValue(KelpBlock.TOP) ? "kelp" : "kelp_plant");
            else if(block instanceof PowderSnow.Cauldron)selected=model(state.getValue(BlockCauldron.LEVEL)<3?"powder_snow_cauldron_level"+Math.max(1,state.getValue(BlockCauldron.LEVEL)):"powder_snow_cauldron_full");
            else if(block instanceof FilledCauldron)selected=model(name.equals("lava_cauldron")?"lava_cauldron":state.getValue(BlockCauldron.LEVEL)<3?"water_cauldron_level"+Math.max(1,state.getValue(BlockCauldron.LEVEL)):"water_cauldron_full");
            else if((name.equals("soul_torch")||name.equals("copper_torch"))&&state.getValue(BlockTorch.FACING)!=net.minecraft.util.EnumFacing.UP){net.minecraft.util.EnumFacing facing=state.getValue(BlockTorch.FACING);JsonObject wall=model(name.equals("copper_torch")?"copper_wall_torch":"soul_wall_torch");wall.addProperty("y",facing==net.minecraft.util.EnumFacing.EAST?0:facing==net.minecraft.util.EnumFacing.SOUTH?90:facing==net.minecraft.util.EnumFacing.WEST?180:270);selected=wall;}
            else if(block instanceof NetherVines)selected=model(name+(state.getValue(NetherVines.TIP)?"":"_plant"));
            else if(block instanceof Seagrass)selected=model(state.getValue(Seagrass.PART)==Seagrass.Part.SINGLE?"seagrass":state.getValue(Seagrass.PART)==Seagrass.Part.LOWER?"tall_seagrass_bottom":"tall_seagrass_top");
            else if (block instanceof BlockLog && state.getValue(BlockLog.LOG_AXIS) == BlockLog.EnumAxis.NONE) selected = model(name + "_bark");
            else if (released != null && released.has("variants")) {
                for (Map.Entry<String, JsonElement> entry : released.getAsJsonObject("variants").entrySet()) {
                    boolean match = true;
                    if (!entry.getKey().isEmpty()) for (String part : entry.getKey().split(",")) {
                        String[] property = part.split("=", 2);
                        String nativeValue = values.get(property[0]);
                        if(block instanceof CrafterBlock&&property[0].equals("orientation")){net.minecraft.util.EnumFacing direction=state.getValue(CrafterBlock.FACING);nativeValue=direction.getName()+ (direction.getAxis()==net.minecraft.util.EnumFacing.Axis.Y?"_north":"_up");}
                        if(block instanceof BeeHiveBlock&&property[0].equals("honey_level"))nativeValue=state.getValue(BeeHiveBlock.FULL)?"5":"0";
                        if(block instanceof AquaticPlant&&property[0].equals("waterlogged"))nativeValue=String.valueOf(((AquaticPlant)block).submerged);
                        if (block instanceof BlockSlab && property[0].equals("type")) nativeValue = ((BlockSlab) block).isDouble() ? "double" : state.getValue(BlockSlab.HALF).getName();
                        if (nativeValue != null && !nativeValue.equals(property[1]) || nativeValue == null && (property[0].equals("waterlogged") || property[0].equals("hanging")) && !property[1].equals("false")) match = false;
                    }
                    if (match) { selected = rewrite(entry.getValue(), ""); break; }
                }
            }
            if (selected == null) selected = model(assetName(name));
            variants.add(variantKey(values), selected);
        }
        result.add("variants", variants); return result;
    }
    private JsonElement generate(String path) {
        if (path.startsWith("blockstates/") && path.endsWith(".json")) {
            String name = path.substring(12, path.length() - 5); Block block = Content.BLOCKS.get(name);
            return block == null ? null : blockstates(name, block);
        }
        if ((!path.startsWith("models/block/") && !path.startsWith("models/item/")) || !path.endsWith(".json")) return null;
        boolean item = path.startsWith("models/item/");
        String name = path.substring(item ? 12 : 13, path.length() - 5);
        if(item&&(name.equals("lodestone_compass")||name.equals("recovery_compass"))){JsonObject compass=new JsonObject(),textures=new JsonObject();compass.addProperty("parent","minecraft:item/generated");textures.addProperty("layer0","etfuturum:items/"+(name.equals("recovery_compass")?"recovery_compass":"compass")+"_16");compass.add("textures",textures);JsonArray overrides=new JsonArray();for(int i=0;i<32;i++){JsonObject entry=new JsonObject(),predicate=new JsonObject();predicate.addProperty("angle",i/32D);entry.add("predicate",predicate);entry.addProperty("model","etfuturum:item/"+name+"_"+String.format(Locale.ROOT,"%02d",i));overrides.add(entry);}compass.add("overrides",overrides);return compass;}
        if(item&&(name.startsWith("lodestone_compass_")||name.startsWith("recovery_compass_"))&&name.matches("(?:lodestone|recovery)_compass_[0-3][0-9]")){JsonObject compass=new JsonObject(),textures=new JsonObject();compass.addProperty("parent","minecraft:item/generated");textures.addProperty("layer0","etfuturum:items/"+(name.startsWith("recovery")?"recovery_compass":"compass")+name.substring(name.length()-3));compass.add("textures",textures);return compass;}
        if(item&&Content.BLOCKS.get(name) instanceof CopperChestBlock){JsonObject chest=rewrite(read("models/item/template_chest.json"),"").getAsJsonObject();chest.addProperty("parent","minecraft:builtin/entity");return chest;}
        if(name.equals("raw_adamantium")||name.equals("raw_adamantium_block")){JsonObject own=read((item?"models/item/":"models/block/")+name+".json");if(own!=null)return own;}
        if(name.endsWith("_barrel")||name.endsWith("_barrel_open")){JsonObject own=read((item?"models/item/":"models/block/")+name+".json");if(own!=null)return own;}
        if(item&&(name.equals("barrel_upgrade")||name.equals("shulker_box_upgrade"))){JsonObject own=read("models/item/"+name+".json");if(own!=null)return own;}
        if(item&&(name.startsWith("shulker_")||name.startsWith("barrel_"))&&name.endsWith("_upgrade")){JsonObject own=read("models/item/"+name+".json");if(own!=null)return own;}
        if(item&&VariantItem.model(name)!=null){JsonObject variant=new JsonObject(),textures=new JsonObject();variant.addProperty("parent","minecraft:item/generated");textures.addProperty("layer0","etfuturum:items/"+VariantItem.model(name));variant.add("textures",textures);return variant;}
        if(item&&(name.equals("shulker_box")||name.equals("decorated_pot")||name.equals("copper_chest")||name.equals("copper_golem_statue"))){JsonObject pot=new JsonObject();pot.addProperty("parent","builtin/entity");return pot;}
        if(item&&(name.equals("eyeblossom")||name.equals("open_eyeblossom"))){JsonObject flower=new JsonObject(),textures=new JsonObject();flower.addProperty("parent","minecraft:item/generated");textures.addProperty("layer0","etfuturum:blocks/"+(name.equals("eyeblossom")?"closed_eyeblossom":name));flower.add("textures",textures);return flower;}
        if(item&&(name.equals("light")||name.matches("light_[0-9]{2}"))){JsonObject light=new JsonObject(),textures=new JsonObject();light.addProperty("parent","minecraft:item/generated");textures.addProperty("layer0","etfuturum:items/"+(name.equals("light")?"light_00":name));light.add("textures",textures);return light;}
        if(item&&(name.equals("beehive")||name.equals("bee_nest"))){JsonObject hive=new JsonObject(),entry=new JsonObject(),predicate=new JsonObject();hive.addProperty("parent","etfuturum:block/"+name+"_empty");predicate.addProperty("honey",1);entry.add("predicate",predicate);entry.addProperty("model","etfuturum:block/"+name+"_honey");JsonArray overrides=new JsonArray();overrides.add(entry);hive.add("overrides",overrides);return hive;}
        if(item&&name.equals("goat_horn")){JsonObject horn=rewrite(read("models/item/goat_horn.json"),"").getAsJsonObject(),entry=new JsonObject(),predicate=new JsonObject();predicate.addProperty("tooting",1);entry.add("predicate",predicate);entry.addProperty("model","etfuturum:item/tooting_goat_horn");JsonArray overrides=new JsonArray();overrides.add(entry);horn.add("overrides",overrides);return horn;}
        if(item&&name.equals("crossbow")){
            JsonObject base=rewrite(read("models/item/crossbow.json"),"").getAsJsonObject();JsonArray overrides=new JsonArray();
            for(int stage=0;stage<4;stage++){JsonObject entry=new JsonObject(),predicate=new JsonObject();if(stage<3){predicate.addProperty("pulling",1);if(stage>0)predicate.addProperty("pull",stage==1?.58:.9);entry.addProperty("model","etfuturum:item/crossbow_pulling_"+stage);}else{predicate.addProperty("charged",1);entry.addProperty("model","etfuturum:item/crossbow_arrow");}entry.add("predicate",predicate);overrides.add(entry);}base.add("overrides",overrides);return base;
        }
        if (item) {
            JsonObject definition = read("items/" + assetName(name) + ".json");
            JsonObject itemModel = definition == null ? null : definition.getAsJsonObject("model");
            if (itemModel != null && itemModel.has("type") && itemModel.get("type").getAsString().equals("minecraft:model") && !itemModel.get("model").getAsString().equals("minecraft:item/" + assetName(name))) {
                JsonObject parent = new JsonObject(); parent.addProperty("parent", reference(itemModel.get("model").getAsString(), false, false)); return parent;
            }
        }
        if(name.equals("rose")||name.equals("old_gravel")||name.equals("daylight_detector")){JsonObject own=new JsonObject(),textures=new JsonObject();if(name.equals("daylight_detector"))own.addProperty("parent","minecraft:block/daylight_detector");else if(name.equals("rose")){own.addProperty("parent",item?"minecraft:item/generated":"minecraft:block/cross");textures.addProperty(item?"layer0":"cross","etfuturum:blocks/flower_rose");}else{own.addProperty("parent",item?"etfuturum:block/old_gravel":"minecraft:block/cube_all");if(!item)textures.addProperty("all","etfuturum:blocks/old_gravel");}if(textures.size()>0)own.add("textures",textures);return own;}
        if(Content.BLOCKS.get(name) instanceof ModdedOreBlock){JsonObject alias=new JsonObject();alias.addProperty("parent","etfuturum:block/"+((ModdedOreBlock)Content.BLOCKS.get(name)).variant(0));return alias;}
        String specialTexture=SpecialOres.texture(name);if(specialTexture!=null){JsonObject ore=new JsonObject(),textures=new JsonObject();ore.addProperty("parent",item?"etfuturum:block/"+name:"minecraft:block/cube_all");if(!item){textures.addProperty("all",specialTexture);ore.add("textures",textures);JsonObject cube=new JsonObject(),faces=new JsonObject();JsonArray from=new JsonArray(),to=new JsonArray(),elements=new JsonArray();for(int axis=0;axis<3;axis++){from.add(0);to.add(16);}cube.add("from",from);cube.add("to",to);for(String face:new String[]{"down","up","north","south","west","east"}){JsonObject side=new JsonObject();side.addProperty("texture","#all");side.addProperty("cullface",face);side.addProperty("tintindex",0);faces.add(face,side);}cube.add("faces",faces);elements.add(cube);ore.add("elements",elements);}return ore;}
        if(OreCompatibility.blockModel(name)){JsonObject ore=new JsonObject(),textures=new JsonObject();ore.addProperty("parent",item?"etfuturum:block/"+name:"minecraft:block/cube_all");if(!item){textures.addProperty("all","etfuturum:blocks/"+name);ore.add("textures",textures);}return ore;}
        JsonObject released = read((item ? "models/item/" : "models/block/") + assetName(name) + ".json");
        if (released != null) return rewrite(released, "");
        JsonObject result = new JsonObject(), textures = new JsonObject();
        if (!item && name.endsWith("_bark") && Content.BLOCKS.containsKey(name.substring(0, name.length() - 5))) {
            result.addProperty("parent", "minecraft:block/cube_all"); String trunk=name.substring(0,name.length()-5);if(trunk.endsWith("_hyphae"))trunk=trunk.replace("_hyphae","_stem");if(trunk.endsWith("_wood"))trunk=trunk.replace("_wood","_log");textures.addProperty("all", "etfuturum:blocks/" + trunk);
        } else if (item && Content.BLOCKS.containsKey(name)) result.addProperty("parent", "etfuturum:block/" + (name.equals("sweet_berry_bush") ? "sweet_berry_bush_stage3" : assetName(name)));
        else if (item && Content.ITEMS.containsKey(name)) { result.addProperty("parent", "minecraft:item/generated"); textures.addProperty("layer0", "etfuturum:items/" + assetName(name)); }
        else return null;
        if (textures.size() != 0) result.add("textures", textures);
        return result;
    }
    @Override public boolean resourceExists(ResourceLocation location) {
        if(location.getNamespace().equals(ModernSounds.DOMAIN))return audio.contains(location.getPath());
        if (!location.getNamespace().equals(UAB.ID)) return false;
        if (generated.containsKey(location.getPath()) || generate(location.getPath()) != null) return true;
        try (InputStream lang = language(location.getPath())) { if (lang != null) return true; } catch (IOException failure) { return false; }
        return archive.getEntry(modernTexture(location.getPath())) != null;
    }
    @Override public Set<String> getResourceDomains() { return new HashSet<>(Arrays.asList(UAB.ID,ModernSounds.DOMAIN)); }
    @Override public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer serializer, String section) { return null; }
    @Override public BufferedImage getPackImage() throws IOException { return ImageIO.read(GeneratedResources.class.getResourceAsStream("/assets/etfuturum/logo.png")); }
    @Override public String getPackName() { return "UAB native 1.12.2 models and Minecraft 1.21.11 resources"; }
}
