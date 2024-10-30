package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * VT100: CPR may be solicited by a DEVICE STATUS REPORT (DSR) or be sent unsolicited.
 * VT500: Send "CSI 6 6/14"
 */
public class CursorPositionReport extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public CursorPositionReport() {
		super(0x52, "CPR", Level.VT100);
	}

	//-------------------------------------------------------------------
	public CursorPositionReport(int line, int column) {
		this();
		parameter.clear();
		parameter.add(line);
		parameter.add(column);
	}

	//-------------------------------------------------------------------
	/**
	 * @return indicates what line the cursor is on.
	 */
	public int getLine() {
		int value = parameter.get(0);
		return (value==0)?1:value;
	}

	//-------------------------------------------------------------------
	/**
	 * @return indicates what column the cursor is at.
	 */
	public int getColumn() {
		int value = parameter.get(1);
		return (value==0)?1:value;
	}

}
