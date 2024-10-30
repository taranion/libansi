package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * 
 */
public class ScrollUp extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public ScrollUp() {
		super('S', "SU", Level.UNKNOWN);
	}

	//-------------------------------------------------------------------
	public ScrollUp(int value) {
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
