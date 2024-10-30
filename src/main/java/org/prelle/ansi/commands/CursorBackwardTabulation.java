package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * The active position is moved to the character position corresponding to the n-th preceding horizontal tabulation stop. If an attempt is made to move the active position past the first character position on the line, then the active position stays at column one.
 */
public class CursorBackwardTabulation extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	CursorBackwardTabulation() {
		super(0x5A, "CBT", Level.ANSI);
		parameter.add(1);
	}

	//-------------------------------------------------------------------
	public CursorBackwardTabulation(int value) {
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
