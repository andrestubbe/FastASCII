package fastascii.raymarch;

import fastascii.FastASCIIWriter;
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
        return -1.0; 
    }

    // Fast ANSI color generation without dependency on FastANSI
    static int writeFg(byte[] buf, int offset, int r, int g, int b) {
        // \033[38;2;R;G;Bm
        buf[offset++] = 27;
        buf[offset++] = '[';
        buf[offset++] = '3';
        buf[offset++] = '8';
        buf[offset++] = ';';
        buf[offset++] = '2';
        buf[offset++] = ';';
        offset += FastASCIIWriter.writeInt(buf, offset, r);
        buf[offset++] = ';';
        offset += FastASCIIWriter.writeInt(buf, offset, g);
        buf[offset++] = ';';
        offset += FastASCIIWriter.writeInt(buf, offset, b);
        buf[offset++] = 'm';
        return offset;
    }
    
    static int writeReset(byte[] buf, int offset) {
        // \033[0m
        buf[offset++] = 27;
        buf[offset++] = '[';
        buf[offset++] = '0';
        buf[offset++] = 'm';
        return offset;
    }

    public static void main(String[] args) throws Exception {
        int width = 80;
        int height = 40;
        
        System.out.println("FastASCII Raymarch Demo Starting...");
        System.out.write("\033[?25l".getBytes(StandardCharsets.UTF_8)); 
        System.out.write("\033[2J".getBytes(StandardCharsets.UTF_8)); 
        
        long startTime = System.currentTimeMillis();
        
        // Use a single pre-allocated buffer for ZERO allocations during rendering
        byte[] buffer = new byte[1024 * 128];

        try {
            while (true) {
                double time = (System.currentTimeMillis() - startTime) / 1000.0;
                
                Vec3 lightPos = new Vec3(
                    Math.sin(time) * 5.0,
                    5.0,
                    5.0 + Math.cos(time) * 5.0
                );
                
                int offset = 0;

                // Move cursor to top-left
                buffer[offset++] = 27;
                buffer[offset++] = '[';
                buffer[offset++] = 'H';
                
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        // UV coordinates
                        double uvX = (x / (double)width) * 2.0 - 1.0;
                        double uvY = ((y / (double)height) * 2.0 - 1.0) * (height / (double)width * 2.0);
                        
                        Vec3 ro = new Vec3(0, 0, 0); 
                        Vec3 rd = new Vec3(uvX, uvY, 1.0).normalize(); 
                        
                        double t = rayMarch(ro, rd, 20.0);
                        
                        if (t > 0) {
                            Vec3 hitPos = ro.add(rd.mul(t));
                            Vec3 normal = calcNormal(hitPos);
                            
                            boolean hitWall = (hitPos.z >= 9.9);
                            
                            Vec3 lightDir = lightPos.sub(hitPos).normalize();
                            double lightDist = lightPos.sub(hitPos).length();
                            
                            double diffuse = Math.max(0.0, normal.dot(lightDir));
                            
                            Vec3 viewDir = new Vec3(0, 0, -1).normalize();
                            Vec3 reflectDir = lightDir.mul(-1).sub(normal.mul(2.0 * normal.dot(lightDir.mul(-1)))).normalize();
                            double specular = Math.pow(Math.max(0.0, viewDir.dot(reflectDir)), 32.0);
                            
                            double shadowDist = rayMarch(hitPos.add(normal.mul(0.02)), lightDir, lightDist);
                            if (shadowDist > 0 && shadowDist < lightDist) {
                                diffuse *= 0.05; 
                                specular = 0.0;
                            }
                            
                            double finalLighting = diffuse + specular;
                            if (finalLighting > 1.0) finalLighting = 1.0;
                            if (finalLighting < 0.0) finalLighting = 0.0;
                            
                            char glyph;
                            if (finalLighting <= 0.02) glyph = ' '; 
                            else glyph = FastGlyphDensity.getGlyphForOpacity((float) finalLighting);
                            
                            int r, g, b;
                            if (hitWall) {
                                r = 128; g = 128; b = 128; 
                            } else {
                                r = 255; g = 255; b = 255; 
                            }
                            
                            // Write color
                            offset = writeFg(buffer, offset, r, g, b);
                            // Write glyph
                            buffer[offset++] = (byte) glyph;
                        } else {
                            offset = writeReset(buffer, offset);
                            buffer[offset++] = ' ';
                        }
                    }
                    if (y < height - 1) {
                        buffer[offset++] = '\n';
                    }
                }
                
                // Commit frame
                buffer[offset++] = 27;
                buffer[offset++] = '[';
                buffer[offset++] = '?';
                buffer[offset++] = '2';
                buffer[offset++] = '0';
                buffer[offset++] = '2';
                buffer[offset++] = '6';
                buffer[offset++] = 'l';
                
                System.out.write(buffer, 0, offset);
                System.out.flush();
                
                Thread.sleep(16);
            }
        } finally {
            System.out.write("\033[?2026l\033[?25h\033[0m".getBytes(StandardCharsets.UTF_8));
        }
    }
}
