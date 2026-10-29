package ganymedes01.etfuturum.client.renderer.block;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Released vanilla elements prepared once per atlas reload, emitted through the native chunk tessellator. */
public final class ReleasedBlockModel {
	private static final String[] SIDES = {"down", "up", "north", "south", "west", "east"};
	private final List<Face> faces = new ArrayList<>();
    private boolean tinted;
	public final IIcon particle;

	public ReleasedBlockModel(IIconRegister atlas, String name) { this(atlas, name, 0); }
	public ReleasedBlockModel(IIconRegister atlas, String name, int rotation) {
		this(atlas, name, rotation, 1);
	}
	public ReleasedBlockModel(IIconRegister atlas, String name, int rotation, int facing) {
		try (Reader reader = new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(
				new ResourceLocation("etfuturum", "models/block/" + name + ".json")).getInputStream(), StandardCharsets.UTF_8)) {
			JsonObject model = new JsonParser().parse(reader).getAsJsonObject();
			JsonObject textures = model.getAsJsonObject("textures");
			particle = atlas.registerIcon(texture(textures, "#particle"));
			for (JsonElement value : model.getAsJsonArray("elements")) {
				JsonObject element = value.getAsJsonObject();
				double[] from = vector(element.getAsJsonArray("from")), to = vector(element.getAsJsonArray("to"));
				JsonObject definitions = element.getAsJsonObject("faces");
				for (int side = 0; side < SIDES.length; side++) if (definitions.has(SIDES[side])) {
					JsonObject definition = definitions.getAsJsonObject(SIDES[side]);
					JsonArray uv = definition.getAsJsonArray("uv");
					if (uv == null) throw new IllegalArgumentException("Explicit UV required: " + name);
					double[][] vertices = vertices(side, from, to);
					if (element.has("rotation")) rotate(vertices, element.getAsJsonObject("rotation"));
					for (double[] vertex : vertices) for (int turn = 0; turn < rotation; turn++) { double oldX = vertex[0]; vertex[0] = 1 - vertex[2]; vertex[2] = oldX; }
					for (double[] vertex : vertices) orient(vertex, facing);
					int cull = -1;
					if (definition.has("cullface")) cull = Arrays.asList(SIDES).indexOf(definition.get("cullface").getAsString());
					int orientedSide = orientSide(rotateSide(side, rotation), facing);
					float shade = element.has("shade") && !element.get("shade").getAsBoolean() ? 1 : orientedSide == 0 ? .5F : orientedSide == 1 ? 1 : orientedSide < 4 ? .8F : .6F;
					tinted|=definition.has("tintindex");
                    faces.add(new Face(vertices, uv, definition.has("rotation") ? definition.get("rotation").getAsInt() / 90 : 0,
							atlas.registerIcon(texture(textures, definition.get("texture").getAsString())), orientedSide, cull < 0 ? -1 : orientSide(rotateSide(cull, rotation), facing), shade,definition.has("tintindex")));
				}
			}
		} catch (IOException exception) { throw new IllegalStateException("Cannot load released block model " + name, exception); }
	}
	private static void orient(double[] v, int facing) {
		double x = v[0], y = v[1], z = v[2];
		switch (facing) {
			case 0: v[1] = 1 - y; v[2] = 1 - z; break;
			case 2: v[1] = z; v[2] = 1 - y; break;
			case 3: v[1] = 1 - z; v[2] = y; break;
			case 4: v[0] = 1 - y; v[1] = x; break;
			case 5: v[0] = y; v[1] = 1 - x; break;
		}
	}
	private static int orientSide(int side, int facing) {
		ForgeDirection d = ForgeDirection.getOrientation(side);
		double[] v = {.5 + d.offsetX * .5, .5 + d.offsetY * .5, .5 + d.offsetZ * .5}; orient(v, facing);
		for (int i = 0; i < 6; i++) { ForgeDirection n = ForgeDirection.getOrientation(i); if (v[0] == .5 + n.offsetX * .5 && v[1] == .5 + n.offsetY * .5 && v[2] == .5 + n.offsetZ * .5) return i; }
		throw new IllegalArgumentException("Invalid model face");
	}
	private static int rotateSide(int side, int rotation) { int[] next = {0, 1, 5, 4, 2, 3}; for (int i = 0; i < rotation; i++) side = next[side]; return side; }
	private static String texture(JsonObject textures, String key) {
		for (int depth = 0; key.startsWith("#") && depth < 16; depth++) key = textures.get(key.substring(1)).getAsString();
		if (key.startsWith("#")) throw new IllegalArgumentException("Cyclic block texture " + key);
		return key.replace("minecraft:", "").replace("block/", "");
	}
	private static double[] vector(JsonArray values) { return new double[]{values.get(0).getAsDouble() / 16, values.get(1).getAsDouble() / 16, values.get(2).getAsDouble() / 16}; }
	private static double[][] vertices(int side, double[] a, double[] b) {
		double x = a[0], y = a[1], z = a[2], X = b[0], Y = b[1], Z = b[2];
		return switch (side) {
			case 0 -> new double[][]{{x,y,Z},{x,y,z},{X,y,z},{X,y,Z}};
			case 1 -> new double[][]{{X,Y,Z},{X,Y,z},{x,Y,z},{x,Y,Z}};
			case 2 -> new double[][]{{X,Y,z},{X,y,z},{x,y,z},{x,Y,z}};
			case 3 -> new double[][]{{x,Y,Z},{x,y,Z},{X,y,Z},{X,Y,Z}};
			case 4 -> new double[][]{{x,Y,z},{x,y,z},{x,y,Z},{x,Y,Z}};
			default -> new double[][]{{X,Y,Z},{X,y,Z},{X,y,z},{X,Y,z}};
		};
	}
	private static void rotate(double[][] vertices, JsonObject rotation) {
		double[] origin = vector(rotation.getAsJsonArray("origin"));
		double angle = Math.toRadians(rotation.get("angle").getAsDouble()), sin = Math.sin(angle), cos = Math.cos(angle);
		int axis = "xyz".indexOf(rotation.get("axis").getAsString());
		int a = (axis + 1) % 3, b = (axis + 2) % 3;
		for (double[] vertex : vertices) {
			double va = vertex[a] - origin[a], vb = vertex[b] - origin[b];
			vertex[a] = origin[a] + va * cos - vb * sin; vertex[b] = origin[b] + va * sin + vb * cos;
		}
	}
	public void render(Block block, IBlockAccess world, int x, int y, int z, IIcon override) {
		Tessellator tess = Tessellator.instance;
        int tint=tinted?(world==null?block.getRenderColor(0):block.colorMultiplier(world,x,y,z)):0xffffff;
		if (world != null) tess.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
		for (Face face : faces) {
			if (world != null && face.cull >= 0) {
				ForgeDirection direction = ForgeDirection.getOrientation(face.cull);
				if (world.getBlock(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ).isOpaqueCube()) continue;
			}
			if (world == null) { ForgeDirection normal = ForgeDirection.getOrientation(face.side); tess.setNormal(normal.offsetX, normal.offsetY, normal.offsetZ); }
			tess.setColorOpaque_F(face.shade*(face.tinted?(tint>>16&255)/255F:1),face.shade*(face.tinted?(tint>>8&255)/255F:1),face.shade*(face.tinted?(tint&255)/255F:1));
			IIcon icon = override == null ? face.icon : override;
			for (int i = 0; i < 4; i++) {
				int uv = (i + face.rotation) & 3;
				double u = uv < 2 ? face.uv[0] : face.uv[2], v = uv == 0 || uv == 3 ? face.uv[1] : face.uv[3];
				tess.addVertexWithUV(x + face.vertices[i][0], y + face.vertices[i][1], z + face.vertices[i][2], icon.getInterpolatedU(u), icon.getInterpolatedV(v));
			}
		}
	}
	private static final class Face {
		final double[][] vertices;
		final double[] uv;
		final IIcon icon;
		final int side, cull, rotation;
		final float shade;
        final boolean tinted;
		Face(double[][] vertices, JsonArray uv, int rotation, IIcon icon, int side, int cull, float shade,boolean tinted) {
			this.vertices = vertices; this.uv = new double[4]; for (int i = 0; i < 4; i++) this.uv[i] = uv.get(i).getAsDouble();
			this.rotation = rotation; this.icon = icon; this.side = side; this.cull = cull; this.shade = shade;this.tinted=tinted;
		}
	}
}
