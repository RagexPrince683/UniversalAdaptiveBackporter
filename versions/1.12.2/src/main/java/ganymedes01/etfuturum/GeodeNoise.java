package ganymedes01.etfuturum;

import java.util.Random;

/** The maintained geode's double Perlin sampler, specialized to its single octave at -4. */
final class GeodeNoise {
    private final Perlin first, second;
    GeodeNoise(Random random) { first = octave(random); second = octave(random); }
    private static Perlin octave(Random random) {
        new Perlin(random); // The zero-octave sampler is consumed before the three absent octaves.
        for (int i = 0; i < 3 * 262; i++) random.nextInt();
        return new Perlin(random);
    }
    double sample(double x, double y, double z) {
        double scale = 1.0181268882175227D;
        return (first.sample(precision(x / 16), precision(y / 16), precision(z / 16))
            + second.sample(precision(x * scale / 16), precision(y * scale / 16), precision(z * scale / 16)))
            * (0.16666666666666666D / 0.2D);
    }
    private static double precision(double value) { return value - Math.floor(value / 3.3554432E7D + .5) * 3.3554432E7D; }
    private static double fade(double value) { return value * value * value * (value * (value * 6 - 15) + 10); }
    private static double lerp(double delta, double a, double b) { return a + delta * (b - a); }
    private static final class Perlin {
        private static final int[][] GRADIENTS = {{1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},{1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},{0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1},{1,1,0},{0,-1,1},{-1,1,0},{0,-1,-1}};
        final int[] permutations = new int[256];
        final double x, y, z;
        Perlin(Random random) {
            x = random.nextDouble() * 256; y = random.nextDouble() * 256; z = random.nextDouble() * 256;
            for (int i = 0; i < 256; i++) permutations[i] = i;
            for (int i = 0; i < 256; i++) { int j = i + random.nextInt(256-i), value = permutations[i]; permutations[i] = permutations[j]; permutations[j] = value; }
        }
        int hash(int value) { return permutations[value & 255]; }
        double grad(int hash, double x, double y, double z) { int[] v = GRADIENTS[hash & 15]; return v[0] * x + v[1] * y + v[2] * z; }
        double sample(double px, double py, double pz) {
            px += x; py += y; pz += z;
            int ix = (int) Math.floor(px), iy = (int) Math.floor(py), iz = (int) Math.floor(pz);
            px -= ix; py -= iy; pz -= iz;
            int a = hash(ix), b = hash(ix+1), aa = hash(a+iy), ab = hash(a+iy+1), ba = hash(b+iy), bb = hash(b+iy+1);
            double fx = fade(px), fy = fade(py), fz = fade(pz);
            return lerp(fz, lerp(fy, lerp(fx, grad(hash(aa+iz), px, py, pz), grad(hash(ba+iz), px-1, py, pz)),
                    lerp(fx, grad(hash(ab+iz), px, py-1, pz), grad(hash(bb+iz), px-1, py-1, pz))),
                lerp(fy, lerp(fx, grad(hash(aa+iz+1), px, py, pz-1), grad(hash(ba+iz+1), px-1, py, pz-1)),
                    lerp(fx, grad(hash(ab+iz+1), px, py-1, pz-1), grad(hash(bb+iz+1), px-1, py-1, pz-1))));
        }
    }
}
