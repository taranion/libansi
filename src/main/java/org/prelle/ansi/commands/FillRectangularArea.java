package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class FillRectangularArea extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public FillRectangularArea() {
		super("$",0x78, "DECFRA", Level.VT200);
	}

	//-------------------------------------------------------------------
	public FillRectangularArea(int value, int top, int left, int bottom, int right) {
		this();
		parameter.clear();
		parameter.add(value);
		parameter.add(top);
		parameter.add(left);
		parameter.add(bottom);
		parameter.add(right);
	}

}
