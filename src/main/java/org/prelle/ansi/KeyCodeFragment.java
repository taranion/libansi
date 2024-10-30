package org.prelle.ansi;

import java.io.ByteArrayOutputStream;

/**
 * 
 */
public class KeyCodeFragment extends AParsedElement {
	
	private int keyCode;
	private String name;

	//-------------------------------------------------------------------
	public KeyCodeFragment(int code, String name) {
		this.type = Type.KEYCODE;
		this.keyCode = code;
		this.name    = name;
	}

	//-------------------------------------------------------------------
	public String toString() {
		return (name!=null)?name:"Keycode";
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.AParsedElement#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.AParsedElement#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
	}

	//-------------------------------------------------------------------
	/**
	 * @return the keyCode
	 */
	public int getKeyCode() {
		return keyCode;
	}

}
