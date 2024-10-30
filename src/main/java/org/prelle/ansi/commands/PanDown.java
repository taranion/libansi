package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function moves the user window down a specified number of lines in page memory.
 */
public class PanDown extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public PanDown() {
		super(0x53, "SU", Level.VT100);
	}

	//-------------------------------------------------------------------
	public PanDown(int value) {
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
