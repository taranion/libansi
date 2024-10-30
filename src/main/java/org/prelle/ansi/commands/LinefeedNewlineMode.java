package org.prelle.ansi.commands;

import java.io.IOException;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.C1Code;
import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class LinefeedNewlineMode extends ModeSequence {

	//-------------------------------------------------------------------
	/**
	 */
	public LinefeedNewlineMode() {
		super(0x68, "LNM", Level.VT100);
	}

	//-------------------------------------------------------------------
	/**
	 * @param finalChar
	 * @param name
	 * @param level
	 */
	public LinefeedNewlineMode(int finalChar, String name, Level level) {
		super(finalChar, name, level);
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @param intermediate
	 * @param finalChar
	 * @param name
	 * @param level
	 */
	public LinefeedNewlineMode(String intermediate, int finalChar, String name, Level level) {
		super(intermediate, finalChar, name, level);
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	public static byte[] encode(ModeState state) {
		return ModeSequence.encode(0, state);
	}

//	//-------------------------------------------------------------------
//	public static void encode(ANSIOutputStream out, ModeState value) throws IOException {
//		ModeSequence.encode(out, 0, value);
//	}
//
//	//-------------------------------------------------------------------
//	public static void encode(ANSIOutputStream out, LinefeedNewlineMode value) throws IOException {
//		ModeSequence.encode(out, 0, value.getMode());
//	}

}
