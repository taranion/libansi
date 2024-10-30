package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.DeviceAttributes.OperatingLevel;
import org.prelle.ansi.Level;

/**
 * Invoke the graphic rendition specified by the parameter(s). All following characters transmitted to the VT100 are rendered according to the parameter(s) until the next occurrence of SGR.
 */
public class SetConformanceLevel extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	SetConformanceLevel() {
		super("\"", 0x70, "DECSCL", Level.VT200);
	}

	//-------------------------------------------------------------------
	public SetConformanceLevel(OperatingLevel level) {
		this();
		this.parameter.clear();
		parameter.add(level.code());
	}

	//-------------------------------------------------------------------
	public SetConformanceLevel(OperatingLevel level, boolean sevenBit) {
		this();
		this.parameter.clear();
		parameter.add(level.code());
		parameter.add(sevenBit?1:0);
	}

}
