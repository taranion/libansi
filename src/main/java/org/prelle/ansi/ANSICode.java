package org.prelle.ansi;

/**
 *
 */
public enum ANSICode {

	NULL(0),
	SOH(1),
	STX(2),
	ETX(3),
	EOT(4),
	ENQ(5),
	ACK(6),
	BEL(7),
	BS(8),
	HT(9),
	LF(10),
	VT(11),
	FF(12),
	CR(13),
	ESC(27)
	;

	public int value;

	ANSICode(int x) {
		this.value=x;
	}
	public int getValue() { return value; }

	public static ANSICode getByValue(int value) {
		for (ANSICode code : ANSICode.values()) {
			if (code.value==value) return code;
		}
		return null;
	}

	//-------------------------------------------------------------------
	public static String ESC(String value) {
		return ((char)ESC.value)+value;
	}
//
//	//-------------------------------------------------------------------
//	public static String CSI(int n, String value) {
//		return ((char)ESC.value)+"["+n+"m"+value;
//	}
//
//	//-------------------------------------------------------------------
//	public static String CSI(int n, int...param) {
//		StringBuffer buf = new StringBuffer(((char)ESC.value)+"["+n);
//		for (int p : param)
//			buf.append(";"+p);
//		return buf+"m";
//	}
//
//	//-------------------------------------------------------------------
//	public static String BG(int color256) {
//		return CSI(48,5,color256);
//	}
//
//	//-------------------------------------------------------------------
//	public static String FG(int color256) {
//		return CSI(38,5,color256);
//	}
//
//	//-------------------------------------------------------------------
//	public static String reset() {
//		return CSI(0);
//	}
}
