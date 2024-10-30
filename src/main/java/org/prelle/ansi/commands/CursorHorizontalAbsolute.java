package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * The active position is moved to the n-th character position of the active line.
 */
public class CursorHorizontalAbsolute extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public CursorHorizontalAbsolute() {
		super(0x47, "CHA", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public CursorHorizontalAbsolute(int value) {
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
