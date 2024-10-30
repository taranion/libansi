package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class GraphicSizeSelection extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public GraphicSizeSelection() {
		super(" ", 0x43, "GSS", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public GraphicSizeSelection(int scale) {
		this();
		parameter.clear();
		parameter.add(scale);
	}

}
