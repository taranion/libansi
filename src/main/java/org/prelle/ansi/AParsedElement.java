package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 *
 */
public abstract class AParsedElement {

	public static enum Type {
		PRINTABLE,
		C0,
		C1,
		/**
		 * ESC, followed by 0..x intermediates 0x20..0x2f and a closing
		 * final character from 0x30 to 0x7e
		 */
		ESCAPE_SEQUENCE,
		/**
		 * CSI, than 0..x parameter 0x30..0x3f, followed by 0..x intermediates 0x20..0x2f and a closing
		 * final character from 0x40 to 0x7e.
		 * If the first character in a parameter string is the ? (3/15) character, it indicates that VT parameters follow. The terminal interprets VT parameters according to ANSI X3.64 and ISO 6429.
		 */
		CONTROL_SEQENCE,
		/**
		 * DCS, than like control sequence, after final character data string
		 * until an ST is received
		 */
		COMMAND,
		/**
		 * NOT ANSI - just for internal use
		 */
		KEYCODE,
	}

	protected Type type;
	protected byte[] rawData;

	//-------------------------------------------------------------------
	public Type getType() {
		return type;
	}

	public AParsedElement setRaw(byte[] raw) {
		this.rawData = raw;
		return this;
	}
	
	//-------------------------------------------------------------------
	public byte[] getRaw() {
		return rawData;
	}
	
	//-------------------------------------------------------------------
	public abstract String getName();

	//-------------------------------------------------------------------
	public abstract void encode(ByteArrayOutputStream toFill, boolean use7Bit);

	//-------------------------------------------------------------------
	public void readExpectedLateBytes(InputStream in) {}

}
