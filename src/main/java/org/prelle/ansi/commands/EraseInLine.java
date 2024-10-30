package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function erases characters from part or all of the display. When you erase complete lines, they become single-height, single-width lines, with all visual character attributes cleared. ED works inside or outside the scrolling margins.
 */
public class EraseInLine extends ControlSequenceFragment {

	public static enum Mode {
		LINE_FROM_CURSOR(0),
		LINE_TO_CURSOR(1),
		LINE(2);
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
	public EraseInLine() {
		super(0x4B, "EL", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public EraseInLine(Mode value) {
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
