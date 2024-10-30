package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function moves the cursor to the right by a specified number of columns.
 * The cursor stops at the right border of the page.
 */
public class CursorForward extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public CursorForward() {
		super(0x43, "CUF", Level.VT100);
	}

	//-------------------------------------------------------------------
	public CursorForward(int value) {
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
