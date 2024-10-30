package org.prelle.ansi.commands.xterm;

import org.prelle.ansi.C1Code;
import org.prelle.ansi.StringMessageFragment;

/**
 *
 */
public class XtermTextParameter extends StringMessageFragment {

	public final static int WINDOW_AND_ICON = 0;
	public final static int ICON   = 1;
	public final static int WINDOW = 2;
	/**
	 * Set X property on top-level window.  Pt should be in the form "prop=value", or just "prop" to delete the property.
	 */
	public final static int XPROPERTY = 3;
	/**
	 * Ps = 4 ; c ; spec ⇒  Change Color Number c to the color specified by spec.
	 */
	public final static int COLOR = 4;
	public final static int COLOR_SPECIAL = 5;
	public final static int ENABLE_COLOR_SPECIAL = 5;
	public final static int COLOR_FOREGROUND = 10;
	public final static int COLOR_BACKGROUND = 11;
	public final static int COLOR_CURSOR     = 12;
	public final static int COLOR_POINTER    = 13;
	public final static int COLOR_POINTER_BACKGROUND = 14;
	public final static int COLOR_TEKTRONIK_FOREGROUND = 15;
	public final static int COLOR_TEKTRONIC_BACKGROUND = 16;
	public final static int COLOR_HIGHLIGHT_BACKGROUND = 17;
	public final static int COLOR_TEKTRONIC_CURSOR     = 18;
	public final static int COLOR_HIGHLIGHT_FOREGROUND = 19;
	public final static int CURSOR_SHAPE = 22;
	public final static int LOGFILE = 46;
	public final static int FONT    = 50;
	public final static int RESERVED= 51;
	public final static int SELECTION= 52;
	public final static int FEATURES_ALLOWED = 60;
	public final static int FEATURES_DISALLOWED= 61;

	//-------------------------------------------------------------------
	public XtermTextParameter(String data) {
		super(C1Code.OSC, data);
	}

	//-------------------------------------------------------------------
	public XtermTextParameter(int operation, String data) {
		super(C1Code.OSC, operation+";"+data);
	}

}
