package com.imageprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model class representing a set of filter adjustments.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterPreset {
    private String name = "Untitled";
    private double brightness = 0.0;
    private double contrast = 0.0;
    private double saturation = 0.0;
    private double hue = 0.0;
    private double blur = 0.0;
    private double sharpen = 0.0;
    private boolean grayscale = false;
    private boolean sepia = false;
    private boolean invert = false;
    private boolean edgeDetect = false;
    private double rotation = 0.0;
    private boolean flipHorizontal = false;
    private boolean flipVertical = false;

    public FilterPreset() {
    }

    public static FilterPreset copyOf(FilterPreset other) {
        if (other == null) return null;
        FilterPreset copy = new FilterPreset();
        copy.setName(other.getName());
        copy.setBrightness(other.getBrightness());
        copy.setContrast(other.getContrast());
        copy.setSaturation(other.getSaturation());
        copy.setHue(other.getHue());
        copy.setBlur(other.getBlur());
        copy.setSharpen(other.getSharpen());
        copy.setGrayscale(other.isGrayscale());
        copy.setSepia(other.isSepia());
        copy.setInvert(other.isInvert());
        copy.setEdgeDetect(other.isEdgeDetect());
        copy.setRotation(other.getRotation());
        copy.setFlipHorizontal(other.isFlipHorizontal());
        copy.setFlipVertical(other.isFlipVertical());
        return copy;
    }

    public void reset() {
        this.name = "Untitled";
        this.brightness = 0.0;
        this.contrast = 0.0;
        this.saturation = 0.0;
        this.hue = 0.0;
        this.blur = 0.0;
        this.sharpen = 0.0;
        this.grayscale = false;
        this.sepia = false;
        this.invert = false;
        this.edgeDetect = false;
        this.rotation = 0.0;
        this.flipHorizontal = false;
        this.flipVertical = false;
    }

    public boolean isDefault() {
        return "Untitled".equals(this.name) &&
                this.brightness == 0.0 &&
                this.contrast == 0.0 &&
                this.saturation == 0.0 &&
                this.hue == 0.0 &&
                this.blur == 0.0 &&
                this.sharpen == 0.0 &&
                !this.grayscale &&
                !this.sepia &&
                !this.invert &&
                !this.edgeDetect &&
                this.rotation == 0.0 &&
                !this.flipHorizontal &&
                !this.flipVertical;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBrightness() { return brightness; }
    public void setBrightness(double brightness) { this.brightness = brightness; }

    public double getContrast() { return contrast; }
    public void setContrast(double contrast) { this.contrast = contrast; }

    public double getSaturation() { return saturation; }
    public void setSaturation(double saturation) { this.saturation = saturation; }

    public double getHue() { return hue; }
    public void setHue(double hue) { this.hue = hue; }

    public double getBlur() { return blur; }
    public void setBlur(double blur) { this.blur = blur; }
    public double getBlurRadius() { return blur; }
    public void setBlurRadius(double blur) { this.blur = blur; }

    public double getSharpen() { return sharpen; }
    public void setSharpen(double sharpen) { this.sharpen = sharpen; }
    public double getSharpenStrength() { return sharpen; }
    public void setSharpenStrength(double sharpen) { this.sharpen = sharpen; }

    public boolean isGrayscale() { return grayscale; }
    public void setGrayscale(boolean grayscale) { this.grayscale = grayscale; }

    public boolean isSepia() { return sepia; }
    public void setSepia(boolean sepia) { this.sepia = sepia; }

    public boolean isInvert() { return invert; }
    public void setInvert(boolean invert) { this.invert = invert; }

    public boolean isEdgeDetect() { return edgeDetect; }
    public void setEdgeDetect(boolean edgeDetect) { this.edgeDetect = edgeDetect; }
    public boolean isEdgeDetection() { return edgeDetect; }
    public void setEdgeDetection(boolean edgeDetect) { this.edgeDetect = edgeDetect; }

    public double getRotation() { return rotation; }
    public void setRotation(double rotation) { this.rotation = rotation; }

    public boolean isFlipHorizontal() { return flipHorizontal; }
    public void setFlipHorizontal(boolean flipHorizontal) { this.flipHorizontal = flipHorizontal; }

    public boolean isFlipVertical() { return flipVertical; }
    public void setFlipVertical(boolean flipVertical) { this.flipVertical = flipVertical; }
}
