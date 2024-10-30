package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function copies a rectangular area of characters from one section to another in page memory
 */
public class DECCopyRectangularArea extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public DECCopyRectangularArea() {
		super("$", 'v', "DECCRA", Level.VT400);
	}

	//-------------------------------------------------------------------
	public DECCopyRectangularArea(int topSrc, int leftSrc, int botSrc, int rghtSrc, int pageSrc, int topDest, int leftDest, int pageDest) {
		this();
		parameter.clear();
		parameter.add(topSrc);
		parameter.add(leftSrc);
		parameter.add(botSrc);
		parameter.add(rghtSrc);
		parameter.add(pageSrc);
		parameter.add(topDest);
		parameter.add(leftDest);
		parameter.add(pageDest);
	}

	//-------------------------------------------------------------------
	public int getTop() {
		int value = parameter.get(0);
		return (value==0)?1:value;
	}

}
