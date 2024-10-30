package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;
import org.prelle.ansi.commands.EraseInDisplay.Mode;

/**
 *
 */
public class DECResetMode extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public DECResetMode() {
		super(0x6C, "DECRM", Level.VT100);
		this.firstParam='?';
	}

	//-------------------------------------------------------------------
	public DECResetMode(DECSetMode.DECMode mode) {
		this();
		parameter.clear();
		parameter.add(mode.val);
	}

	//-------------------------------------------------------------------
	public Mode getValue() {
		int value = parameter.get(0);
		return Mode.valueOf(value);
	}

}
