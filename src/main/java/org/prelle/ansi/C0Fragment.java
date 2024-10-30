package org.prelle.ansi;

import java.io.ByteArrayOutputStream;

/**
 *
 */
public class C0Fragment extends AParsedElement {

	private C0Code code;

	//-------------------------------------------------------------------
	public C0Fragment(C0Code code) {
		super.type = Type.C0;
		this.code = code;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "C0("+code+")";
	}

	//-------------------------------------------------------------------
	public C0Code getCode() {
		return code;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.AParsedElement#getName()
	 */
	@Override
	public String getName() {
		return code.name();
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.AParsedElement#encode(java.nio.ByteBuffer, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		toFill.write( (byte)code.code);
	}

}
