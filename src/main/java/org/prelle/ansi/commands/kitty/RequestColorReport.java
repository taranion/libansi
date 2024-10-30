package org.prelle.ansi.commands.kitty;

import org.prelle.ansi.C1Code;
import org.prelle.ansi.StringMessageFragment;

/**
 * file:docs/Proprietary%20Escape%20Codes%20-%20Documentation%20-%20iTerm2%20-%20macOS%20Terminal%20Replacement.html
 */
public class RequestColorReport extends StringMessageFragment {

	public final static int WINDOW_AND_ICON = 0;
	public final static int ICON   = 1;
	public final static int WINDOW = 2;

	//-------------------------------------------------------------------
	public RequestColorReport(int what, String title) {
		super(C1Code.OSC, title);
		// TODO Auto-generated constructor stub
	}

}
