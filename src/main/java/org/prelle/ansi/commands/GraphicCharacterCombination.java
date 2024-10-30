package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class GraphicCharacterCombination extends ControlSequenceFragment {

	public static enum Mode {
		NEXT_TWO(0),
		UNTIL_END(1),
		END(2);
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
	public GraphicCharacterCombination() {
		super(" ", 0x5F, "GCC", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public GraphicCharacterCombination(Mode mode) {
		this();
		parameter.clear();
		parameter.add(mode.val);
	}

	//-------------------------------------------------------------------
	public Mode getMode() {
		int value = parameter.get(0);
		return Mode.valueOf(value);
	}

}
