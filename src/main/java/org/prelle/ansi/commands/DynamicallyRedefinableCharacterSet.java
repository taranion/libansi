package org.prelle.ansi.commands;

import org.prelle.ansi.DeviceControlFragment;
import org.prelle.ansi.Level;

/**
 * This control function moves the cursor to the specified line and column.
 * The starting point for lines and columns depends on the setting of origin mode (DECOM).
 * CUP applies only to the current page.
 */
public class DynamicallyRedefinableCharacterSet extends DeviceControlFragment {

	public static enum Erase {
		ALL_MATCHING_WIDTH_RENDITION,
		ONLY_RELOADED,
		ALL
	}

	public static enum TextOrFullCell {
		DEFAULT,
		TEXT,
		FULL_CELL
	}

	//-------------------------------------------------------------------
	public DynamicallyRedefinableCharacterSet() {
		super( (int)'{', "DECDLD", Level.VT200);
	}

	//-------------------------------------------------------------------
	/**
	 * @param fn Font number
	 * @param cn Starting character
	 * @param cmw Character matrix width
	 * @param ss Font set size
	 * @param t Text or full cell
	 * @param cmh Character matrix height
	 * @param css Character set size (94 or 96)
	 */
	public DynamicallyRedefinableCharacterSet(int fn, int cn, Erase e, int cmw, int ss, TextOrFullCell t, int cmh, int css, String data) {
		this();
		parameter.clear();
		parameter.add(fn);
		parameter.add(cn);
		parameter.add(e.ordinal());
		parameter.add(cmw);
		parameter.add(ss);
		parameter.add(t.ordinal());
		parameter.add(cmh);
		parameter.add(css);
		this.data = data;
	}

	//-------------------------------------------------------------------
	/**
	 * @param fn Font number
	 * @param cn Starting character
	 */
	public DynamicallyRedefinableCharacterSet(int fn, int cn, Erase e, String data) {
		this();
		this.data = data;
		parameter.clear();
		parameter.add(fn);
		parameter.add(cn);
		parameter.add(e.ordinal());
	}

	//-------------------------------------------------------------------
	/**
	 * @return indicates what line the cursor is on.
	 */
	public int getLine() {
		int value = parameter.get(0);
		return (value==0)?1:value;
	}

	//-------------------------------------------------------------------
	/**
	 * @return indicates what column the cursor is at.
	 */
	public int getColumn() {
		int value = parameter.get(1);
		return (value==0)?1:value;
	}

}
