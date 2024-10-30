package org.prelle.ansi.control;

import static org.prelle.ansi.ANSICode.ESC;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.EscapeSequenceFragment;
import org.prelle.ansi.commands.CursorBackward;
import org.prelle.ansi.commands.CursorDown;
import org.prelle.ansi.commands.CursorForward;
import org.prelle.ansi.commands.CursorPosition;
import org.prelle.ansi.commands.CursorUp;
import org.prelle.ansi.commands.DECResetMode;
import org.prelle.ansi.commands.DECSetMode;
import org.prelle.ansi.commands.DECSetMode.DECMode;

/**
 *
 */
public class CursorControls {

	private static void write(OutputStream out, String value) throws IOException {
		out.write(value.getBytes(Charset.defaultCharset()));
	}

	//-------------------------------------------------------------------
	public static void moveCursorHorizontal(ANSIOutputStream out, int value) throws IOException {
		// CSI Pn G
		out.write(new CursorForward(value));
	}

	//-------------------------------------------------------------------
	public static void home(OutputStream out) throws IOException {
		write(out,(((char)ESC.value)+"[H"));
	}

	//-------------------------------------------------------------------
	public static void setCursorPosition(ANSIOutputStream out, int x, int y) throws IOException {
//		out.writeCSI(ControlFunction.CUP.finalChar, y,x);
		out.write(new CursorPosition(x, y));
//		write(out,((char)ESC.value)+"["+y+";"+x+"H");
	}

	//-------------------------------------------------------------------
	/**
	 * CUF causes the active presentation position to be moved rightwards in
	 * the presentation component by n character positions if the character
	 * path is horizontal, or by n line positions if the character path is
	 * vertical, where n equals the value of Pn
	 */
	public static void moveCursorRight(ANSIOutputStream out, int value) throws IOException {
		//out.writeCSI(ControlFunction.CUF, value);
		out.writeCSI(new CursorForward(value));
	}

	//-------------------------------------------------------------------
	/**
	 * CUB causes the active presentation position to be moved leftwards in
	 * the presentation component by n character positions if the character
	 * path is horizontal, or by n line positions if the character path is
	 * vertical, where n equals the value of Pn.
	 */
	public static void moveCursorLeft(ANSIOutputStream out, int value) throws IOException {
		//out.writeCSI(ControlFunction.CUB, value);
		out.writeCSI(new CursorBackward(value));
	}

	//-------------------------------------------------------------------
	/**
	 * CNL causes the active presentation position to be moved to the first
	 * character position of the n-th following line in the presentation
	 * component, where n equals the value of Pn.
	 */
	public static void nextLine(OutputStream out, int value) throws IOException {
		write(out,((char)ESC.value)+"["+value+"E");
	}

	//-------------------------------------------------------------------
	/**
	 * Move to beginning of previous line, {value} lines up
	 */
	public static void previousLine(OutputStream out, int value) throws IOException {
		write(out,((char)ESC.value)+"["+value+"F");
	}

	//-------------------------------------------------------------------
	public static void setColumn(OutputStream out, int value) throws IOException {
		write(out,((char)ESC.value)+"["+value+"G");
	}

	//-------------------------------------------------------------------
	public static void requestCursorPosition(OutputStream out) throws IOException {
		write(out,((char)ESC.value)+"[6n");
	}

	//-------------------------------------------------------------------
	public static void moveCursorUp(ANSIOutputStream out, int value) throws IOException {
		//out.writeCSI(ControlFunction.CUU, value);
		out.writeCSI(new CursorUp(value));
	}

	//-------------------------------------------------------------------
	public static void moveCursorDown(ANSIOutputStream out, int value) throws IOException {
		//out.writeCSI(ControlFunction.CUD, value);
		out.writeCSI(new CursorDown(value));
	}

	//-------------------------------------------------------------------
	public static void savePositionDEC(ANSIOutputStream out) throws IOException {
		out.write(new EscapeSequenceFragment('7', "DECSC", null));
//		write(out,((char)ESC.value)+"7");
	}

	//-------------------------------------------------------------------
	public static void restorePositionDEC(ANSIOutputStream out) throws IOException {
		out.write(new EscapeSequenceFragment('8', "DECRC", null));
		//write(out,((char)ESC.value)+"8");
	}

	//-------------------------------------------------------------------
	public static void savePositionSCO(OutputStream out) throws IOException {
		write(out,((char)ESC.value)+"[s");
	}

	//-------------------------------------------------------------------
	public static void restorePositionSCO(OutputStream out) throws IOException {
		write(out,((char)ESC.value)+"[u");
	}

	//-------------------------------------------------------------------
	public static void scrollRegion(OutputStream out, int y1, int y2) throws IOException {
		write(out,((char)ESC.value)+"["+y1+";"+y2+"r");
		//write(out,((char)ESC.value)+"[?6h");
	}

//	//-------------------------------------------------------------------
//	public static void setOriginMode(ANSIOutputStream out, boolean on) throws IOException {
//		if (on) {
//			// CSI ? 6 h
//			out.writeSGR(0x68, 0x3f, 0x36);
//		} else {
//			// CSI ? 6 l
//			out.writeSGR(0x6C, 0x3f, 0x36);
//		}
//	}

	//-------------------------------------------------------------------
	public static void enableCursor(ANSIOutputStream out, boolean set) throws IOException {
		if (set)
			out.write(new DECSetMode(DECMode.TEXT_CURSOR_ENABLE));
		else
			out.write(new DECResetMode(DECMode.TEXT_CURSOR_ENABLE));
	}

}
