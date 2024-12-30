package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function moves the cursor to the specified line and column.
 * The starting point for lines and columns depends on the setting of origin mode (DECOM).
 * CUP applies only to the current page.
 */
public class CursorPosition extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public CursorPosition() {
		super(0x48, "CUP", Level.VT100);
	}

	//-------------------------------------------------------------------
	public CursorPosition(int x, int y) {
		this();
		parameter.clear();
		parameter.add(y);
		parameter.add(x);
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
	public void setLine(int val) {
		parameter.remove(0);
		parameter.add(0,val);
	}

	//-------------------------------------------------------------------
	/**
	 * @return indicates what column the cursor is at.
	 */
	public int getColumn() {
		int value = parameter.get(1);
		return (value==0)?1:value;
	}

	//-------------------------------------------------------------------
	public void setColumn(int val) {
		parameter.remove(1);
		parameter.add(val);
	}
}
