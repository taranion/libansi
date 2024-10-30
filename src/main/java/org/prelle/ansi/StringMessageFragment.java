package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class StringMessageFragment extends C1Fragment {

	protected String data;

	//-------------------------------------------------------------------
	public StringMessageFragment(C1Code code, String data) {
		super(code);
		this.data = data;
	}

	//-------------------------------------------------------------------
	public String toString() {
		if (data.length()>50)
			return code+":"+data.substring(0, 50);
		return code+":"+data;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		super.encode(toFill, use7Bit);
		toFill.writeBytes(data.getBytes(StandardCharsets.US_ASCII));
		if (use7Bit) {
			toFill.write((byte) C0Code.ESC.code);
			toFill.write((byte) (C1Code.ST.getAsEscapeCode()));
		} else {
			toFill.write((byte) C1Code.ST.code);
		}
	}

}
