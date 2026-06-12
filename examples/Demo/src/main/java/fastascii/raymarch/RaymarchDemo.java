package fastascii.raymarch;

import fastansi.FastANSI;
import fastascii.FastGlyphDensity;
import java.nio.charset.StandardCharsets;

public class RaymarchDemo {
    
    // Lightweight Hyper-Optimized Vector Math
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

    // Geometry Map (Signed Distance Field)
    static double map(Vec3 p) {
        // Sphere floating at (0, 0, 5) with Radius 2.625 (1.5 * 1.75)
        double dSphere = p.sub(new Vec3(0, 0, 5)).length() - 2.625;
        // Infinite flat Wall behind the sphere at Z = 10
        double dWall = 10.0 - p.z;
        return Math.min(dSphere, dWall);
    }

    // Surface Normal Calculation
    static Vec3 calcNormal(Vec3 p) {
        double e = 0.001;
        return new Vec3(
            map(new Vec3(p.x + e, p.y, p.z)) - map(new Vec3(p.x - e, p.y, p.z)),
            map(new Vec3(p.x, p.y + e, p.z)) - map(new Vec3(p.x, p.y - e, p.z)),
            map(new Vec3(p.x, p.y, p.z + e)) - map(new Vec3(p.x, p.y, p.z - e))
        ).normalize();
    }

    // Shoot Ray
    static double rayMarch(Vec3 ro, Vec3 rd, double maxDist) {
        double t = 0.0;
        for (int i = 0; i < 80; i++) {
            Vec3 p = ro.add(rd.mul(t));
            double d = map(p);
            if (d < 0.001) return t; // Hit
            t += d;
            if (t > maxDist) break; // Miss
        }
        return -1;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("FastANSI 3D Raymarcher Demo");
        System.out.println("Initializing Scene & GPU (CPU)...");

        int width = 120;
        int height = 30;

        System.out.write("\033[?25l".getBytes(StandardCharsets.UTF_8)); // Hide Cursor
        
        long startTime = System.currentTimeMillis();
        
        try {
            while (true) {
                double time = (System.currentTimeMillis() - startTime) / 1000.0;
                
                // Orbiting Spotlight (Moves left-right and up-down smoothly)
                Vec3 lightPos = new Vec3(
                    Math.sin(time) * 5.0, 
                    4.0 + Math.cos(time * 0.7) * 2.0, 
                    0.0
                );

                StringBuilder sb = new StringBuilder();
                sb.append("\033[?2026h"); // Start synchronized update
                sb.append("\033[H"); // Reset cursor to 0,0

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        
                        // Normalize Coordinates from -1.0 to 1.0
                        double nx = (double) x / width * 2.0 - 1.0;
                        double ny = (double) y / height * 2.0 - 1.0;
                        
                        // Fix Terminal Aspect Ratio (Font is approx 2:1 height:width)
                        nx *= (double) width / height * 0.5;
                        ny = -ny; // Flip Y axis
                        
                        Vec3 ro = new Vec3(0, 0, 0); // Camera at origin
                        Vec3 rd = new Vec3(nx, ny, 1.0).normalize(); // Ray direction
                        
                        double dist = rayMarch(ro, rd, 20.0);
                        
                        if (dist > 0) {
                            Vec3 hitPos = ro.add(rd.mul(dist));
                            Vec3 normal = calcNormal(hitPos);
                            
                            // Determine which object we hit (Sphere vs Wall)
                            double distToSphere = hitPos.sub(new Vec3(0, 0, 5)).length() - 2.625;
                            boolean hitWall = distToSphere > 0.1;
                            
                            // Diffuse Lighting (Dot product)
                            Vec3 lightDir = lightPos.sub(hitPos).normalize();
                            double diffuse = Math.max(0.0, normal.dot(lightDir));
                            
                            // Distance Attenuation (Light falls off as it gets further)
                            double lightDist = lightPos.sub(hitPos).length();
                            double attenuation = 1.0 / (1.0 + 0.02 * lightDist * lightDist);
                            diffuse *= attenuation * 2.0; // Boost light intensity

                            // Specular Highlight for the Sphere
                            double specular = 0.0;
                            if (!hitWall) {
                                Vec3 viewDir = ro.sub(hitPos).normalize();
                                Vec3 reflectDir = lightDir.mul(-1).add(normal.mul(2.0 * normal.dot(lightDir))).normalize();
                                double specAngle = Math.max(0.0, viewDir.dot(reflectDir));
                                specular = Math.pow(specAngle, 16.0) * 0.8; // Shininess
                            }
                            
                            // Cast Shadow Ray
                            double shadowDist = rayMarch(hitPos.add(normal.mul(0.02)), lightDir, lightDist);
                            if (shadowDist > 0 && shadowDist < lightDist) {
                                diffuse *= 0.05; // Hard Shadow (Pitch Black)
                                specular = 0.0;
                            }
                            
                            // Combine lighting
                            double finalLighting = diffuse + specular;
                            if (finalLighting > 1.0) finalLighting = 1.0;
                            if (finalLighting < 0.0) finalLighting = 0.0;
                            
                            // Get mathematically accurate glyph
                            char glyph;
                            if (finalLighting <= 0.02) glyph = ' '; // Force empty space for pure shadow
                            else glyph = FastGlyphDensity.getGlyphForOpacity((float) finalLighting);
                            
                            // Assign constant base colors
                            int r, g, b;
                            if (hitWall) {
                                r = 128; g = 128; b = 128; // Half Gray Wall
                            } else {
                                r = 255; g = 255; b = 255; // White Sphere
                            }
                            
                            sb.append(FastANSI.fg(r, g, b)).append(glyph);
                        } else {
                            sb.append(FastANSI.RESET).append(' '); // Empty space
                        }
                    }
                    if (y < height - 1) sb.append("\n");
                }
                sb.append("\033[?2026l"); // Commit frame
                
                byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
                System.out.write(bytes);
                System.out.flush();
                
                // Sleep to cap at 60 FPS
                Thread.sleep(16);
            }
        } finally {
            // Restore VT and cursor if interrupted
            System.out.write("\033[?2026l".getBytes(StandardCharsets.UTF_8));
            System.out.write("\033[?25h".getBytes(StandardCharsets.UTF_8)); // Show Cursor
            System.out.println(FastANSI.RESET);
        }
    }
}
