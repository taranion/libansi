package org.prelle.ansi;

/**
 *
 */
public enum C0Code {

	/** NUL has no function (ignored by the terminal). */
	NUL(0),
	/** Start of Heading */
	SOH(1),
	/** Start of TEXT */
	SOT(2),
	/** End of Text - to be interpreted as Ctrl-C */
	ETX(3),
	/** End of Transmission */
	EOT(4),
	/** Sends the answerback message. */
	ENQ(5),
	/** ACK is transmitted by a receiver as an affirmative response to the sender. */
	ACK(6),
	/** Sounds the bell tone if the bell is enabled in Keyboard Set-Up. */
	BEL(7),
	/** Moves the cursor one character position to the left. If the cursor is at the left margin, no action occurs. */
	BS(8),
	/** Moves the cursor to the next tab stop. If there are no more tab stops, the cursor moves to the right margin. HT does not cause text to auto wrap. */
	HT(9),
	/** Causes a line feed or a new line operation, depending on the setting of line feed/new line mode. */
	LF(10),
	/** Treated as LF. */
	VT(11),
	/** Treated as LF. */
	FF(12),
	/** Moves the cursor to the left margin on the current line. */
	CR(13),
	/** Shift-Out. Maps the G1 character set into GL. You designate G1 by using a select character set (SCS) sequence. */
	SO(14),
	/** Shift-In. Maps the G0 character set into GL. You designate G0 by using a select character set (SCS) sequence. */
	SI(15),
	DLE(16),
	/** Also known as XON. If XON/XOFF flow control is enabled in Communications Set-Up, DC1 clears DC3 (XOFF). This action causes the VT510 to continue sending characters. */
	DC1(17),
	DC2(18),
	/** Also known as XOFF. If XON/XOFF flow control is enabled in Communications Set-Up, DC3 causes the VT510 to stop sending characters. The terminal cannot resume sending characters until it receives a DC1 control character. */
	DC3(19),
	DC4(20),
	NAK(21),
	SYN(22),
	ETB(23),
	/** Immediately cancels an escape sequence, control sequence, or device control string in progress. In this case, the VT510 does not display any error character.<br/>
	 * CAN is used to indicate that the data preceding it in the data stream
	 * is in error. As a result, this data shall be ignored. The specific
	 * meaning of this control function shall be defined for each application
	 * and/or between sender and recipient.
	 * */
	CAN(24),
	/** End of Medium */
	EM(25),
	/** Immediately cancels an escape sequence, control sequence, or device control string in progress, and displays a reverse question mark as an error character. */
	SUB(26),
	/** Introduces an escape sequence. ESC also cancels any escape sequence, control sequence, or device control string in progress. */
	ESC(27),
	/** File separator */
	FS(28),
	/** Group separator */
	GS(29),
	/** Record Separator */
	RS(30),
	/** Ignored when received, unless a 96- character set is mapped into GL. DEL is not used as a fill character. Digital does not recommend using DEL as a fill character. Use NUL instead. */
	US(31),
	/** Ignored when received, unless a 96-character set is mapped into GL. DEL is not used as a fill character. Digital does not recommend using DEL as a fill character. Use NUL instead. */
	DEL(127)
	;

	int code;

	//-------------------------------------------------------------------
	C0Code(int val) {
		this.code = val;
	}
	public int code() { return code; }

	//-------------------------------------------------------------------
	public static C0Code valueOf(int code) {
		for (C0Code func : C0Code.values()) {
			if (func.code==code)
				return func;
		}
		throw new IllegalArgumentException("Not a valid C0 Bit code: "+code);
	}

}
