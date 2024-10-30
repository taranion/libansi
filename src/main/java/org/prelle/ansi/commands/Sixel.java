package org.prelle.ansi.commands;

import java.util.List;

import org.prelle.ansi.DeviceControlFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class Sixel extends DeviceControlFragment {

	public static interface SixelControlFunction {}

	public static record Repeat(int count, char c) implements SixelControlFunction {
		public String toString() { return "!"+count+"c"; }
	}

	/**
	 * @param an Ratio1 (an:ad)
	 * @param ad Ratio2
	 * @param h Horizontal width of image
	 * @param v Vertical height of image
	 */
	public static record RasterAttribute(int an, int ad, int h, int v) implements SixelControlFunction {
		public String toString() { return String.format("\"%d;%d;%d;%d", an,ad,h,v); }
	}
	public static record SpecifyColor(int number, int unit, int x, int y, int z) implements SixelControlFunction {
		public String toString() { return String.format("#%d;%d;%d;%d;%d", number,unit,x,y,z); }
	}
	public static record UseColor(int number) implements SixelControlFunction {
		public String toString() { return String.format("#%d", number); }
	}
	public static record CR() implements SixelControlFunction {
		public String toString() { return "$"; }
	}
	public static record NewLine() implements SixelControlFunction {
		public String toString() { return "-"; }
	}
	public static record SixelData(String data) implements SixelControlFunction {
		public String toString() { return this.data(); }
	}


	public static enum BackgroundMode {
		BACKGROUND_COLOR,
		TRANSPARENT,
		/** Default is BACKGROUND_COLOR */
		DEFAULT
	}

	//-------------------------------------------------------------------
	public Sixel() {
		super(0x71, "SIXEL", Level.VT300);
	}

	//-------------------------------------------------------------------
	/**
	 */
	public Sixel(int p1, BackgroundMode p2, String sixelData) {
		this();
		parameter.clear();
		parameter.add(p1);
		parameter.add(p2.ordinal());
		parameter.add(0);
		this.data = sixelData;
	}

	//-------------------------------------------------------------------
	/**
	 */
	public Sixel(int p1, BackgroundMode p2, List<SixelControlFunction> data) {
		this();
		parameter.clear();
		parameter.add(p1);
		parameter.add(p2.ordinal());
		StringBuffer buf = new StringBuffer();
		for (SixelControlFunction tmp :data)
			buf.append(tmp.toString());
		this.data = buf.toString();
	}

}
