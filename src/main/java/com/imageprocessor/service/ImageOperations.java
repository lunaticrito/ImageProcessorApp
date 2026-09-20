package com.imageprocessor.service;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;

/**
 * Utility class for image manipulation.
 */
public final class ImageOperations {

    private ImageOperations() {
        // Private constructor for utility class
    }

    private static BufferedImage ensureARGB(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
            return src;
        }
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dest.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return dest;
    }

    private static int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    private static float clamp01(float val) {
        return Math.max(0.0f, Math.min(1.0f, val));
    }

    public static BufferedImage rotate(BufferedImage src, double angleDegrees) {
        double angleRad = Math.toRadians(angleDegrees);
        double sin = Math.abs(Math.sin(angleRad));
        double cos = Math.abs(Math.cos(angleRad));
        int w = src.getWidth();
        int h = src.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage dest = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dest.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.translate((newW - w) / 2.0, (newH - h) / 2.0);
        g2d.rotate(angleRad, w / 2.0, h / 2.0);
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return dest;
    }

    public static BufferedImage rotate90CW(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(h, w, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                dest.setRGB(h - 1 - y, x, src.getRGB(x, y));
            }
        }
        return dest;
    }

    public static BufferedImage rotate90CCW(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(h, w, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                dest.setRGB(y, w - 1 - x, src.getRGB(x, y));
            }
        }
        return dest;
    }

    public static BufferedImage flipHorizontal(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g2d = dest.createGraphics();
        g2d.drawImage(src, w, 0, 0, h, 0, 0, w, h, null);
        g2d.dispose();
        return dest;
    }

    public static BufferedImage flipVertical(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g2d = dest.createGraphics();
        g2d.drawImage(src, 0, h, w, 0, 0, 0, w, h, null);
        g2d.dispose();
        return dest;
    }

    public static BufferedImage crop(BufferedImage src, int x, int y, int width, int height) {
        int safeX = Math.max(0, Math.min(x, src.getWidth() - 1));
        int safeY = Math.max(0, Math.min(y, src.getHeight() - 1));
        int safeW = Math.max(1, Math.min(width, src.getWidth() - safeX));
        int safeH = Math.max(1, Math.min(height, src.getHeight() - safeY));

        BufferedImage dest = new BufferedImage(safeW, safeH, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g2d = dest.createGraphics();
        g2d.drawImage(src.getSubimage(safeX, safeY, safeW, safeH), 0, 0, null);
        g2d.dispose();
        return dest;
    }

    public static BufferedImage resize(BufferedImage src, int newWidth, int newHeight) {
        BufferedImage dest = new BufferedImage(newWidth, newHeight, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g2d = dest.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(src, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return dest;
    }

    public static BufferedImage adjustBrightness(BufferedImage src, double value) {
        src = ensureARGB(src);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int offset = (int) ((value / 100.0) * 255.0);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = src.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = clamp(((rgba >> 16) & 0xFF) + offset);
                int g = clamp(((rgba >> 8) & 0xFF) + offset);
                int b = clamp((rgba & 0xFF) + offset);
                dest.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return dest;
    }

    public static BufferedImage adjustContrast(BufferedImage src, double value) {
        src = ensureARGB(src);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        double factor = (259.0 * (value + 255.0)) / (255.0 * (259.0 - value));

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = src.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = clamp((int) (factor * (((rgba >> 16) & 0xFF) - 128) + 128));
                int g = clamp((int) (factor * (((rgba >> 8) & 0xFF) - 128) + 128));
                int b = clamp((int) (factor * ((rgba & 0xFF) - 128) + 128));
                dest.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return dest;
    }

    public static BufferedImage adjustSaturation(BufferedImage src, double value) {
        src = ensureARGB(src);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        float scale = (float) (1.0 + (value / 100.0));
        float[] hsb = new float[3];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = src.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;
                Color.RGBtoHSB(r, g, b, hsb);
                hsb[1] = clamp01(hsb[1] * scale);
                int newRgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                dest.setRGB(x, y, (a << 24) | (newRgb & 0xFFFFFF));
            }
        }
        return dest;
    }

    public static BufferedImage adjustHue(BufferedImage src, double value) {
        src = ensureARGB(src);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        float hueShift = (float) (value / 360.0);
        float[] hsb = new float[3];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = src.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;
                Color.RGBtoHSB(r, g, b, hsb);
                hsb[0] = (hsb[0] + hueShift) % 1.0f;
                if (hsb[0] < 0) hsb[0] += 1.0f;
                int newRgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                dest.setRGB(x, y, (a << 24) | (newRgb & 0xFFFFFF));
            }
        }
        return dest;
    }

    public static BufferedImage applyGrayscale(BufferedImage src) {
        src = ensureARGB(src);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = src.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                gray = clamp(gray);
                dest.setRGB(x, y, (a << 24) | (gray << 16) | (gray << 8) | gray);
            }
        }
        return dest;
    }

    public static BufferedImage applySepia(BufferedImage src) {
        src = ensureARGB(src);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = src.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;

                int tr = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                int tg = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                int tb = (int)(0.272 * r + 0.534 * g + 0.131 * b);

                dest.setRGB(x, y, (a << 24) | (clamp(tr) << 16) | (clamp(tg) << 8) | clamp(tb));
            }
        }
        return dest;
    }

    public static BufferedImage applyInvert(BufferedImage src) {
        src = ensureARGB(src);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = src.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = 255 - ((rgba >> 16) & 0xFF);
                int g = 255 - ((rgba >> 8) & 0xFF);
                int b = 255 - (rgba & 0xFF);
                dest.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return dest;
    }

    private static BufferedImage applyConvolution(BufferedImage src, float[][] kernel) {
        int kHeight = kernel.length;
        int kWidth = kernel[0].length;
        int kYCenter = kHeight / 2;
        int kXCenter = kWidth / 2;
        
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float rSum = 0, gSum = 0, bSum = 0;
                int a = (src.getRGB(x, y) >> 24) & 0xFF;
                
                for (int ky = 0; ky < kHeight; ky++) {
                    for (int kx = 0; kx < kWidth; kx++) {
                        int pixelY = Math.max(0, Math.min(h - 1, y + ky - kYCenter));
                        int pixelX = Math.max(0, Math.min(w - 1, x + kx - kXCenter));
                        int rgb = src.getRGB(pixelX, pixelY);
                        float val = kernel[ky][kx];
                        rSum += ((rgb >> 16) & 0xFF) * val;
                        gSum += ((rgb >> 8) & 0xFF) * val;
                        bSum += (rgb & 0xFF) * val;
                    }
                }
                dest.setRGB(x, y, (a << 24) | (clamp((int)rSum) << 16) | (clamp((int)gSum) << 8) | clamp((int)bSum));
            }
        }
        return dest;
    }

    public static BufferedImage applyGaussianBlur(BufferedImage src, double radius) {
        if (radius <= 0) return src;
        int r = (int) Math.ceil(radius);
        int size = r * 2 + 1;
        float[] kernel = new float[size];
        float sum = 0;
        double sigma = radius / 3.0;
        if (sigma < 0.1) sigma = 0.1;
        double twoSigmaSq = 2.0 * sigma * sigma;
        double sqrtTwoPiSigma = Math.sqrt(2.0 * Math.PI) * sigma;

        for (int i = -r; i <= r; i++) {
            float val = (float)(Math.exp(-(i * i) / twoSigmaSq) / sqrtTwoPiSigma);
            kernel[i + r] = val;
            sum += val;
        }
        for (int i = 0; i < size; i++) {
            kernel[i] /= sum;
        }

        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage pass1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float rSum = 0, gSum = 0, bSum = 0;
                int a = (src.getRGB(x, y) >> 24) & 0xFF;
                for (int i = -r; i <= r; i++) {
                    int pixelX = Math.max(0, Math.min(w - 1, x + i));
                    int rgb = src.getRGB(pixelX, y);
                    float val = kernel[i + r];
                    rSum += ((rgb >> 16) & 0xFF) * val;
                    gSum += ((rgb >> 8) & 0xFF) * val;
                    bSum += (rgb & 0xFF) * val;
                }
                pass1.setRGB(x, y, (a << 24) | (clamp((int)rSum) << 16) | (clamp((int)gSum) << 8) | clamp((int)bSum));
            }
        }

        BufferedImage pass2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float rSum = 0, gSum = 0, bSum = 0;
                int a = (pass1.getRGB(x, y) >> 24) & 0xFF;
                for (int i = -r; i <= r; i++) {
                    int pixelY = Math.max(0, Math.min(h - 1, y + i));
                    int rgb = pass1.getRGB(x, pixelY);
                    float val = kernel[i + r];
                    rSum += ((rgb >> 16) & 0xFF) * val;
                    gSum += ((rgb >> 8) & 0xFF) * val;
                    bSum += (rgb & 0xFF) * val;
                }
                pass2.setRGB(x, y, (a << 24) | (clamp((int)rSum) << 16) | (clamp((int)gSum) << 8) | clamp((int)bSum));
            }
        }
        return pass2;
    }

    public static BufferedImage applySharpen(BufferedImage src, double strength) {
        if (strength <= 0) return src;
        BufferedImage blurred = applyGaussianBlur(src, 1.0);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int orig = src.getRGB(x, y);
                int blur = blurred.getRGB(x, y);
                int a = (orig >> 24) & 0xFF;
                
                int oR = (orig >> 16) & 0xFF;
                int oG = (orig >> 8) & 0xFF;
                int oB = orig & 0xFF;
                
                int bR = (blur >> 16) & 0xFF;
                int bG = (blur >> 8) & 0xFF;
                int bB = blur & 0xFF;
                
                int r = clamp((int) (oR + strength * (oR - bR)));
                int g = clamp((int) (oG + strength * (oG - bG)));
                int b = clamp((int) (oB + strength * (oB - bB)));
                
                dest.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return dest;
    }

    public static BufferedImage applyEdgeDetection(BufferedImage src) {
        src = applyGrayscale(src);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        int[][] gx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] gy = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int sumX = 0;
                int sumY = 0;
                int a = (src.getRGB(x, y) >> 24) & 0xFF;
                
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixelY = Math.max(0, Math.min(h - 1, y + ky));
                        int pixelX = Math.max(0, Math.min(w - 1, x + kx));
                        int val = src.getRGB(pixelX, pixelY) & 0xFF; 
                        sumX += val * gx[ky + 1][kx + 1];
                        sumY += val * gy[ky + 1][kx + 1];
                    }
                }
                int magnitude = clamp((int) Math.sqrt(sumX * sumX + sumY * sumY));
                dest.setRGB(x, y, (a << 24) | (magnitude << 16) | (magnitude << 8) | magnitude);
            }
        }
        return dest;
    }
}
