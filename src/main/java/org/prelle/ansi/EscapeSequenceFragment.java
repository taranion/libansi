package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class EscapeSequenceFragment extends C0Fragment {

	protected String intermediate;
	protected int finalChar;
	protected Level level;
	protected String commandName;

	//-------------------------------------------------------------------
	public EscapeSequenceFragment(int finalChar, String name, Level level) {
		super(C0Code.ESC);
		this.type=Type.ESCAPE_SEQUENCE;
		this.finalChar = finalChar;
		this.commandName = name;
		this.level   = level;
	}

	//-------------------------------------------------------------------
	public EscapeSequenceFragment(String intermediate, int finalChar, String name, Level level) {
		super(C0Code.ESC);
		this.type=Type.ESCAPE_SEQUENCE;
		this.intermediate = intermediate;
		this.finalChar = finalChar;
		this.commandName = name;
		this.level   = level;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ESC("+commandName+")";
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		super.encode(toFill, use7Bit);
		if (intermediate!=null)
			toFill.writeBytes(intermediate.getBytes(StandardCharsets.US_ASCII));
		toFill.write( (byte)finalChar);
	}

}
