package org.prelle.ansi.commands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.C0Code;
import org.prelle.ansi.C1Code;
import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class ModeSequence extends ControlSequenceFragment implements ModeSwitch {

	protected ModeState mode;

	//-------------------------------------------------------------------
	/**
	 */
	public ModeSequence() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @param finalChar
	 * @param name
	 * @param level
	 */
	public ModeSequence(int finalChar, String name, Level level) {
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
	public ModeSequence(String intermediate, int finalChar, String name, Level level) {
		super(intermediate, finalChar, name, level);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ModeState getMode() {
		return mode;
	}

	//-------------------------------------------------------------------
	public static byte[] encode(int modeNumber, ModeState state) {
		String enc = String.valueOf(modeNumber);
		byte[] toCopy = enc.getBytes(StandardCharsets.ISO_8859_1);
		byte[] buf = new byte[4+enc.length()];
		int pos=0;
		buf[pos++]=(byte) C0Code.ESC.code();
		buf[pos++]=(byte) C1Code.CSI.getAsEscapeCode();
		buf[pos++]=(byte) 0x3F;
		System.arraycopy(toCopy, 0, buf, pos, toCopy.length);
		pos+=toCopy.length;
		buf[pos++]=(byte) ((state==ModeState.RESET)?0x6C:0x68);
		return buf;
//		out.write(C1Code.CSI);
//		out.write(0x3F); // ?
//		ControlSequenceFragment.encodeParameter(out, List.of(modeNumber));
//		out.write( (state==ModeState.RESET)?0x6C:0x68 );
	}

//	//-------------------------------------------------------------------
//	protected static void encodeStart(ANSIOutputStream out, ModeSequence value) throws IOException {
//		out.write(C1Code.CSI);
//		out.write(0x3F); // ?
//	}
//
//	protected static void encodeEnd(ANSIOutputStream out, ModeSequence value) throws IOException {
//		out.write( (value.getMode()==ModeState.RESET)?0x6C:0x68 );
//	}

}
