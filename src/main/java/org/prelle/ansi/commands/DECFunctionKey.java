package org.prelle.ansi.commands;

import org.prelle.ansi.EscapeSequenceFragment;
import org.prelle.ansi.Level;

/**
 * These sequences cause the line containing the active position to become the
 * top or bottom half of a double-height double-width line. The sequences must
 * be used in pairs on adjacent lines and the same character output must be
 * sent to both lines to form full double-height characters. If the line was
 * single-width single-height, all characters to the right of the center of
 * the screen are lost. The cursor remains over the same character position
 * unless it would be to the right of the right margin, in which case it is
 * moved to the right margin.
 *
 * NOTE: The use of double-width characters reduces the number of characters per line by half.
 */
public class DECFunctionKey extends EscapeSequenceFragment {

	//-------------------------------------------------------------------
	public DECFunctionKey() {
		super(" ", 0x7e, "DECFNK", Level.VT100);

	}

}
