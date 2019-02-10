package org.area515.resinprinter.job;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreationWorkshopImageCache {
    private static final Logger logger = LogManager.getLogger();

    /* Cache, with image index as key. */
    private int lastSliceIndex = 0;
    private int cacheMaxSize = 20;
    private HashMap<Integer, BufferedImage> cache = new HashMap<Integer, BufferedImage>();
    private ReentrantLock cacheLock = new ReentrantLock(true);

    private File parentFile;
    private String baseFilename;
    private String extension = new String(".png");
    private int padLength;

    public CreationWorkshopImageCache(File parentFile, String baseFilename, int padLength) {
        this.parentFile = parentFile;
        this.baseFilename = baseFilename;
        this.padLength = padLength;
    }

    private File getImageFile(int sliceIndex) {
        String sliceNumber = String.format("%0" + this.padLength + "d", sliceIndex);
        String imageFilename = this.baseFilename + sliceNumber + this.extension;
        File imageFile = new File(this.parentFile, imageFilename);
        return imageFile;
    }

    private BufferedImage loadImageFile(int sliceIndex) throws IOException {
        File imageFile = getImageFile(sliceIndex);
        logger.info("Loading image from file: {}.", imageFile.getName());
        BufferedImage image = ImageIO.read(imageFile);
        return image;
    }

    private void addImageToCache(int sliceIndex) throws IOException {
        cacheLock.lock();
        try {
            if (!cache.containsKey(sliceIndex))
            {
                BufferedImage image = loadImageFile(sliceIndex);
                cache.put(sliceIndex, image);
            }
        } finally {
            cacheLock.unlock();
        }
    }

    public BufferedImage getCachedOrLoadImage(int sliceIndex) throws IOException {
        BufferedImage image;
        cacheLock.lock();
        logger.info("Get or load cached image with index: {}.", sliceIndex);
        try {
            if (cache.containsKey(sliceIndex))
            {
                // Image already in cache.
                image = cache.get(sliceIndex);
                cache.remove(sliceIndex);
            }
            else {
                image = loadImageFile(sliceIndex);
            }
            this.lastSliceIndex = sliceIndex;
        } finally {
            cacheLock.unlock();
        }
        return image;
    }
}