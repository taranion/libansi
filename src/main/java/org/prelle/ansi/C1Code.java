package org.prelle.ansi;

/**
 *
 */
public enum C1Code {

	// Not a C1 code - just for temporary parsing
	ESC(0x1b),

	/** Padding character */
	PAD(0x80),
	/** High Octet Preset */
	HOP(0x81),
	/** Break Permitted Here */
	BPH(0x82),
	NBH(0x83),
	/** Index. Moves the cursor down one line in the same column. If the cursor is at the bottom margin, the page scrolls up */
	IND(0x84),
	/** Moves the cursor to the first position on the next line. If the cursor is at the bottom margin, the page scrolls up. */
	NEL(0x85),
	SSA(0x86),
	ESA(0x87),
	/** Horizontal tab set. Sets a horizontal tab stop at the column where the cursor is */
	HTS(0x88),
	HTJ(0x89),
	VTS(0x8A),
	PLD(0x8B),
	PLU(0x8C),
	/** Reverse index. Moves the cursor up one line in the same column. If the cursor is at the top margin, the page scrolls down. */
	RI (0x8D),
	/** Temporarily maps the G2 character set into GL, for the next graphic character. You designate the G2 set by using a select character set (SCS) sequence */
	SS2(0x8E),
	/** Temporarily maps the G3 character set into GL, for the next graphic character. You designate the G3 set by using a select character set (SCS) sequence  */
	SS3(0x8F),

	/** Device Control String. Introduces a device control string. Used for loading function keys or a soft character set. */
	DCS(0x90),
	PU1(0x91),
	PU2(0x92),
	STS(0x93),
	CCH(0x94),
	MW (0x95),
	SPA(0x96),
	EPA(0x97),
	SOS(0x98),
	SGCI(0x99),
	/** Makes the terminal send its device attributes response to the host (same as an ANSI device attributes (DA) sequence). Programs should use the ANSI DA sequence instead. */
	DECID(0x9A),
	SCI(0x9A),
	ROI(0x9A),
	/** Control Sequence Introducer. Introduces a control sequence. */
	CSI(0x9B),
	/** Ends a device control string. You use ST in combination with DCS */
	ST (0x9C),
	/** Introduces an operating system command. The VT420 ignores all following characters until it receives a SUB, ST, or any other C1 control character. */
	OSC(0x9D),
	/** Introduces a privacy message string. The VT420 ignores all following characters until it receives a SUB, ST, or any other C1 control character. */
	PM (0x9E),
	/** Introduces an application system command. The VT420 ignores all following characters until it receives a SUB, ST, or any other C1 control character. */
	APC(0x9F)
	;

	int code;

	//-------------------------------------------------------------------
	C1Code(int val) {
		this.code = val;
	}

	//-------------------------------------------------------------------
	public int code() {
		return code;
	}
	//-------------------------------------------------------------------
	public int getAsEscapeCode() {
		return code-64;
	}

	//-------------------------------------------------------------------
	public static C1Code valueOf(int code) {
		for (C1Code func : C1Code.values()) {
			if (func.code==code)
				return func;
		}
		throw new IllegalArgumentException("Not a valid C1 code: "+code);
	}

}
