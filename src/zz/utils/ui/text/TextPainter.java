/*
 * Created on Jun 2, 2004
 */
package zz.utils.ui.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utilities to paint text.
 * 
 * @author gpothier
 */
public class TextPainter
{
	public static enum VerticalAlignment {
		TOP, CENTER, BOTTOM
	}
	
	public static enum HorizontalAlignment {
		LEFT, CENTER, RIGHT
	}
	
	/**
	 * A font constant.
	 */
	public static final Font SANS_SERIF_PLAIN_10 = new Font("SansSerif", Font.PLAIN, 10);
	
	/**
	 * A default graphics that can be used for font metrics calculations
	 */
	private static Graphics2D DEFAULT_GRAPHICS;
	
	/**
	 * Returns a graphics that can be used for font metrics computations.
	 */
	public static Graphics2D getDefaultGraphics()
	{
		if (DEFAULT_GRAPHICS == null)
		{
			GraphicsConfiguration theConfiguration = 
				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
			
			BufferedImage theImage = theConfiguration.createCompatibleImage(1, 1);
			DEFAULT_GRAPHICS = theImage.createGraphics();
		}
		return DEFAULT_GRAPHICS;
	}



	public static AttributedString createAttributedString(Font aFont,
			boolean aUnderline,
			Paint aColor,
			String aText)
	{
		AttributedString theString = new AttributedString(aText);
		
		if (aColor != null) theString.addAttribute(TextAttribute.FOREGROUND, aColor);
		if (aFont != null) theString.addAttribute(TextAttribute.FONT, aFont);
		theString.addAttribute(
				TextAttribute.UNDERLINE, 
				aUnderline ? TextAttribute.UNDERLINE_ON : null);
		
		return theString;
	}
	
	public static void paint(
			Graphics2D aGraphics, 
			Font aFont,
			boolean aUnderline,
			Paint aColor,
			String aText,
			Shape aShape,
			VerticalAlignment aVAlign,
			HorizontalAlignment aHAlign)
	{
		if (aText == null || aText.length() == 0 || aColor == null) return;
		
		AttributedString theString = createAttributedString(aFont, aUnderline, aColor, aText);
		
		paint (aGraphics, theString, aShape, aVAlign, aHAlign);
	}
	
	/**
	 * Paints a text in the specified shape.
	 * 
	 * @param aGraphics The graphics where to paint the text
	 * @param aText The text to paint
	 * @param aShape Shape in which the text should fit.
	 */
	public static void paint(
			Graphics2D aGraphics, 
			AttributedString aText, 
			Shape aShape,
			VerticalAlignment aVAlign,
			HorizontalAlignment aHAlign)
	{
		Rectangle2D theBounds;
		
		if (aShape instanceof Rectangle2D)
		{
			theBounds = (Rectangle2D) aShape;
		}
		else if (aShape instanceof Ellipse2D)
		{
			Ellipse2D theEllipse = (Ellipse2D) aShape;
			double theX = theEllipse.getMinX();
			double theY = theEllipse.getMinY();
			double theW = theEllipse.getWidth();
			double theH = theEllipse.getHeight();
			
			theBounds = new Rectangle2D.Double (
					theX + theW/4, theY + theH/4,
					theW/2, theH/2);
		}
		else theBounds = aShape.getBounds2D();

		paint(aGraphics, aText, theBounds, aVAlign, aHAlign);
	}

	public static void paint(
			Graphics2D aGraphics, 
			AttributedString aText, 
			Rectangle2D aBounds,
			VerticalAlignment aVAlign,
			HorizontalAlignment aHAlign)
	{
		if (aText == null) return;
		
		// Save previous clip & set new clip
		Shape thePreviousClip = aGraphics.getClip();
		aGraphics.clip(aBounds);
		
		FontRenderContext theContext = aGraphics.getFontRenderContext();

		float theX = (float) aBounds.getX();
		float theW = (float) aBounds.getWidth();
		float theH = (float) aBounds.getHeight();
				
		AttributedCharacterIterator theIterator = aText.getIterator();

		LineBreakMeasurer theMeasurer = new LineBreakMeasurer(theIterator, theContext);

		List <TextLayout>  theListLayout=new ArrayList<TextLayout>();  
		
		float theY = 0;
		while (theMeasurer.getPosition() < theIterator.getEndIndex() && theY < theH)
		{
			TextLayout theLayout = theMeasurer.nextLayout(theW);
			theY = theY + theLayout.getAscent()+theLayout.getDescent() +theLayout.getLeading();
			theListLayout.add(theLayout);
		}
		
		
		float theYMargin = 0;
		
		switch (aVAlign)
		{
			case TOP: theYMargin = 0;break;
			case CENTER:  theYMargin = (theH-theY)/2f; break;
			case BOTTOM:  theYMargin = theH-theY;break;	
		}
		
		theY = (float) aBounds.getY() + theYMargin;
		for(TextLayout theLayout : theListLayout)
		{
			theY += theLayout.getAscent();
			float theLineW = (float) theLayout.getBounds().getWidth();
			
			float theXMargin = 0;
			switch (aHAlign)
			{
				case LEFT: theXMargin = 0;break;
				case CENTER: theXMargin = (theW - theLineW)/2f;break;
				case RIGHT:	theXMargin = theW - theLineW;break;
			}
			
			theLayout.draw(aGraphics, theX + theXMargin, theY);
			theY += theLayout.getDescent() +theLayout.getLeading();
		}
		
		// restore previous clip
		aGraphics.setClip(thePreviousClip);
	}

	/**
	 * Computes the size of the given text.
	 * @param aGraphics A graphics used to perform computations. If no graphics
	 * is available use {@link #getDefaultGraphics()}.
	 */
	public static Point2D computeSize(
			Graphics2D aGraphics, 
			Font aFont,
			boolean aUnderline,
			String aText)
	{
		AttributedString theString = createAttributedString(aFont, aUnderline, null, aText);
		return computeSize(aGraphics, theString);
	}
	
	/**
	 * Computes the size of the given text.
	 * @param aGraphics A graphics used to perform computations. If no graphics
	 * is available use {@link #getDefaultGraphics()}.
	 */
	public static Point2D computeSize(
			Graphics2D aGraphics, 
			AttributedString aText)
	{
		FontRenderContext theContext = aGraphics.getFontRenderContext();
		AttributedCharacterIterator theIterator = aText.getIterator();

		TextLayout theLayout = new TextLayout(theIterator, theContext);
		
		Rectangle2D theBounds = theLayout.getBounds();
		
		return new Point2D.Double(theBounds.getWidth() + 2, theBounds.getHeight() + 2);
	}
}