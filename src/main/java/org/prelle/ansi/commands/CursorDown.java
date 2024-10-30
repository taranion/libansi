package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function moves the cursor down a specified number of lines in the same column.
 * The cursor stops at the bottom margin. If the cursor is already below the bottom margin,
 * then the cursor stops at the bottom line.
 */
public class CursorDown extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public CursorDown() {
		super(0x42, "CUD", Level.VT100);
	}

	//-------------------------------------------------------------------
	public CursorDown(int value) {
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
