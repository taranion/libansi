package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class DECSetMode extends ControlSequenceFragment {

	public static enum DECMode {
		/** 
		 * The reset state causes any displayable characters received when the cursor is at the right margin to replace any previous characters there. 
		 * The set state causes these characters to advance to the start of the next line, doing a scroll up if required and permitted. 
		 */
		WRAP_AROUND_MODE(7),
		X10_MOUSE(9),
		TEXT_CURSOR_ENABLE(25),
		OLD_ALTERNATE_BUFFER(47),
		VT200_MOUSE(1000),
		VT200_HIGHLIGHT_MOUSE(1001),
		BTN_EVENT_MOUSE(1002),
		EXT_MODE_MOUSE(1005),
		SGR_EXT_MODE_MOUSE(1006),
		URXVT_EXT_MODE_MOUSE(1015),
		PIXEL_POSITION_MOUSE(1016),
		ALTERNATE_BUFFER(1047),
		ALTERNATE_BUFFER_SAVE_CURSOR(1049),
		;
		int val;
		DECMode(int val) { this.val = val; }
		public int value() { return val; }
		public static DECMode valueOf(int x) {
			for (DECMode m : DECMode.values())
				if (m.val==x) return m;
			return null;
		}
	}

	//-------------------------------------------------------------------
	public DECSetMode() {
		super(0x68, "DECSM", Level.ANSI);
		this.firstParam='?';
	}

	//-------------------------------------------------------------------
	public DECSetMode(DECMode... modes) {
		this();
		parameter.clear();
		for (DECMode mode : modes)
			parameter.add(mode.val);
	}

	//-------------------------------------------------------------------
	public DECMode getValue() {
		int value = parameter.get(0);
		return DECMode.valueOf(value);
	}

}
