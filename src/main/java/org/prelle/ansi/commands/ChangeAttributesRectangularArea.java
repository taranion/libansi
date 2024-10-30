package org.prelle.ansi.commands;

import java.util.List;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;
import org.prelle.ansi.commands.SelectGraphicRendition.Meaning;

/**
 * This control function erases characters from part or all of the display. When you erase complete lines, they become single-height, single-width lines, with all visual character attributes cleared. ED works inside or outside the scrolling margins.
 */
public class ChangeAttributesRectangularArea extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public ChangeAttributesRectangularArea() {
		super("$", 'r', "DECCARA", Level.VT400);
	}

	//-------------------------------------------------------------------
	public ChangeAttributesRectangularArea(int t, int l, int b, int r, List<Meaning> params) {
		this();
		parameter.clear();
		parameter.add(t);
		parameter.add(l);
		parameter.add(b);
		parameter.add(r);
		params.stream().map(m -> m.getCode()).forEach(m -> parameter.add(m));
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
