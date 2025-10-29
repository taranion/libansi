package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;
import org.prelle.ansi.commands.SetMode.ANSIMode;

/**
 *
 */
public class ResetMode extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public ResetMode() {
		super(0x6C, "RM", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public ResetMode(ANSIMode mode) {
		this();
		parameter.clear();
		parameter.add(mode.val);
	}

	//-------------------------------------------------------------------
	public ANSIMode getValue() {
		int value = parameter.get(0);
		return ANSIMode.valueOf(value);
	}

}
