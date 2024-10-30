package org.prelle.ansi.control;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.C0Code;
import org.prelle.ansi.C1Code;
import org.prelle.ansi.commands.LeftRightMarginMode;
import org.prelle.ansi.commands.LinefeedNewlineMode;
import org.prelle.ansi.commands.ModeState;
import org.prelle.ansi.commands.SetLeftAndRightMargin;

/**
 *
 */
public class DisplayControl {

	protected final static Logger logger = System.getLogger(DisplayControl.class.getPackageName());

	//-------------------------------------------------------------------
	/**
	 * This control function turns local echo on or off. When local echo is on, the
	 * terminal sends keyboard characters to the screen. The host does not have
	 * to send (echo) the characters back to the terminal display. When local
	 * echo is off, the terminal only sends characters to the host. It is up to the
	 * host to echo characters back to the screen.
	 * <p><b>Default</b>: No local echo</p>
	 *
	 *
	 * @param localEcho When the SRM function is set, the terminal sends keyboard
	 *                  characters to the host only. The host can echo the characters
	 *                  back to the screen.<br/>When the SRM function is reset, the
	 *                  terminal sends keyboard characters to the host and to the
	 *                  screen. The host does have to echo characters back to the terminal.
	 */
	public static void setSendReceiveMode(ANSIOutputStream out, boolean localEcho) throws IOException {
		if (localEcho) {
			// l
			out.writeCSI(0x6C, 0x31, 0x32);
		} else {
			// h
			out.writeCSI(0x68, 0x31, 0x32);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * This control function selects a dark or light background on the screen.
	 * <p><b>Default:</b> Dark background</p>
	 * <h3>Notes on DECSCNM</h3>
	 * <p>Screen mode only affects how the data appears on the screen. DECSCNM does not change the data in page memory.</p>
	 *
	 * @param lightOnDark When DECSCNM is set, the screen displays dark characters
	 *        on a light background.<br/>When DECSCNM is reset, the screen displays
	 *        light characters on a dark background
	 */
	public static void setScreenMode(ANSIOutputStream out, boolean reverse) throws IOException {
		if (reverse) {
			// l
			out.writeCSI(0x6C, 0x3F, 0x35);
		} else {
			// h
			out.writeCSI(0x68, 0x3F, 0x35);
		}
	}
	//-------------------------------------------------------------------
	public static void setDECSCNM(ANSIOutputStream out, boolean localEcho) throws IOException {
		setScreenMode(out, localEcho);
	}

	//-------------------------------------------------------------------
	public static void setLinefeedNewlineMode(ANSIOutputStream out, ModeState carriageReturnOnLinefeed) throws IOException {
		logger.log(Level.DEBUG, "setLinefeedMode");
		byte[] data = LinefeedNewlineMode.encode(carriageReturnOnLinefeed);
		out.write(data, "LNM");
	}

	//-------------------------------------------------------------------
	public static void setLeftRightMarginMode(ANSIOutputStream out, ModeState splitScreen) throws IOException {
		logger.log(Level.DEBUG, "setLeftRightMarginMode");
		LeftRightMarginMode pdu = new LeftRightMarginMode(splitScreen);
		out.write(pdu);
//		byte[] data = LeftRightMarginMode.encode(splitScreen);
//		out.write(data, "DECLRMM");
	}
}
