package org.prelle.ansi.commands;

import org.prelle.ansi.EscapeSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class DesignateCharacterSet extends EscapeSequenceFragment {

	//-------------------------------------------------------------------
	public DesignateCharacterSet(int designateAs, String iif) {
		super("(", iif.charAt(iif.length()-1), "DSCS", Level.VT100);
		super.intermediate = ((char)(0x28+designateAs))+iif.substring(0, iif.length()-1);
	}

}
