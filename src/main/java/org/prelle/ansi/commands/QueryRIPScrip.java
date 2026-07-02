package org.prelle.ansi.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.C0Code;
import org.prelle.ansi.C1Code;
import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 *
 */
public class QueryRIPScrip extends ControlSequenceFragment {

	public static enum RipState {
	    QUERY(0),
	    OFF(1),
	    ON(2)
	    ;
	    int value;
	    RipState(int val) { this.value = val; }
	    public int getValue() { return value; }
	}

    //-------------------------------------------------------------------
    public QueryRIPScrip() {
        super(0x21, "QRIP", Level.UNKNOWN);
    }

    //-------------------------------------------------------------------
    public QueryRIPScrip(RipState state) {
        this();
	    this.parameter.add(state.value);
    }

    //-------------------------------------------------------------------
	public RipState getValue() {
	    switch (parameter.get(0)) {
	    case 0: return RipState.QUERY;
        case 1: return RipState.OFF;
        case 2: return RipState.ON;
	    }
		return null;
	}

	//-------------------------------------------------------------------
	public static byte[] encode(RipState state) {
		byte[] buf = new byte[4];
		int pos=0;
		buf[pos++]=(byte) C0Code.ESC.code();
		buf[pos++]=(byte) C1Code.CSI.getAsEscapeCode();
		buf[pos++]=(byte) (((int)'0')+state.value);
		buf[pos++]=(byte) '!';
		return buf;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		toFill.write( (byte)C0Code.ESC.code());
		toFill.write( (byte)C1Code.CSI.getAsEscapeCode());
		if (firstParam!=0) {
			toFill.write( (byte)firstParam);
		}
		toFill.write( (byte)'!');
	}


}
