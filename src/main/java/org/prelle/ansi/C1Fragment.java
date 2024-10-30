package org.prelle.ansi;

import java.io.ByteArrayOutputStream;

/**
 *
 */
public class C1Fragment extends AParsedElement {

	protected C1Code code;

	//-------------------------------------------------------------------
	public C1Fragment(C1Code code) {
		super.type = Type.C1;
		this.code = code;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "C1("+code+")";
	}

	//-------------------------------------------------------------------
	public C1Code getCode() {
		return code;
	}

	//-------------------------------------------------------------------
	public String getName() {
		return code.name();
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.AParsedElement#encode(java.nio.ByteBuffer, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		if (use7Bit) {
			toFill.write((byte) C0Code.ESC.code);
			toFill.write((byte) (code.getAsEscapeCode()));
		} else {
			toFill.write((byte) code.code);
		}

	}

}
