package com.customworldgen.noise;

import com.customworldgen.config.NoiseType;

/**
 * Utility class providing static methods for various noise generation algorithms.
 */
public final class NoiseGenerator {

    private NoiseGenerator() {
    }

    // Permutation table for Perlin/Simplex noise
    private static final int[] PERM = new int[512];

    static {
        int[] base = {
            151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225,
            140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148,
            247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32,
            57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175,
            74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122,
            60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54,
            65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169,
            200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64,
            52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212,
            207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213,
            119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
            129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104,
            218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241,
            81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157,
            184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93,
            222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
        };
        for (int i = 0; i < 256; i++) {
            PERM[i] = base[i];
            PERM[i + 256] = base[i];
        }
    }

    // Simplex noise skew constants for 3D
    private static final double F3 = 1.0 / 3.0;
    private static final double G3 = 1.0 / 6.0;

    private static final int[][] GRAD3 = {
        {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
        {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
        {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}
    };

    /**
     * Dispatches to the appropriate noise function based on the given type.
     */
    public static double generateNoise(NoiseType type, double x, double y, double z, long seed) {
        return switch (type) {
            case PERLIN -> perlinNoise(x, y, z, seed);
            case SIMPLEX -> simplexNoise(x, y, z, seed);
            case RIDGED -> ridgedNoise(x, y, z, seed, 6);
            case VORONOI -> voronoiNoise(x, y, z, seed);
        };
    }

    /**
     * Generates layered octave noise by combining multiple frequencies.
     */
    public static double octaveNoise(NoiseType type, double x, double y, double z,
                                     long seed, int octaves, double persistence) {
        double total = 0.0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxValue = 0.0;

        for (int i = 0; i < octaves; i++) {
            total += generateNoise(type,
                    x * frequency, y * frequency, z * frequency,
                    seed + i) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2.0;
        }

        return total / maxValue;
    }

    // ---- Perlin Noise ----

    /**
     * Standard 3D Perlin noise implementation returning values in approximately [-1, 1].
     */
    public static double perlinNoise(double x, double y, double z, long seed) {
        // Apply seed offset
        x += (seed & 0xFFFF);
        y += ((seed >> 16) & 0xFFFF);
        z += ((seed >> 32) & 0xFFFF);

        int xi = fastFloor(x) & 255;
        int yi = fastFloor(y) & 255;
        int zi = fastFloor(z) & 255;

        double xf = x - fastFloor(x);
        double yf = y - fastFloor(y);
        double zf = z - fastFloor(z);

        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);

        int aaa = PERM[PERM[PERM[xi] + yi] + zi];
        int aba = PERM[PERM[PERM[xi] + yi + 1] + zi];
        int aab = PERM[PERM[PERM[xi] + yi] + zi + 1];
        int abb = PERM[PERM[PERM[xi] + yi + 1] + zi + 1];
        int baa = PERM[PERM[PERM[xi + 1] + yi] + zi];
        int bba = PERM[PERM[PERM[xi + 1] + yi + 1] + zi];
        int bab = PERM[PERM[PERM[xi + 1] + yi] + zi + 1];
        int bbb = PERM[PERM[PERM[xi + 1] + yi + 1] + zi + 1];

        double x1 = lerp(grad(aaa, xf, yf, zf), grad(baa, xf - 1, yf, zf), u);
        double x2 = lerp(grad(aba, xf, yf - 1, zf), grad(bba, xf - 1, yf - 1, zf), u);
        double y1 = lerp(x1, x2, v);

        x1 = lerp(grad(aab, xf, yf, zf - 1), grad(bab, xf - 1, yf, zf - 1), u);
        x2 = lerp(grad(abb, xf, yf - 1, zf - 1), grad(bbb, xf - 1, yf - 1, zf - 1), u);
        double y2 = lerp(x1, x2, v);

        return lerp(y1, y2, w);
    }

    // ---- Simplex Noise ----

    /**
     * 3D Simplex noise implementation returning values in approximately [-1, 1].
     */
    public static double simplexNoise(double x, double y, double z, long seed) {
        x += (seed & 0xFFFF);
        y += ((seed >> 16) & 0xFFFF);
        z += ((seed >> 32) & 0xFFFF);

        double s = (x + y + z) * F3;
        int i = fastFloor(x + s);
        int j = fastFloor(y + s);
        int k = fastFloor(z + s);

        double t = (i + j + k) * G3;
        double x0 = x - (i - t);
        double y0 = y - (j - t);
        double z0 = z - (k - t);

        int i1, j1, k1, i2, j2, k2;
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 0; k2 = 1;
            } else {
                i1 = 0; j1 = 0; k1 = 1; i2 = 1; j2 = 0; k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0; j1 = 0; k1 = 1; i2 = 0; j2 = 1; k2 = 1;
            } else if (x0 < z0) {
                i1 = 0; j1 = 1; k1 = 0; i2 = 0; j2 = 1; k2 = 1;
            } else {
                i1 = 0; j1 = 1; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
            }
        }

        double x1 = x0 - i1 + G3;
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + 2.0 * G3;
        double y2 = y0 - j2 + 2.0 * G3;
        double z2 = z0 - k2 + 2.0 * G3;
        double x3 = x0 - 1.0 + 3.0 * G3;
        double y3 = y0 - 1.0 + 3.0 * G3;
        double z3 = z0 - 1.0 + 3.0 * G3;

        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;

        double n0 = simplexContribution(PERM[ii + PERM[jj + PERM[kk]]], x0, y0, z0);
        double n1 = simplexContribution(PERM[ii + i1 + PERM[jj + j1 + PERM[kk + k1]]], x1, y1, z1);
        double n2 = simplexContribution(PERM[ii + i2 + PERM[jj + j2 + PERM[kk + k2]]], x2, y2, z2);
        double n3 = simplexContribution(PERM[ii + 1 + PERM[jj + 1 + PERM[kk + 1]]], x3, y3, z3);

        return 32.0 * (n0 + n1 + n2 + n3);
    }

    private static double simplexContribution(int hash, double x, double y, double z) {
        double t = 0.6 - x * x - y * y - z * z;
        if (t < 0) return 0.0;
        t *= t;
        int[] g = GRAD3[hash % 12];
        return t * t * (g[0] * x + g[1] * y + g[2] * z);
    }

    // ---- Ridged Multifractal Noise ----

    /**
     * Ridged multifractal noise built on Perlin noise, returning values in [0, 1].
     */
    public static double ridgedNoise(double x, double y, double z, long seed, int octaves) {
        double total = 0.0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double weight = 1.0;
        double maxValue = 0.0;

        for (int i = 0; i < octaves; i++) {
            double signal = perlinNoise(x * frequency, y * frequency, z * frequency, seed + i);
            signal = 1.0 - Math.abs(signal);
            signal *= signal;
            signal *= weight;
            weight = Math.min(1.0, Math.max(0.0, signal * 2.0));

            total += signal * amplitude;
            maxValue += amplitude;
            frequency *= 2.0;
            amplitude *= 0.5;
        }

        return total / maxValue;
    }

    // ---- Voronoi / Worley Noise ----

    /**
     * Voronoi (Worley/cell) noise returning values in approximately [0, 1].
     * Returns the distance to the nearest random feature point.
     */
    public static double voronoiNoise(double x, double y, double z, long seed) {
        int xi = fastFloor(x);
        int yi = fastFloor(y);
        int zi = fastFloor(z);

        double minDist = Double.MAX_VALUE;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int cx = xi + dx;
                    int cy = yi + dy;
                    int cz = zi + dz;

                    // Deterministic random offset per cell based on seed
                    long cellHash = voronoiHash(cx, cy, cz, seed);
                    double px = cx + ((cellHash & 0xFFFF) / 65535.0);
                    double py = cy + (((cellHash >> 16) & 0xFFFF) / 65535.0);
                    double pz = cz + (((cellHash >> 32) & 0xFFFF) / 65535.0);

                    double distSq = (x - px) * (x - px) + (y - py) * (y - py) + (z - pz) * (z - pz);
                    if (distSq < minDist) {
                        minDist = distSq;
                    }
                }
            }
        }

        return Math.min(1.0, Math.sqrt(minDist));
    }

    private static long voronoiHash(int x, int y, int z, long seed) {
        long h = seed;
        h ^= x * 0x6C62272E07BB0142L;
        h ^= y * 0x94D049BB133111EBL;
        h ^= z * 0xC86B14F7109F5A7BL;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    // ---- Utility functions ----

    private static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
