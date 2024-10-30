package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class HorizontalPositionForward extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public HorizontalPositionForward() {
		super(0x6A, "HPB", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public HorizontalPositionForward(int value) {
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
