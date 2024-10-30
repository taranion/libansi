package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * The active position is moved to the n-th character position of the active line.
 */
public class HorizontalPositionAbsolute extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public HorizontalPositionAbsolute() {
		super(0x60, "HPA", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public HorizontalPositionAbsolute(int value) {
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
