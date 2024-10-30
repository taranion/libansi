package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function erases characters from part or all of the display. When you erase complete lines, they become single-height, single-width lines, with all visual character attributes cleared. ED works inside or outside the scrolling margins.
 */
public class EraseInDisplay extends ControlSequenceFragment {

	public static enum Mode {
		SCREEN_FROM_CURSOR(0),
		SCREEN_TO_CURSOR(1),
		SCREEN(2);
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
	public EraseInDisplay() {
		super(0x4A, "ED", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public EraseInDisplay(Mode value) {
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
