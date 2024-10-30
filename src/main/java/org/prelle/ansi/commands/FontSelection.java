package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class FontSelection extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public FontSelection() {
		super(" ", 0x44, "FNT", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public FontSelection(int toChange, int register) {
		this();
		parameter.clear();
		parameter.add(toChange);
		parameter.add(register);
	}

	//-------------------------------------------------------------------
	public int getValue() {
		int value = parameter.get(0);
		return (value==0)?1:value;
	}

}
