package sotasadventure;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * The class that has useful methods.
 * 
 * @author Sota Nishiyama
 * @since  1.0
 */
public class Util {

	/**
	 * Returns the image scaled into the designated size.
	 * 
	 * @param image the image to be scaled
	 * @param width the width of the image
	 * @param height the height of the image
	 * @return Image the scaled image
	 */
	public static Image getScaledImage(Image image, int width, int height) {
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resized.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(image, 0, 0, width, height, null);
		g2.dispose();

		return resized;
	}

	/**
	 * Returns the flipped image of the given image.
	 * 
	 * @param image the image to be flipped
	 * @return Image The flipped image
	 */
	public static Image getFlippedImage(Image image) {
		// convert image to buffered image
		BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = bimage.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();

		AffineTransform at = new AffineTransform();
		at.concatenate(AffineTransform.getScaleInstance(-1, 1));
		at.concatenate(AffineTransform.getTranslateInstance(-bimage.getWidth(), 0));

		BufferedImage flipped = new BufferedImage(bimage.getWidth(), bimage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		g2 = flipped.createGraphics();
		g2.transform(at);
		g2.drawImage(bimage, 0, 0, null);
		g2.dispose();

		return flipped;
	}

}