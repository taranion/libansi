package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function moves the user window up a specified number of lines in page memory.
 */
public class PanUp extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public PanUp() {
		super(0x54, "SD", Level.VT100);
	}

	//-------------------------------------------------------------------
	public PanUp(int value) {
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
