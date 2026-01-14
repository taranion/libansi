package org.prelle.ansi.commands;

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
        super("!", 127, "QRIP", Level.UNKNOWN);
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


}
