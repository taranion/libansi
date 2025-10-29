package org.prelle.ansi.commands;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;
import org.prelle.ansi.commands.EraseInDisplay.Mode;

/**
 * file:docs/vt220-rm/chapter4.html#S4.6
 */
public class SetMode extends ControlSequenceFragment {

	public static enum ANSIMode {
		GATM_GUARDED_AREA_TRANSFER(1),
		/** Keyboard Action mode lets your program lock and unlock the keyboard. When the keyboard is locked, it cannot send codes to the program. To alert the operator, the terminal turns on the Wait indicators and disables the keyclick features whenever the keyboard is locked. */
		KAM_KEYBOARD_ACTION_MODE(2),
		CRM_SHOW_CONTROL_CHARACTER(3),
		IRM_INSERT_REPLACE(4),
		/**
		 *
		 */
		VEM_LINE_EDITING_MODE(7),
		HEM_HORIZONTAL_EDITING(10),
		/**
		 * Send/receive mode turns local echo on or off. When send/receive mode is reset (local echo on), every character sent from the keyboard automatically appears on the screen. Therefore, the host does not have to send (echo) the character back to the terminal display. When send/receive mode is set (local echo off), the terminal only sends characters to the application. The host must echo the characters back to the screen.
		 * Set - Turns off (disables) local echo. When the terminal sends characters to the host, the host must echo characters back to the screen.
		 * Reset - Turns on (enables) local echo. When the terminal sends characters, the characters are automatically sent to the screen.
		 */
		SRM_SEND_RECEIVE_MODE(12),
		/**
		 * Line feed/new line mode selects the control character(s) transmitted to the application by the Return and Enter keys. Enter sends the same code as Return only when the auxiliary keypad is in Keypad numeric mode (DECKPNM).
		 * Line feed/new line also selects the action taken by the terminal when receiving line feed (LF), form feed (FF), or vertical tab (VT) codes. These three codes are always processed identically.
		 * You set and reset line feed/new line mode as follows.
		 * NOTE: For compatibility with Digital software, this mode should always be reset.<br/>
		 * Set   - Causes a received LF, FF, or VT code to move the cursor to the first column of the next line. Return sends both a CR and a LF code.<br/>
		 * Reset - Causes a received LF, FF, or VT code to move the cursor to the next line in the current column. Return sends a CR code only.
		 */
		LNM_LINE_FEED_NEW_LINE(20),
		GRAPHIC_RENDITION_COMBINATION_MODE(21),
		;
		int val;
		ANSIMode(int val) { this.val = val; }
		public int value() { return val; }
		public static ANSIMode valueOf(int x) {
			for (ANSIMode m : ANSIMode.values())
				if (m.val==x) return m;
			return null;
		}
	}

	/**
	 * file:docs/vt220-rm/chapter4.html#S4.6.8
	 * https://vt100.net/emu/dec_private_modes.html
	 */
	public static enum DECMode {
		DECCKM_CURSOR_KEY(1),
		DECANM_ANSI_VT52(2),
		DECCOLM_COLUMN_132(3),
		/** Set: Smooth scrolling */
		DECSCLM_SCROLLING(4),
		/** Set: dark characters on light background, Reset=light characters on dark background */
		DECSCNM_SCREEN(5),
		/**
		 * Set: Selects the home position with line numbers starting at top margin of the user-defined scrolling region. The cursor cannot move out of the scrolling region.
		 * Reset: Selects the home position in the upper-left corner of screen. Line numbers are independent of the scrolling region. Use the CUP sequence to move the cursor out of the scrolling region.
		 */
		DECOM_ORIGIN(6),
		/**
		 * Set: Selects auto wrap. Graphic display characters received when the cursor is at the right margin appear on the next line. The display scrolls up if the cursor is at the end of the scrolling region.
		 * Reset: Turns off auto wrap. Graphic display characters received when the cursor is at the right margin replace previously displayed characters.
		 */
		DECAWM_AUTOWRAP(7),
		DECARM_AUTOREPEAT(8),
		DECPEX_PRINT_EXTENT(9),
		DECTCEM_TEXT_CURSOR_ENABLE(25),
		DECRLM_RIGHT_TO_LEFT_MODE(34),
		DECSDM_SIXEL_DISPLAY(80),
		DECBCSM_NO_CLEAR_SCREEN(95),
		;
		int val;
		DECMode(int val) { this.val = val; }
		public int value() { return val; }
		public static DECMode valueOf(int x) {
			for (DECMode m : DECMode.values())
				if (m.val==x) return m;
			return null;
		}
	}

	//-------------------------------------------------------------------
	public SetMode() {
		super(0x68, "SM", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public SetMode(ANSIMode mode) {
		this();
		this.intermediate=null;
		parameter.clear();
		parameter.add(mode.val);
	}

	//-------------------------------------------------------------------
	public SetMode(DECMode mode) {
		this();
		this.firstParam='?';
		parameter.clear();
		parameter.add(mode.val);
	}

	//-------------------------------------------------------------------
	public ANSIMode getValue() {
		int value = parameter.get(0);
		return ANSIMode.valueOf(value);
	}

}
