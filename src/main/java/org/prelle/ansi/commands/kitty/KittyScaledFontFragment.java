package org.prelle.ansi.commands.kitty;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.prelle.ansi.C0Code;
import org.prelle.ansi.C1Code;
import org.prelle.ansi.StringMessageFragment;

/**
 *
 */
public class KittyScaledFontFragment extends StringMessageFragment {
	
	public static enum VPos {
		TOP, BOTTOM, CENTERED
	}
	public static enum HPos {
		LEFT, RIGHT, CENTERED
	}
	

	/** 1..7: The overall scale, the text will be rendered in a block of s * w by s cells */
	protected int scale = 1;
	/** 0..7: The width, in cells, in which the text should be rendered. When zero, the terminal should calculate the width as it would for normal text, splitting it up into scaled cells. */
	protected int width;
	/** 0..15: The numerator for the fractional scale. */
	protected int numerator;
	/** 0..15: The denominator for the fractional scale. Must be > n when non-zero. */
	protected int denominator;
	protected VPos vertical=VPos.TOP;
	protected HPos horizontal=HPos.LEFT;
	protected String text;

	//-------------------------------------------------------------------
	public KittyScaledFontFragment() {
		super(C1Code.OSC, null);
	}

	//-------------------------------------------------------------------
	public KittyScaledFontFragment(int scale, String text) {
		super(C1Code.OSC, null);
		this.text = text;
		this.scale = scale;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		// Prepare line
		List<String> params = new ArrayList<>();
		if (scale!=1) params.add("s="+scale);
		if (width!=0) params.add("w="+width);
		if (numerator!=0) params.add("n="+numerator);
		if (denominator!=0) params.add("d="+denominator);
		if (vertical!=VPos.TOP) params.add("v="+vertical.ordinal());
		if (horizontal!=HPos.LEFT) params.add("h="+horizontal.ordinal());
		String metadata = String.join(":", params);
		
		data = "66;"+metadata+";"+text;
//		toFill.writeBytes(data.getBytes(StandardCharsets.US_ASCII));
		super.encode(toFill, use7Bit);

	}

	//-------------------------------------------------------------------
	public KittyScaledFontFragment setText(String value) {
		this.text = value;
		return this;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getNumerator() {
		return numerator;
	}

	public void setNumerator(int numerator) {
		this.numerator = numerator;
	}

	public int getDenominator() {
		return denominator;
	}

	public void setDenominator(int denominator) {
		this.denominator = denominator;
	}

	public VPos getVertical() {
		return vertical;
	}

	public void setVertical(VPos vertical) {
		this.vertical = vertical;
	}

	public HPos getHorizontal() {
		return horizontal;
	}

	public void setHorizontal(HPos horizontal) {
		this.horizontal = horizontal;
	}

}
