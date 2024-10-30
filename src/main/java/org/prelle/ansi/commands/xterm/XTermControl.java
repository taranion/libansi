package org.prelle.ansi.commands.xterm;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.commands.DeviceAttributes;
import org.prelle.ansi.commands.DeviceAttributes.Variant;
import org.prelle.ansi.commands.iterm.SendITermImage;
import org.prelle.ansi.commands.xterm.XtermTextParameter;

public class XTermControl {

	protected final static Logger logger = System.getLogger(XTermControl.class.getPackageName());

	//-------------------------------------------------------------------
	/**
	 * See https://invisible-island.net/xterm/ctlseqs/ctlseqs.html
	 * https://tintin.mudhalla.net/info/xterm/
	 * @param out
	 * @throws IOException
	 */
	public static void reportAll(ANSIOutputStream out) throws IOException {
		logger.log(Level.DEBUG, "requestTermInfo (xterm XTGETTCAP)");
		out.write(new XTermWindowOperation(XTermWindowOperation.Operation.REPORT_POSITION));
		out.write(new XTermWindowOperation(XTermWindowOperation.Operation.REPORT_RESOLUTION));
		out.write(new XTermWindowOperation(XTermWindowOperation.Operation.REPORT_RESOLUTION_CHAR));
		out.write(new XTermWindowOperation(XTermWindowOperation.Operation.REPORT_SIZE_CHAR));
		out.write(new XTermWindowOperation(XTermWindowOperation.Operation.REPORT_SIZE_PIXEL));
	}


}
