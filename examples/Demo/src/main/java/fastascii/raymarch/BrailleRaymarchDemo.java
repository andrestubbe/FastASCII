package fastascii.raymarch;

import fastascii.FastBrailleDither;
import java.nio.charset.StandardCharsets;

public class BrailleRaymarchDemo {

    static class Vec3 {
        final double x, y, z;
        Vec3(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
        Vec3 add(Vec3 v) { return new Vec3(x + v.x, y + v.y, z + v.z); }
        Vec3 sub(Vec3 v) { return new Vec3(x - v.x, y - v.y, z - v.z); }
        Vec3 mul(double s) { return new Vec3(x * s, y * s, z * s); }
        double dot(Vec3 v) { return x * v.x + y * v.y + z * v.z; }
        double length() { return Math.sqrt(x * x + y * y + z * z); }
        Vec3 normalize() {
            double l = length();
            return l == 0 ? new Vec3(0, 0, 0) : new Vec3(x / l, y / l, z / l);
        }
    }

    static double map(Vec3 p) {
        double dSphere = p.sub(new Vec3(0, 0, 5)).length() - 2.625;
        double dWall = 10.0 - p.z;
        return Math.min(dSphere, dWall);
    }

    static Vec3 calcNormal(Vec3 p) {
        double e = 0.001;
        return new Vec3(
            map(new Vec3(p.x + e, p.y, p.z)) - map(new Vec3(p.x - e, p.y, p.z)),
            map(new Vec3(p.x, p.y + e, p.z)) - map(new Vec3(p.x, p.y - e, p.z)),
            map(new Vec3(p.x, p.y, p.z + e)) - map(new Vec3(p.x, p.y, p.z - e))
        ).normalize();
    }

    static double rayMarch(Vec3 ro, Vec3 rd, double maxDist) {
        double t = 0.0;
        for (int i = 0; i < 80; i++) {
            Vec3 p = ro.add(rd.mul(t));
            double d = map(p);
            if (d < 0.001) return t;
            t += d;
            if (t > maxDist) break;
        }
        return -1;
    }

    static float shade(Vec3 ro, Vec3 rd, Vec3 lightPos) {
        double dist = rayMarch(ro, rd, 50.0);
        if (dist <= 0) return 0.0f;

        Vec3 hitPos = ro.add(rd.mul(dist));
        Vec3 normal = calcNormal(hitPos);

        double distToSphere = hitPos.sub(new Vec3(0, 0, 5)).length() - 2.625;
        boolean hitWall = distToSphere > 0.1;

        Vec3 lightDir = lightPos.sub(hitPos).normalize();
        double diffuse = Math.max(0.0, normal.dot(lightDir));

        double lightDist = lightPos.sub(hitPos).length();
        double attenuation = 1.0 / (1.0 + 0.02 * lightDist * lightDist);
        diffuse *= attenuation * 2.0;

        double specular = 0.0;
        if (!hitWall) {
            Vec3 viewDir = ro.sub(hitPos).normalize();
            Vec3 reflectDir = lightDir.mul(-1).add(normal.mul(2.0 * normal.dot(lightDir))).normalize();
            double specAngle = Math.max(0.0, viewDir.dot(reflectDir));
            specular = Math.pow(specAngle, 16.0) * 0.8;
        }

        double shadowDist = rayMarch(hitPos.add(normal.mul(0.02)), lightDir, lightDist);
        if (shadowDist > 0 && shadowDist < lightDist) {
            diffuse *= 0.05;
            specular = 0.0;
        }

        double lighting = diffuse + specular;
        if (lighting > 1.0) lighting = 1.0;
        if (lighting < 0.0) lighting = 0.0;
        return (float) lighting;
    }

    public static void main(String[] args) throws Exception {
        int charCols = 120;
        int charRows = 30;
        FastBrailleDither frame = new FastBrailleDither(charCols, charRows);
        int pixelW = frame.getPixelWidth();
        int pixelH = frame.getPixelHeight();

        System.out.println("FastASCII Braille Raymarcher (2x4 B&W, " + pixelW + "x" + pixelH + " px)");
        System.out.println("Initializing Scene...");

        System.out.write("\033[?25l".getBytes(StandardCharsets.UTF_8));

        long startTime = System.currentTimeMillis();
        Vec3 ro = new Vec3(0, 0, 0);

        try {
            while (true) {
                double time = (System.currentTimeMillis() - startTime) / 1000.0;
                Vec3 lightPos = new Vec3(
                    Math.sin(time) * 5.0,
                    4.0 + Math.cos(time * 0.7) * 2.0,
                    0.0
                );

                frame.clear();

                for (int py = 0; py < pixelH; py++) {
                    double ny = (double) py / pixelH * 2.0 - 1.0;
                    ny = -ny;

                    for (int px = 0; px < pixelW; px++) {
                        double nx = (double) px / pixelW * 2.0 - 1.0;
                        // Match Demo.java: aspect from char grid, not pixel grid
                        nx *= (double) charCols / charRows * 0.5;

                        Vec3 rd = new Vec3(nx, ny, 1.0).normalize();
                        float lum = shade(ro, rd, lightPos);
                        frame.setPixel(px, py, lum);
                    }
                }

                StringBuilder sb = new StringBuilder();
                sb.append("\033[?2026h");
                sb.append("\033[H");
                frame.appendTo(sb);
                sb.append("\033[?2026l");

                System.out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                System.out.flush();
                Thread.sleep(16);
            }
        } finally {
            System.out.write("\033[?2026l".getBytes(StandardCharsets.UTF_8));
            System.out.write("\033[?25h".getBytes(StandardCharsets.UTF_8));
        }
    }
}
