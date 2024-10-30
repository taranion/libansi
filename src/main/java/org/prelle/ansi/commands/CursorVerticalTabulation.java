package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * CVT causes the active presentation position to be moved to the corresponding character position of the
line corresponding to the n-th following line tabulation stop in the presentation component, where n
equals the value of Pn.
 */
public class CursorVerticalTabulation extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public CursorVerticalTabulation() {
		super(0x59, "CVT", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public CursorVerticalTabulation(int value) {
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
