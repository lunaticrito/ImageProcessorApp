package com.imageprocessor;

import com.imageprocessor.service.ImageOperations;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class ImageOperationsTest {

    private BufferedImage createTestImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = (int) ((x / (float) width) * 255);
                int g = (int) ((y / (float) height) * 255);
                int b = 128;
                int a = 255;
                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(x, y, argb);
            }
        }
        return img;
    }

    @Test
    public void testRotate90CW() {
        BufferedImage img = createTestImage(100, 50);
        BufferedImage rotated = ImageOperations.rotate90CW(img);
        assertNotNull(rotated);
        assertEquals(50, rotated.getWidth());
        assertEquals(100, rotated.getHeight());
    }

    @Test
    public void testRotate90CCW() {
        BufferedImage img = createTestImage(100, 50);
        BufferedImage rotated = ImageOperations.rotate90CCW(img);
        assertNotNull(rotated);
        assertEquals(50, rotated.getWidth());
        assertEquals(100, rotated.getHeight());
    }

    @Test
    public void testFlipHorizontal() {
        BufferedImage img = createTestImage(100, 50);
        int origColor = img.getRGB(0, 0);
        BufferedImage flipped = ImageOperations.flipHorizontal(img);
        assertNotNull(flipped);
        assertEquals(100, flipped.getWidth());
        assertEquals(50, flipped.getHeight());
        assertEquals(origColor, flipped.getRGB(99, 0));
    }

    @Test
    public void testFlipVertical() {
        BufferedImage img = createTestImage(100, 50);
        int origColor = img.getRGB(0, 0);
        BufferedImage flipped = ImageOperations.flipVertical(img);
        assertNotNull(flipped);
        assertEquals(100, flipped.getWidth());
        assertEquals(50, flipped.getHeight());
        assertEquals(origColor, flipped.getRGB(0, 49));
    }

    @Test
    public void testCrop() {
        BufferedImage img = createTestImage(100, 100);
        BufferedImage cropped = ImageOperations.crop(img, 10, 10, 50, 50);
        assertNotNull(cropped);
        assertEquals(50, cropped.getWidth());
        assertEquals(50, cropped.getHeight());
    }

    @Test
    public void testResize() {
        BufferedImage img = createTestImage(100, 100);
        BufferedImage resized = ImageOperations.resize(img, 200, 150);
        assertNotNull(resized);
        assertEquals(200, resized.getWidth());
        assertEquals(150, resized.getHeight());
    }

    @Test
    public void testAdjustBrightness() {
        BufferedImage img = createTestImage(10, 10);
        Color origColor = new Color(img.getRGB(5, 5));
        
        BufferedImage brightened = ImageOperations.adjustBrightness(img, 50.0);
        assertNotNull(brightened);
        
        Color newColor = new Color(brightened.getRGB(5, 5));
        assertTrue(newColor.getRed() > origColor.getRed() || newColor.getRed() == 255);
        assertTrue(newColor.getGreen() > origColor.getGreen() || newColor.getGreen() == 255);
        assertTrue(newColor.getBlue() > origColor.getBlue() || newColor.getBlue() == 255);
    }

    @Test
    public void testApplyGrayscale() {
        BufferedImage img = createTestImage(10, 10);
        BufferedImage grayscale = ImageOperations.applyGrayscale(img);
        assertNotNull(grayscale);
        
        Color color = new Color(grayscale.getRGB(5, 5));
        assertEquals(color.getRed(), color.getGreen());
        assertEquals(color.getGreen(), color.getBlue());
    }

    @Test
    public void testApplyInvert() {
        BufferedImage img = createTestImage(10, 10);
        Color origColor = new Color(img.getRGB(5, 5));
        
        BufferedImage inverted = ImageOperations.applyInvert(img);
        assertNotNull(inverted);
        
        Color newColor = new Color(inverted.getRGB(5, 5));
        assertEquals(255 - origColor.getRed(), newColor.getRed());
        assertEquals(255 - origColor.getGreen(), newColor.getGreen());
        assertEquals(255 - origColor.getBlue(), newColor.getBlue());
    }

    @Test
    public void testGaussianBlur() {
        BufferedImage img = createTestImage(20, 20);
        BufferedImage blurred = ImageOperations.applyGaussianBlur(img, 2.0);
        assertNotNull(blurred);
        assertEquals(20, blurred.getWidth());
        assertEquals(20, blurred.getHeight());
    }

    @Test
    public void testApplyEdgeDetection() {
        BufferedImage img = createTestImage(20, 20);
        BufferedImage edge = ImageOperations.applyEdgeDetection(img);
        assertNotNull(edge);
        assertEquals(20, edge.getWidth());
        assertEquals(20, edge.getHeight());
    }
}
