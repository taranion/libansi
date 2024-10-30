package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * Moves the cursor up a specified number of lines in the same column. The cursor stops at the top margin.
 * If the cursor is already above the top margin, then the cursor stops at the top line.
 */
public class CursorUp extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public CursorUp() {
		super(0x41, "CUU", Level.VT100);
	}

	//-------------------------------------------------------------------
	public CursorUp(int value) {
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
