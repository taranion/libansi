package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Abstract base class representing a parsed element fragment from an ANSI stream.
 */
public abstract class AParsedElement {

	/** Enumeration of fragment types. */
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
	/**
	 * Returns the type of this element.
	 *
	 * @return The element Type
	 */
	public Type getType() {
		return type;
	}

	//-------------------------------------------------------------------
	/**
	 * Sets the raw byte payload associated with this element fragment.
	 *
	 * @param raw Raw byte array to set
	 * @return This AParsedElement instance for chaining
	 */
	public AParsedElement setRaw(byte[] raw) {
		this.rawData = raw;
		return this;
	}
	
	//-------------------------------------------------------------------
	/**
	 * Returns the raw byte payload of this element fragment.
	 *
	 * @return Raw byte array, or null if not set
	 */
	public byte[] getRaw() {
		if (rawData == null) {
			// If rawData is not set, encode the element into a byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			encode(baos, false);
			rawData = baos.toByteArray();
		}
		return rawData;
	}
	
	//-------------------------------------------------------------------
	/**
	 * Returns the human-readable name or mnemonic of this element.
	 *
	 * @return The element name
	 */
	public abstract String getName();

	//-------------------------------------------------------------------
	/**
	 * Encodes this element into the given output stream buffer.
	 *
	 * @param toFill Buffer to write encoded bytes into
	 * @param use7Bit true to encode C1 codes as 7-bit ESC sequences; false for 8-bit
	 */
	public abstract void encode(ByteArrayOutputStream toFill, boolean use7Bit);

	//-------------------------------------------------------------------
	/**
	 * Reads any expected late bytes from the input stream if required by this fragment type.
	 *
	 * @param in InputStream to read additional bytes from
	 */
	public void readExpectedLateBytes(InputStream in) {}

}
