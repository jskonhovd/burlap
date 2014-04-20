package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;


/**
 * An instance of the {@link ActionGlyphPainter} that will render arrows to the graphics context.
 * @author Robert Hunter
 *
 */
public class SmallArrowActionGlyph implements ActionGlyphPainter {

	/**
	 * The direction of the arrow.0: north; 1: south; 2: east; 3:west
	 */
	protected int			direction;
	
	/**
	 * The color of the arrow
	 */
	protected Color			fillColor = Color.BLACK;
	
	
	/**
	 * creates an arrow action glyph painter in the specified direction
	 * @param direction 0: north; 1: south; 2: east; 3:west
	 */
	public SmallArrowActionGlyph(int direction){
		this.direction = direction;
	}
	
	
	
	@Override
	public void paintGlyph(Graphics2D g2, float x, float y, float width, float height) {
		
		BufferedImage glyphImage = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D img = (Graphics2D)glyphImage.getGraphics();

                int centerX = Math.round(width/2f);
                int centerY = Math.round(height/2f);
                int seg = Math.min(centerX, centerY) / 2;
                int[][] bbox = {{centerX - seg, centerY - seg},
                                {centerX + seg, centerY - seg},
                                {centerX + seg, centerY + seg},
                                {centerX - seg, centerY + seg}};

                int d = direction;
                switch (d) {
                case 0: case 3: break;
                case 1: d = 2; break;
                case 2: d = 1; break;
                default: break;
                }
                        
                int[][] points = {{centerX, centerY},
                                  {bbox[d][0], bbox[d][1]},
                                  {bbox[(d + 1) % 4][0], bbox[(d + 1) % 4][1]}};
                int triCenterX = (int)Math.round((points[0][0] + points[1][0] + points[2][0]) / 3.);
                int triCenterY = (int)Math.round((points[0][1] + points[1][1] + points[2][1]) / 3.);


                Polygon triangle = new Polygon();
                triangle.addPoint(points[0][0], points[0][1]);
                triangle.addPoint(points[1][0], points[1][1]);
                triangle.addPoint(points[2][0], points[2][1]);
		
                img.setPaint(Color.black);
                img.fill(triangle);

                AffineTransform tx = AffineTransform.getRotateInstance(Math.PI, triCenterX, triCenterY);
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                g2.drawImage(glyphImage, op, (int)x, (int)y);

	}

}