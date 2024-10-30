package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class DECSetMode extends ControlSequenceFragment {

	public static enum DECMode {
		WRAP_AROUND_MODE(7),
		TEXT_CURSOR_ENABLE(25),
		OLD_ALTERNATE_BUFFER(47),
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
	public DECSetMode(DECMode mode) {
		this();
		parameter.clear();
		parameter.add(mode.val);
	}

	//-------------------------------------------------------------------
	public DECMode getValue() {
		int value = parameter.get(0);
		return DECMode.valueOf(value);
	}

}
