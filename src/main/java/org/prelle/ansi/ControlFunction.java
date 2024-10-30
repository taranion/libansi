package org.prelle.ansi;

import org.prelle.ansi.commands.CursorBackward;
import org.prelle.ansi.commands.CursorDown;
import org.prelle.ansi.commands.CursorForward;
import org.prelle.ansi.commands.CursorPosition;
import org.prelle.ansi.commands.CursorUp;
import org.prelle.ansi.commands.EraseInDisplay;
import org.prelle.ansi.commands.SetLeftAndRightMargin;

/**
 *
 */
public enum ControlFunction {

	CUU(0x42, CursorUp.class),
	CUD(0x42, CursorDown.class),
	CUF(0x43, CursorForward.class),
	CUB(0x44, CursorBackward.class),
	CUP(0x48, CursorPosition.class),
	ED (0x4A, EraseInDisplay.class),
	DECSLRM(0x73, SetLeftAndRightMargin.class),
	;
	int finalChar;
	String intermediate;
	Class<? extends AParsedElement> clazz;

	ControlFunction(int finalChar, String intermediate, Class<? extends AParsedElement> clazz) {
		this.finalChar = finalChar;
		this.intermediate = intermediate;
		this.clazz     = clazz;
	}
	ControlFunction(int finalChar, Class<? extends AParsedElement> clazz) {
		this.finalChar = finalChar;
		this.intermediate = null;
		this.clazz     = clazz;
	}

	//-------------------------------------------------------------------
	public int getFinalChar() {
		return finalChar;
	}

	//-------------------------------------------------------------------
	public String getIntermediate() {
		return intermediate;
	}

	//-------------------------------------------------------------------
	public Class<? extends AParsedElement> getClazz() {
		return clazz;
	}

}
