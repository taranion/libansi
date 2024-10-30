package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * 
 */
public class ScrollDown extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public ScrollDown() {
		super('T', "SD", Level.UNKNOWN);
	}

	//-------------------------------------------------------------------
	public ScrollDown(int value) {
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
