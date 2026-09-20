package com.imageprocessor.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Utility class for image format conversions and manipulations.
 */
public class ImageUtils {

    /**
     * Converts a BufferedImage to a JavaFX Image.
     */
    public static Image toFXImage(BufferedImage bimg) {
        return SwingFXUtils.toFXImage(bimg, null);
    }

    /**
     * Alias for toFXImage.
     */
    public static Image convertToFXImage(BufferedImage bimg) {
        return toFXImage(bimg);
    }

    /**
     * Converts a JavaFX Image to a BufferedImage, ensuring it is TYPE_INT_ARGB.
     */
    public static BufferedImage fromFXImage(Image fxImage) {
        BufferedImage bimg = SwingFXUtils.fromFXImage(fxImage, null);
        if (bimg != null && bimg.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage argbImg = new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = argbImg.createGraphics();
            g2d.drawImage(bimg, 0, 0, null);
            g2d.dispose();
            return argbImg;
        }
        return bimg;
    }

    /**
     * Loads a BufferedImage from a File.
     */
    public static BufferedImage loadFromFile(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Unsupported image format: " + file.getName());
        }
        return image;
    }

    /**
     * Saves a BufferedImage to a File, converting to TYPE_INT_RGB for JPEG.
     */
    public static void saveToFile(BufferedImage image, File file) throws IOException {
        String formatName = getFormatFromFileName(file.getName());
        if (formatName.equalsIgnoreCase("JPG") || formatName.equalsIgnoreCase("JPEG")) {
            BufferedImage rgbImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = rgbImg.createGraphics();
            g2d.drawImage(image, 0, 0, java.awt.Color.WHITE, null);
            g2d.dispose();
            image = rgbImg;
        }
        ImageIO.write(image, formatName, file);
    }

    /**
     * Extracts and returns the uppercase format extension from a filename.
     */
    public static String getFormatFromFileName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toUpperCase();
        }
        return "PNG";
    }

    /**
     * Creates an independent deep copy of a BufferedImage.
     */
    public static BufferedImage deepCopy(BufferedImage source) {
        if (source == null) return null;
        ColorModel cm = source.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = source.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Gets image dimensions without loading the full image into memory.
     */
    public static Dimension getImageDimensions(File file) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
            if (in != null) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    try {
                        reader.setInput(in);
                        return new Dimension(reader.getWidth(0), reader.getHeight(0));
                    } finally {
                        reader.dispose();
                    }
                }
            }
        }
        throw new IOException("Could not read image dimensions: " + file.getName());
    }
}
