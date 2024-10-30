package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function lets the host select the type of status line displayed.
 * Available in: VT Level 4 mode only
 * Default: Indicator status display
 * @see https://vt100.net/docs/vt510-rm/DECSSDT.html
 */
public class DECSelectStatusDisplayType extends ControlSequenceFragment {

	public static enum Mode {
		NO_STATUS_LINE,
		INDICATOR_STATUS_LINE,
		HOST_WRITABLE_STATUS_LINE
	}

	//-------------------------------------------------------------------
	public DECSelectStatusDisplayType() {
		super("$", 0x7E, "DECSSDT", Level.VT400);
	}

	//-------------------------------------------------------------------
	public DECSelectStatusDisplayType(Mode value) {
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
