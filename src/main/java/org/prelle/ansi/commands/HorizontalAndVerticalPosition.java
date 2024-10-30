package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * This control function works the same as the cursor position (CUP) function.
 * New applications should use CUP instead of HVP. HVP is provided for compatibility with earlier VT products.
 *
 * If Pl or Pc is not selected or selected as 0, then the cursor moves to the first line or column, respectively. Origin mode (DECOM) selects line numbering and the ability to move the cursor into margins.
 */
public class HorizontalAndVerticalPosition extends ControlSequenceFragment {

	//-------------------------------------------------------------------
	public HorizontalAndVerticalPosition() {
		super(0x66, "HVP", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public HorizontalAndVerticalPosition(int line, int column) {
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
