package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function sets the left and right margins to define the scrolling region. DECSLRM only works when vertical split screen mode (DECLRMM) is set.<br/>
 * Available in: VT Level 4 mode only<br/>
 * Default: Margins are at the left and right page borders.
 */
public class SetLeftAndRightMargin extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public SetLeftAndRightMargin() {
		super(0x73, "DECSLRM", Level.VT400);
	}

	//-------------------------------------------------------------------
	public SetLeftAndRightMargin(int left, int right) {
		this();
		parameter.clear();
		parameter.add(left);
		parameter.add(right);
	}

	//-------------------------------------------------------------------
	public int getLeftMargin() {
		int value = parameter.get(0);
		return (value==0)?1:value;
	}

	//-------------------------------------------------------------------
	public int getRightMargin() {
		int value = parameter.get(1);
		return (value==0)?80:value;
	}

}
