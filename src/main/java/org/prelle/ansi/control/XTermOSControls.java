package org.prelle.ansi.control;

import java.io.IOException;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.commands.xterm.XtermTextParameter;

public class XTermOSControls {

	//-------------------------------------------------------------------
	/** DECSWT */
	public static void setWindowTitle(ANSIOutputStream out, String name) throws IOException {
		XtermTextParameter setTitle = new XtermTextParameter(XtermTextParameter.WINDOW_AND_ICON+";"+name);
		out.write(setTitle);
	}

}
