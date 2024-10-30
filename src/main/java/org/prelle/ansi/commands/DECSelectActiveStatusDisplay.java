package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * <p>DECSASD selects whether the terminal sends data to the main display or the status line.
 * Available in: VT Level 4 mode only<br/>
 * Default: Main display</p>
 * 
 * @see <a href="https://vt100.net/docs/vt510-rm/DECSASD.html">VT510 Docs</a>
 */
public class DECSelectActiveStatusDisplay extends ControlSequenceFragment {

	public static enum Mode {
		MAIN_DISPLAY,
		STATUS_LINE,
	}

	//-------------------------------------------------------------------
	public DECSelectActiveStatusDisplay() {
		super("$", 0x7D, "DECSASD", Level.VT400);
	}

	//-------------------------------------------------------------------
	public DECSelectActiveStatusDisplay(Mode value) {
		this();
		this.parameter.clear();
		this.parameter.add(value.ordinal());
	}

	//-------------------------------------------------------------------
	public Mode getValue() {
		int value = parameter.get(0);
		return Mode.values()[value];
	}

}
