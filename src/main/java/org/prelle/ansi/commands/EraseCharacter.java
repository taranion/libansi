package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class EraseCharacter extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public EraseCharacter() {
		super(0x58, "ECH", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public EraseCharacter(int value) {
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
