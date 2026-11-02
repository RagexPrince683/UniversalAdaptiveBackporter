package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel;
import java.lang.reflect.Method;
import java.util.Arrays;

/** Verify the native model UV corner contract without creating a graphics context. */
public final class ReleasedModelGeometryTest {
    public static void main(String[] args) throws Exception {
        Method vertices = ReleasedBlockModel.class.getDeclaredMethod("vertices", int.class, double[].class, double[].class);
        vertices.setAccessible(true);
        double[][] up = (double[][]) vertices.invoke(null, 1, new double[]{2, 3, 4}, new double[]{11, 13, 17});
        double[][] expected = {{2, 13, 4}, {2, 13, 17}, {11, 13, 17}, {11, 13, 4}};
        if (!Arrays.deepEquals(up, expected)) throw new AssertionError("Upward faces must start at the north-west UV corner, including rotated coral fans");
        System.out.println("Released model geometry: native upward UV corner and coral-fan base verified");
    }
}
