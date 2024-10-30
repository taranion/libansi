package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class EraseInArea extends ControlSequenceFragment {

	public static enum Mode {
		AREA_FROM_CURSOR(0),
		AREA_TO_CURSOR(1),
		AREA(2);
		int val;
		Mode(int val) { this.val = val; }
		public int value() { return val; }
		public static Mode valueOf(int x) {
			for (Mode m : Mode.values())
				if (m.val==x) return m;
			return null;
		}
	}

	//-------------------------------------------------------------------
	public EraseInArea() {
		super(0x4A, "EA", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public EraseInArea(Mode value) {
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
