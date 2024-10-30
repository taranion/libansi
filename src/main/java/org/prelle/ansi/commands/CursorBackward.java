package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function moves the cursor to the left by a specified number of columns.
 * The cursor stops at the left border of the page.
 */
public class CursorBackward extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public CursorBackward() {
		super(0x44, "CUB", Level.VT100);
	}

	//-------------------------------------------------------------------
	public CursorBackward(int value) {
		this();
		parameter.clear();
		parameter.add(value);
	}

	//-------------------------------------------------------------------
	public int getValue() {
		int value = parameter.get(0);
		return (value==0)?1:value;
	}

}
