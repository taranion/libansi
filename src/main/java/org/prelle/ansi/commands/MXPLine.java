package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class MXPLine extends ControlSequenceFragment {
	
	public static enum MXPLineValue {
		OPEN_LINE(0),
		SECURE_LINE(1),
		LOCKED_LINE(2),
		SECURE_TEMP(4),
		OPEN_LOCK(5),
		SECURE_LOCK(6),
		LOCKED_LOCK(7)
		;
		int num;
		MXPLineValue(int num) {
			this.num=num;
		}
		public static MXPLineValue byValue(int num) {
			for (MXPLineValue tmp : MXPLineValue.values()) {
				if (tmp.num==num) return tmp;
			}
			return null;
		}
		public int getValue() { return num; }
	}

	//-------------------------------------------------------------------
	public MXPLine() {
		super(0x7a, "MXP", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public MXPLine(MXPLineValue val) {
		super(0x7a, "MXP", Level.ANSI);
		parameter.clear();
		parameter.add(val.getValue());
	}

	//-------------------------------------------------------------------
	public int getValue() {
		int value = parameter.get(0);
		return value;
	}

	//-------------------------------------------------------------------
	public MXPLineValue getInterpretedValue() {
		int value = parameter.get(0);
		return MXPLineValue.byValue(value);
	}

}
