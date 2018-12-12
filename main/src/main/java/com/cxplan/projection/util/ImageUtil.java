package com.cxplan.projection.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.plugin2.util.SystemUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * EasyImage lets you do all the basic image operations -  
 * converting, cropping, resizing, rotating, flipping…
 * Plus it let’s you do some really cool affects.
 * All is done super easily.
 * Combining operations can produce some very cool results.
 * 
 * Operations:
    * Open image.
    * Save image
    * Convert image
    * Re-size image
    * Crop image
    * Convert to black and white image
    * Rotate image
    * Flip image
    * Add color to image
    * Create image with multiple instance of the original
    * Combining 2 images together
    * Emphasize parts of the image
    * Affine transform image
 *
 * @author kenny
 */
public class ImageUtil {

    private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);

    /**
     * Resizing the image by percentage of the original.
     * @param percentOfOriginal
     */
    public static BufferedImage resize(BufferedImage bufferedImage, int percentOfOriginal){
        int newWidth = bufferedImage.getWidth()  * percentOfOriginal / 100;
        int newHeight = bufferedImage.getHeight() * percentOfOriginal / 100;
        return resize(bufferedImage, newWidth, newHeight);
    }
        
    /**
     * Resizing the image by width and height. 
     * @param newWidth
     * @param newHeight
     */
    public static BufferedImage resize(BufferedImage bufferedImage, int newWidth, int newHeight){
        
        int oldWidth = bufferedImage.getWidth();
        int oldHeight = bufferedImage.getHeight();
        
        if(newWidth == -1 || newHeight == -1){
            if(newWidth == -1){
                if(newHeight == -1){
                    return bufferedImage;
                } 
                
                newWidth = newHeight * oldWidth/ oldHeight;
            }
            else {
                newHeight = newWidth * oldHeight / oldWidth;
            }
        }
        
        BufferedImage result =
            new BufferedImage(newWidth , newHeight, BufferedImage.TYPE_INT_BGR);
        
        double widthSkip =  new Double(oldWidth-newWidth) / new Double(newWidth);
        double heightSkip =  new Double(oldHeight-newHeight) / new Double(newHeight);
        
        double widthCounter = 0;
        double heightCounter = 0;
        
        int newY = 0;
        
        boolean isNewImageWidthSmaller = widthSkip >0; 
        boolean isNewImageHeightSmaller = heightSkip >0; 
            
        for (int y = 0; y < oldHeight && newY < newHeight; y++) {
            
            if(isNewImageHeightSmaller && heightCounter > 1){ //new image suppose to be smaller - skip row
                heightCounter -= 1;
            }
            else if (heightCounter < -1){ //new image suppose to be bigger - duplicate row
                heightCounter += 1;
                
                if(y > 1)
                    y = y - 2;
                else
                    y = y - 1;
            }
            else{
               
                heightCounter += heightSkip;
                
                int newX = 0;
                
                for (int x = 0; x < oldWidth && newX < newWidth; x++) {
                   
                    if(isNewImageWidthSmaller && widthCounter > 1){ //new image suppose to be smaller - skip column
                        widthCounter -= 1;
                    }
                    else if (widthCounter < -1){ //new image suppose to be bigger - duplicate pixel
                        widthCounter += 1;
                        
                        if(x >1)
                            x = x - 2;
                        else
                            x = x - 1;
                    }
                    else{
                        
                        int rgb = bufferedImage.getRGB(x, y);
                        result.setRGB(newX, newY, rgb);
                        
                        newX++;
                        
                        widthCounter += widthSkip;
                    }
                    
                }
                
                newY++;
            }
            
            
            
        }
        
        
        return result;

    }
    public static double calculateZoomRate(Image image, int maxLength) {
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);

        if (imageWidth > imageHeight) {
            return ((double) maxLength) / imageWidth;
        } else {
            return ((double) maxLength) /imageHeight;
        }
    }
    /**
     * Add color to the RGB of the pixel
     * @param numToAdd
     */
    public static BufferedImage addPixelColor(BufferedImage bufferedImage, int numToAdd){
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                int rgb = bufferedImage.getRGB(x, y);
                bufferedImage.setRGB(x, y, rgb+numToAdd);
            }
        }

        return bufferedImage;
    }
    
    /**
     * Covert image to black and white.
     */
    public static BufferedImage convertToBlackAndWhite(BufferedImage bufferedImage) {
        ColorSpace gray_space = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp convert_to_gray_op = new ColorConvertOp(gray_space, null);
        convert_to_gray_op.filter(bufferedImage, bufferedImage);

        return bufferedImage;
    }
    
    
    /**
     * Rotates image 90 degrees to the left.
     */
    public static BufferedImage rotateLeft(BufferedImage bufferedImage){
        
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        BufferedImage result = new BufferedImage(height, 
                width, bufferedImage.TYPE_INT_BGR);
        
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(y, x, rgb); 
            }
        }
        
        return result;
        
    }
    
    /**
     * Rotates image 90 degrees to the right.
     */
    public static BufferedImage rotateRight(BufferedImage bufferedImage){
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        BufferedImage result = new BufferedImage(height, 
                width, bufferedImage.TYPE_INT_BGR);
        
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(height-y-1, x, rgb); 
            }
        }
        
        return result;
        
    }
    
    
    /**
     * Rotates image 180 degrees.
     */
    public static BufferedImage rotate180(BufferedImage bufferedImage){
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        BufferedImage result = new BufferedImage(width, 
                height, bufferedImage.TYPE_INT_BGR);
        
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(width-x-1, height-y-1, rgb); 
            }
        }
        
        return result;
        
    }
    
    /**
     * Flips the image horizontally
     */
    public static BufferedImage flipHorizontally(BufferedImage bufferedImage){
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        BufferedImage result = new BufferedImage(width, 
                height, bufferedImage.TYPE_INT_BGR);
        
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(width-x-1, y, rgb); 
            }
        }
        
        return result;
        
    }
    
    /**
     * Flips the image vertically.
     */
    public static BufferedImage flipVertically(BufferedImage bufferedImage){
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        BufferedImage result = new BufferedImage(width, 
                height, bufferedImage.TYPE_INT_BGR);
        
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(x, height-y-1, rgb); 
            }
        }
        
        return result;
        
    }
    
    /**
     * Multiply the image.
     * @param timesToMultiplyVertically
     * @param timesToMultiplyHorizantelly
     */
    public static BufferedImage multiply(BufferedImage bufferedImage, int timesToMultiplyVertically,
            int timesToMultiplyHorizantelly){
        return multiply(bufferedImage, timesToMultiplyVertically,timesToMultiplyHorizantelly,0);
    }
    
    /**
     * Multiply the image and also add color each of the multiplied images.
     * @param timesToMultiplyVertically
     * @param timesToMultiplyHorizantelly
     */
    public static BufferedImage multiply(BufferedImage bufferedImage, int timesToMultiplyVertically,
            int timesToMultiplyHorizantelly, int colorToHenhancePerPixel){
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        BufferedImage result = new BufferedImage(width*timesToMultiplyVertically, 
                height*timesToMultiplyHorizantelly, bufferedImage.TYPE_INT_BGR);
        
        for (int xx = 0; xx < timesToMultiplyVertically; xx ++) {
            for (int yy = 0; yy < timesToMultiplyHorizantelly; yy ++) {
                for (int x = 0; x < width; x ++) {
                    for (int y = 0; y < height; y ++) {
                        int rgb = bufferedImage.getRGB(x, y);
                        result.setRGB(width*xx+x, height*yy+y, rgb+colorToHenhancePerPixel*(yy+xx));
                       
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Combines the image with another image in an equal presence to both;
     * @param newImagePath - image to combine with
     */
    public static BufferedImage combineWithPicture(BufferedImage bufferedImage, String newImagePath){
        return combineWithPicture(bufferedImage, newImagePath, 2);
    }
    
    
    
    /**
     * Combines the image with another image.
     * jump = 2 means that every two pixels the new image is replaced. 
     * This makes the 2 images equal in presence. If jump=3 than every 3rd
     * pixel is replaced by the new image.
     * As the jump is higher this is how much the new image has less presence.
     * 
     * @param newImagePath
     * @param jump 
     */
    public static BufferedImage combineWithPicture(BufferedImage bufferedImage, String newImagePath, int jump){
        try {
            BufferedImage bufferedImage2 = ImageIO.read(new File(newImagePath));
            return combineWithPicture(bufferedImage, bufferedImage2, jump, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public void combineWithPicture(BufferedImage bufferedImage, BufferedImage bufferedImage2){
        combineWithPicture(bufferedImage, bufferedImage2, 2, null);
    }
    public void combineWithPicture(BufferedImage bufferedImage, BufferedImage bufferedImage2, int jump){
            combineWithPicture(bufferedImage, bufferedImage2, jump, null);
    }
    
    public void combineWithPicture(BufferedImage bufferedImage, BufferedImage bufferedImage2, Color ignoreColor){
        combineWithPicture(bufferedImage, bufferedImage2, 2, ignoreColor);
    }
    public static BufferedImage combineWithPicture(BufferedImage bufferedImage, BufferedImage bufferedImage2, int jump, Color ignoreColor){
        return doCombineWithPicture(bufferedImage, bufferedImage2, jump, ignoreColor);
    }
    
    /**
     * Combines the image with another image.
     * jump = 2 means that every two pixels the new image is replaced. 
     * This makes the 2 images equal in presence. If jump=3 than every 3rd
     * pixel is replaced by the new image.
     * As the jump is higher this is how much the new image has less presence.
     *  
     * ignoreColor is a color in the new image that will not be copied - 
     * this is good where there is a background which you do not want to copy.
     *  
     * @param bufferedImage2
     * @param jump
     * @param ignoreColor
     */
    private static BufferedImage doCombineWithPicture(BufferedImage bufferedImage,BufferedImage bufferedImage2,
            int jump, Color ignoreColor){
        checkJump(jump);
        
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        int width2 = bufferedImage2.getWidth();
        int height2 = bufferedImage2.getHeight();
        
        int ignoreColorRgb = -1;
        
        if(ignoreColor != null){
            ignoreColorRgb = ignoreColor.getRGB();
        }
        
        for (int y = 0; y < height; y ++) {
            for (int x = y%jump; x < width; x +=jump) {
                if(x >= width2 || y>= height2){
                    continue; 
                }
                
                int rgb = bufferedImage2.getRGB(x, y);
                
                if( rgb != ignoreColorRgb ){
                    bufferedImage.setRGB(x, y, rgb);
                }
            }
        }

        return bufferedImage;
        
    }
    
    
    public static BufferedImage crop(BufferedImage bufferedImage, int startX, int startY, int endX, int endY){
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        if(startX == -1){
            startX = 0;
        }
        
        if(startY == -1){
            startY = 0;
        }
        
        if(endX == -1){
            endX = width-1;
        }
        
        if(endY == -1){
            endY = height-1;
        }
        
        BufferedImage result = new BufferedImage(endX-startX,
                endY-startY+1, bufferedImage.TYPE_4BYTE_ABGR);
        
        for (int y = startY; y < endY; y ++) {
            for (int x = startX; x < endX; x ++) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(x-startX, y-startY, rgb); 
            }
        }
        return result;
    }
    
    public static BufferedImage emphasize(BufferedImage bufferedImage, int startX, int startY, int endX, int endY){
        return emphasize(bufferedImage, startX, startY, endX, endY, Color.BLACK, 3 );
    }
    
    public static BufferedImage emphasize(BufferedImage bufferedImage, int startX, int startY, int endX, int endY, Color backgroundColor){
        return emphasize(bufferedImage ,startX, startY, endX, endY, backgroundColor, 3 );
    }
    
    public static BufferedImage emphasize(BufferedImage bufferedImage, int startX, int startY, int endX, int endY,int jump){
        return emphasize(bufferedImage, startX, startY, endX, endY, Color.BLACK, jump );
    }
    public static BufferedImage emphasize(BufferedImage bufferedImage, int startX, int startY, int endX, int endY, Color backgroundColor,int jump){
        
        checkJump(jump);
        
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        if(startX == -1){
            startX = 0;
        }
        
        if(startY == -1){
            startY = 0;
        }
        
        if(endX == -1){
            endX = width-1;
        }
        
        if(endY == -1){
            endY = height-1;
        }
        
        
        for (int y = 0; y < height; y ++) {
            for (int x = y%jump; x < width; x +=jump) {
                
                if(y >= startY && y <= endY && x >= startX && x <= endX){
                    continue;
                }
                
                bufferedImage.setRGB(x, y, backgroundColor.getRGB()); 
            }
        }

        return bufferedImage;
    }
    
    private static void checkJump(int jump) {
        if(jump<1){
            throw new RuntimeException("Error: jump can not be less than 1");
        }
        
    }

    public static BufferedImage addColorToImage(BufferedImage bufferedImage, Color color, int jump){
        return addColorToImage(bufferedImage, color.getRGB(),jump);
    }
    
    public static BufferedImage addColorToImage(BufferedImage bufferedImage, int rgb, int jump){
        checkJump(jump);
        
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        for (int y = 0; y < height; y ++) {
            for (int x = y%jump; x < width; x +=jump) {
                bufferedImage.setRGB(x, y, rgb); 
            }
        }

        return bufferedImage;
    }
    
    
    
    public static BufferedImage affineTransform (BufferedImage bufferedImage, double fShxFactor, double fShyFactor) {

        try {
          AffineTransform shearer =
            AffineTransform.getShearInstance (fShxFactor, fShyFactor);
          AffineTransformOp shear_op =
            new AffineTransformOp (shearer, null);
          bufferedImage = shear_op.filter (bufferedImage, null);
        }
        catch (Exception e) {
          System.out.println("Shearing exception = " + e);
        }

        return bufferedImage;
      }

   /**
    * Read image from specified file.
    */
    public static BufferedImage readImage(File file) {
        Image src = Toolkit.getDefaultToolkit().getImage(file.getPath());
        BufferedImage bufferedImage = convert2BufferedImage(src);

        return bufferedImage;
    }
    public static BufferedImage readImage(byte[] data) {
        if (data == null) {
            return null;
        }
        return readImage(data, 0, data.length);
    }
    public static BufferedImage readImage(byte[] data, int offset, int length) {
        if (data == null || data.length == 0 || length == 0) {
            return null;
        }
        Image src = Toolkit.getDefaultToolkit().createImage(data, offset, length);
        BufferedImage bufferedImage = convert2BufferedImage(src);

        return bufferedImage;
    }
    public static byte[] image2Bytes(Image image, String format) {
        return image2Bytes(image, format, -1);
    }
    /**
     * Convert image object to byte array with specified format
     * @param image the image object
     * @param format the format of converted image data
     * @param maxDimension The max length of width or height,
     *                     the image will be resized if the width or height is greater than maxDimension.
     *                     The value "-1" indicates the raw image will be accepted without validation.
     */
    public static byte[] image2Bytes(Image image, String format, int maxDimension) {
        if (image == null) {
            return null;
        }

        if (!(image instanceof BufferedImage)) {
            image = convert2BufferedImage(image);
        }
        if (maxDimension != -1) {//check the dimension of image.
            double zoomRate = calculateZoomRate(image, maxDimension);
            if (zoomRate != 1.0D) {
                image = resize((BufferedImage)image, (int)(zoomRate * 100));
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        try {
            ImageIO.write((BufferedImage)image, format, outputStream);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        byte[] ret =  outputStream.toByteArray();
        try {
            outputStream.close();
        } catch (IOException e) {
        }
        return ret;
    }
    /**
     * Save image object to local file with specified format.
     * @param image the image object
     * @param format the format of converted image data
     */
    public static void image2File(Image image, String format, File file) throws IOException {
        if (image == null) {
            return;
        }

        if (!(image instanceof BufferedImage)) {
            image = convert2BufferedImage(image);
        }
        ImageIO.write((BufferedImage)image, format, file);
    }

    public static BufferedImage convert2BufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }
        ImageIcon ii = new ImageIcon(image);
        int width = ii.getIconWidth();
        int height = ii.getIconHeight();

        int type = BufferedImage.TYPE_4BYTE_ABGR;
        BufferedImage bimage = new BufferedImage(width,
                height, type);
        // Copy image to buffered image
        Graphics2D g = bimage.createGraphics();
        bimage = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.OPAQUE);
        g.dispose();
        g = bimage.createGraphics();
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, Color.white, null);
        g.dispose();

        return bimage;
    }

    private static int mediaTrackerID = 0;

    public static int trackImage(MediaTracker mTracker, Image image) {
        int id = mediaTrackerID++;
        mTracker.addImage(image, id);
        try {
            mTracker.waitForID(id, 0);
        } catch (InterruptedException e) {
            System.out.println("INTERRUPTED while loading Image");
        }
        int statusID = mTracker.statusID(id, false);
        mTracker.removeImage(image, id);

        return statusID;
    }

    /**
     * round angle.
     *
     * @param srcImage
     * @param radius
     * @return
     * @throws IOException
     */
    public static BufferedImage setRadius(Image srcImage, int width, int height, int radius) throws IOException
    {

        if (srcImage.getWidth(null) > width || srcImage.getHeight(null) > height)
        {
            ImageIcon imageIcon = new ImageIcon();
            imageIcon.setImage(srcImage.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            srcImage = imageIcon.getImage();
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs = image.createGraphics();
        gs.setComposite(AlphaComposite.Src);
        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gs.setColor(Color.WHITE);
        gs.fill(new RoundRectangle2D.Float(0, 0, width, height, radius, radius));
        gs.setComposite(AlphaComposite.SrcAtop);
        gs.drawImage(srcImage, 0, 0, null);
        gs.dispose();
        return image;
    }

    private static String getFileType(File file) {
        String fileName = file.getName();
        int idx =  fileName.lastIndexOf(".");
        if(idx == -1){
            throw new RuntimeException("Invalid file name");
        }
        
        return fileName.substring(idx+1);
    }

    public static final short MARKER_POS_LEFT_TOP = 1;
    public static final short MARKER_POS_LEFT_MIDDLE = 2;
    public static final short MARKER_POS_LEFT_BOTTOM = 3;
    public static final short MARKER_POS_RIGHT_TOP = 4;
    public static final short MARKER_POS_RIGHT_MIDDLE = 5;
    public static final short MARKER_POS_RIGHT_BOTTOM = 6;
    public static final short MARKER_POS_CENTER = 7;

    public static BufferedImage drawImageMarker(BufferedImage image, BufferedImage markImage, float alpha, int markerDegree, short markerPos) {
        //
        int w = image.getWidth(),h = image.getHeight();
        BufferedImage newImage = new BufferedImage(w , h,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = newImage.createGraphics();
        // draw start
        g.drawImage(image, 0, 0, w, h, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        //rotate marker image
        if (markerDegree > 0) {
            g.rotate(Math.toRadians(markerDegree), image.getWidth() / 2, image.getHeight() / 2);
        }
        int x = 0, y = 0;
        if (markerPos == MARKER_POS_LEFT_TOP) {
            x = 0;
            y = 0;
        } else if (markerPos == MARKER_POS_LEFT_MIDDLE) {
            x = 0;
            y = (h - markImage.getHeight()) / 2;
        } else if (markerPos == MARKER_POS_LEFT_BOTTOM) {
            x = 0;
            y = h - markImage.getHeight();
        } else if (markerPos == MARKER_POS_RIGHT_TOP) {
            x = w - markImage.getWidth();
            y = 0;
        } else if (markerPos == MARKER_POS_RIGHT_MIDDLE) {
            x = w - markImage.getWidth();
            y = (h - markImage.getHeight()) / 2;
        } else if (markerPos == MARKER_POS_RIGHT_BOTTOM) {
            x = w - markImage.getWidth();
            y = h - markImage.getHeight();
        } else if (markerPos == MARKER_POS_CENTER) {
            x = (w - markImage.getWidth()) / 2;
            y = (h - markImage.getHeight()) / 2;
        }

        g.drawImage(markImage, x, y, markImage.getWidth(), markImage.getHeight(), null);
        g.dispose();

        return newImage;
    }

    
    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(300, 100, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = image.createGraphics();
        image = g2d.getDeviceConfiguration().createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = image.createGraphics();

        String drawString = "映课情报课";
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLUE);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        Font font = new Font("宋体", Font.BOLD, 50);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics(font);
        int strWidth = fm.stringWidth(drawString);
        int strHeight = fm.getHeight();
        int x = (image.getWidth() - strWidth) / 2;
        g2d.setStroke(new BasicStroke(4.0f));
        g2d.drawString(drawString, x,strHeight);
        g2d.dispose();

        try {
            BufferedImage main = ImageIO.read(new File("d:\\main.jpg"));
            BufferedImage logo = ImageIO.read(new File("d:\\映课.png"));
            BufferedImage result = drawImageMarker(main, image, 0.3f, 45, MARKER_POS_LEFT_MIDDLE);
            image2File(result, "png", new File("d:\\file.jpg"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}