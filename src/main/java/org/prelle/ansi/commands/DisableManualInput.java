package org.prelle.ansi.commands;

import org.prelle.ansi.EscapeSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class DisableManualInput extends EscapeSequenceFragment {

	//-------------------------------------------------------------------
	public DisableManualInput() {
		super(0x60, "DMI", Level.ANSI);
		// TODO Auto-generated constructor stub
	}

}
