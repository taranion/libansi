package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PrintableFragment extends AParsedElement {

	private List<Character> buffer = new ArrayList<>();

	//-------------------------------------------------------------------
	public PrintableFragment() {
		super.type = Type.PRINTABLE;
	}

	//-------------------------------------------------------------------
	public PrintableFragment add(char b) {
		buffer.add( (Character)b );
		return this;
	}

	//-------------------------------------------------------------------
	public PrintableFragment add(Character b) {
		buffer.add(b );
		return this;
	}

	//-------------------------------------------------------------------
	public byte[] getData() {
//		byte[] buf = new byte[buffer.size()];
//		for (int i=0; i<buf.length; i++) buf[i]=buffer.get(i);
//		return buf;
		return getText().getBytes(StandardCharsets.UTF_8);
	}

	//-------------------------------------------------------------------
	public String getText() {
		char[] buf = new char[buffer.size()];
		for (int i=0; i<buf.length; i++) buf[i]=buffer.get(i);
		return new String(buf);
	}
	public void setText(String data) {
		buffer.clear();
		for (char c: data.toCharArray()) {
			buffer.add(c);
		}
	}

	//-------------------------------------------------------------------
	public boolean isSingleByte() {
		return buffer.size()==1;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "PRINTABLE("+getText()+")";
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.AParsedElement#getName()
	 */
	@Override
	public String getName() {
		return "PRINT";
	}

	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		System.err.println("TODO: PrintableFragment.encode");
	}

}
