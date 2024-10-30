package org.prelle.ansi.commands.iterm;

import org.prelle.ansi.C1Code;
import org.prelle.ansi.StringMessageFragment;

/**
 * file:///home/prelle/git/libterminal/libansi/docs/Proprietary%20Escape%20Codes%20-%20Documentation%20-%20iTerm2%20-%20macOS%20Terminal%20Replacement.html
 */
public class ITermCommandFragment extends StringMessageFragment {

	public final static int REPORT_COLORS = 4;
	public final static int SET_COLOR = 6;
	public final static int ANCHOR = 8;
	public final static int LEET = 1337;

	//-------------------------------------------------------------------
	public ITermCommandFragment(String data) {
		super(C1Code.OSC, data);
	}

}
