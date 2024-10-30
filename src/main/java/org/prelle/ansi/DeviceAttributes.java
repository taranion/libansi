package org.prelle.ansi;

public class DeviceAttributes {

	public static enum OperatingLevel {
		SEND_REPORT(0),
		/**
		 * Bit 0 = Processor option (STP)
		 * Bit 1 = Advanced video option (AVO)
		 * Bit 2 = Graphics option (GO)
		 */
		LEVEL1_VT100_PLAIN(1),
		LEVEL1_VT100(61),
		LEVEL4_VT220(62),
		LEVEL4_VT320(63),
		LEVEL4_VT420c(64),
		LEVEL4_VT520(65),
		;

		int code;
		//-------------------------------------------------------------------
		OperatingLevel(int code) {
			this.code = code;
		}
		//-------------------------------------------------------------------
		public int code() { return code; }

		//-------------------------------------------------------------------
		public static OperatingLevel valueOf(int code) {
			if (code<8) return LEVEL1_VT100_PLAIN;
			for (OperatingLevel p : OperatingLevel.values()) {
				if (p.code==code)
					return p;
			}
			return null;
		}
	}

	/** For secondary device attribute */
	public static enum TerminalType {
		VT100(0),
		VT220(1),
		VT240(2),
		VT330(18),
		VT340(19),
		VT320(24),
		VT420(41),
		VT510(61),
		VT520(64),
		VT525(65),
		;

		int code;
		//-------------------------------------------------------------------
		TerminalType(int code) {
			this.code = code;
		}
		//-------------------------------------------------------------------
		public int code() { return code; }

		//-------------------------------------------------------------------
		public static TerminalType valueOf(int code) {
			for (TerminalType p : TerminalType.values()) {
				if (p.code==code)
					return p;
			}
			return null;
		}
	}


	/**
	 * https://vt100.net/docs/vt420-uu/chapter9.html
	 * https://vt100.net/docs/vt510-rm/DA1.html
	 * https://github.com/microsoft/terminal/issues/14491
	 * https://vt100.net/shuford/terminal/terminal_identification_news.txt
	 */
	public static enum VT220Parameter {
		COLUMNS_132(1),
		PRINTER(2),
		REGIS_GRAPHICS(3),
		SIXEL(4), // Sixel Graphics
		KATAKANA_CHARSET(5),
		SELECTIVE_ERASE(6), // in Printing: Sheet feeder
		SOFT_CHARACTER_SET(7), // Sixel Font
		USER_DEFINED_KEYS(8),
		NATIONAL_REPLACEMENT_CHARSET(9),
		TEXT_RULING_VECTOR(10),
		STATUS_LINE_25(11),
		YUGOSLAVIAN(12), // Hebrew in printing
		LOCAL_EDITING_MODE(13), // VT340
		EIGHT_BIT_ARCHITECTURE(14),
		TECHNICAL_CHARACTERS(15),
		LOCATOR_DEVICE_PORT(16), // VT340
		TERMINAL_STATE_REPORTS(17),
		USER_WINDOWS(18), // Windowing Capability
		TWO_SESSIONS(19),
		HORIZONTAL_SCROLLING(21),
		ANSI_COLOR(22),
		GREEK(23), // Metric line spacing in printing
		TURKISH(24),
		RECTANGULAR_EDITING(28),
		ANSI_TEXT_LOCATOR(29),
		TEXT_MACROS(32),
		ISO_LATIN2_CHARSET(42),
		PCTERM(44),
		SOFT_KEY_MAP(45),
		ASCII_EMULATION(46)
		;

		int code;
		//-------------------------------------------------------------------
		VT220Parameter(int code) {
			this.code = code;
		}
		//-------------------------------------------------------------------
		public int code() { return code; }

		//-------------------------------------------------------------------
		public static VT220Parameter valueOf(int code) {
			for (VT220Parameter p : VT220Parameter.values()) {
				if (p.code==code)
					return p;
			}
			return null;
		}
	}

}
