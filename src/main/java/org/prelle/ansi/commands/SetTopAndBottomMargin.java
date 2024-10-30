package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function sets the top and bottom margins for the current page. You cannot perform scrolling outside the margins.<br/>
 * Default: Margins are at the page limits.
 */
public class SetTopAndBottomMargin extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public SetTopAndBottomMargin() {
		super(0x72, "DECSTBM", Level.VT100);
	}

	//-------------------------------------------------------------------
	public SetTopAndBottomMargin(int top, int bottom) {
		this();
		parameter.clear();
		parameter.add(top);
		parameter.add(bottom);
	}

	//-------------------------------------------------------------------
	public int getTopMargin() {
		int value = parameter.get(0);
		return (value==0)?1:value;
	}

	//-------------------------------------------------------------------
	public int getBottomMargin() {
		int value = parameter.get(1);
		return value; //(value==0)?80:value;
	}

}
