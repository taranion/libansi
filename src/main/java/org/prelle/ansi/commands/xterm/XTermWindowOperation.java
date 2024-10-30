package org.prelle.ansi.commands.xterm;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.DeviceAttributes.OperatingLevel;
import org.prelle.ansi.Level;

/**
 *
 */
public class XTermWindowOperation extends ControlSequenceFragment {

	public static enum Operation {
		DE_ICONIFY(1),
		ICONIFY(2),
		POSITION(3),
		RESIZE_PIXEL(4),
		FOCUS_IN(5),
		FOCUS_OUT(6),
		REFRESH(7),
		RESIZE_CHAR(8),
		MAXIMIZE(9),
		FULLSCREEN(10),
		REPORT_ICON(11),
		REPORT_POSITION(13),
		/**
		 * Report xterm window size in pixels.
         * Normally xterm's window is larger than its text area, since it
         * includes the frame (or decoration) applied by the window
         * manager, as well as the area used by a scroll-bar.
         * Result is CSI  4 ;  height ;  width t
		 */
		REPORT_SIZE_PIXEL(14),
		REPORT_RESOLUTION(15),
		/** Report xterm character cell size in pixels. Result is CSI  6 ;  height ;  width t */
		REPORT_CHARSIZE_PIXEL(16),
		/** Report the size of the text area in characters.  Result is CSI  8 ;  height ;  width t */
		REPORT_SIZE_CHAR(18),
		REPORT_RESOLUTION_CHAR(19),
		REPORT_ICON_LABEL(20),
		REPORT_WINDOW_TITLE(21),
		RESIZE_LINES(24)
		;
		int code;
		Operation(int v) {
			this.code = v;
		}
		static Operation valueOf(int opCode) {
			for (Operation op : Operation.values()) {
				if (op.code==opCode)
					return op;
			}
			return null;
		}
	}

	private Operation variant = null;

	//-------------------------------------------------------------------
	public XTermWindowOperation() {
		super((int)'t', "WINOP", Level.UNKNOWN);
	}

	//-------------------------------------------------------------------
	public XTermWindowOperation(Operation variant, int...params) {
		this();
		this.variant = variant;
		parameter.add(variant.code);
		for (int p : params)
			parameter.add(p);
	}

	//-------------------------------------------------------------------
	public Operation getVariant() {
		return variant;
	}

	//-------------------------------------------------------------------
	/** Called from decode() to allow child classes special treatment
	 */
	protected void decodingHook() {
		int opCode = parameter.getFirst();
		variant = Operation.valueOf(opCode);
	}

}
