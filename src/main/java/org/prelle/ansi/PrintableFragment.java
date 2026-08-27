package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
	public PrintableFragment(String text) {
		super.type = Type.PRINTABLE;
		for (char c:text.toCharArray()) {
			buffer.add(c);
		}
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
	public PrintableFragment add(int codepoint) {
		for (char c:Character.toChars(codepoint))
			buffer.add(c);
		return this;
	}

	//-------------------------------------------------------------------
	public void clear() {
		buffer.clear();
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
	public boolean isEmpty() {
		return buffer.size()==0;
	}

	//-------------------------------------------------------------------
	public int size() {
		return buffer.size();
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "PRINTABLE|"+buffer.size()+"("+getText()+")";
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.AParsedElement#getName()
	 */
	@Override
	public String getName() {
		return "PRINT";
	}

	//-------------------------------------------------------------------
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		try {
			toFill.write( getText().getBytes(StandardCharsets.UTF_8) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
