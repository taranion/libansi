package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class DeviceStatusReport extends ControlSequenceFragment {

	public static enum Type {
		READY(0),
		BUSY_REQUEST_LATER(1),
		BUSY_REPORT_LATER(2),
		MALFUNCTION_REQUEST_LATER(3),
		MALFUNCTION_REPORT_LATER(4),
		REQUEST(5),
		CURSOR_POS(6),
		;
		int val;
		Type(int val) { this.val = val; }
		public int value() { return val; }
		public static Type valueOf(int x) {
			for (Type m : Type.values())
				if (m.val==x) return m;
			return null;
		}
	}

	//-------------------------------------------------------------------
	public DeviceStatusReport() {
		super(0x6E, "DSR", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public DeviceStatusReport(Type value) {
		this();
		parameter.clear();
		parameter.add(value.val);
	}

	//-------------------------------------------------------------------
	public int getValue() {
		int value = parameter.get(0);
		return (value==0)?1:value;
	}

}
