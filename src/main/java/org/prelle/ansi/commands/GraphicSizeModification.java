package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class GraphicSizeModification extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public GraphicSizeModification() {
		super(" ", 0x42, "GSM", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public GraphicSizeModification(int scaleHeight, int scaleWidth) {
		this();
		parameter.clear();
		parameter.add(scaleHeight);
		parameter.add(scaleWidth);
	}

}
