package org.prelle.ansi.commands;

import org.prelle.ansi.Level;

/**
 *
 */
public class LeftRightMarginMode extends ModeSequence {

	//-------------------------------------------------------------------
	public LeftRightMarginMode() {
		super(0x68, "DECLRMM", Level.VT400);
	}

	//-------------------------------------------------------------------
	public LeftRightMarginMode(ModeState state) {
		super( (state==ModeState.SET)?0x68:0x6C, "DECLRMM", Level.VT500);
		firstParam='?';
		parameter.clear();
		parameter.add(69);
	}

	//-------------------------------------------------------------------
	/**
	 * @param intermediate
	 * @param finalChar
	 * @param name
	 * @param level
	 */
	public LeftRightMarginMode(String intermediate, int finalChar, String name, Level level) {
		super(intermediate, finalChar, name, level);
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	public static byte[] encode(ModeState state) {
		return ModeSequence.encode(69, state);
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
