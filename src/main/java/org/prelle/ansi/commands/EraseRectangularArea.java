package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function erases characters from part or all of the display. When you erase complete lines, they become single-height, single-width lines, with all visual character attributes cleared. ED works inside or outside the scrolling margins.
 */
public class EraseRectangularArea extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public EraseRectangularArea() {
		super("$",'r', "DECERA", Level.VT400);
	}

	//-------------------------------------------------------------------
	public EraseRectangularArea(int t, int l, int b, int r) {
		this();
		parameter.clear();
		parameter.add(t);
		parameter.add(l);
		parameter.add(b);
		parameter.add(r);
	}

	//-------------------------------------------------------------------
	public int getTop() {
		int value = parameter.get(0);
		return (value==0)?1:value;
	}

	//-------------------------------------------------------------------
	public int getLeft() {
		return parameter.get(1);
	}

	//-------------------------------------------------------------------
	public int getBottom() {
		return parameter.get(2);
	}

	//-------------------------------------------------------------------
	public int getRight() {
		return parameter.get(3);
	}

}
