package org.prelle.ansi.commands;

import java.util.List;

import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.Level;

/**
 * Invoke the graphic rendition specified by the parameter(s). All following characters transmitted to the VT100 are rendered according to the parameter(s) until the next occurrence of SGR.
 */
public class SelectGraphicRendition extends ControlSequenceFragment {

	public static enum Meaning {
		RESET(0, Level.VT100),
		BOLD_ON(1, Level.VT100),
		FAINT_ON(2, Level.ANSI),
		ITALIC_ON(3, Level.ANSI),
		UNDERLINE_ON(4, Level.VT100),
		BLINKING_ON(5, Level.VT100),
		BLINK_RAPID_ON(6, Level.ANSI),
		NEGATIVE_ON(7, Level.VT100),
		INVISIBLE_ON(8, Level.ANSI),
		CROSSEDOUT_ON(9, Level.ANSI),
		FONT_PRIMARY(10, Level.ANSI),
		FONT_ALT01(11, Level.ANSI),
		FONT_ALT02(12, Level.ANSI),
		FONT_ALT03(13, Level.ANSI),
		FONT_ALT04(14, Level.ANSI),
		FONT_ALT05(15, Level.ANSI),
		FONT_ALT06(16, Level.ANSI),
		FONT_ALT07(17, Level.ANSI),
		FONT_ALT08(18, Level.ANSI),
		FONT_ALT09(19, Level.ANSI),
		FRAKTUR(20, Level.ANSI),
		UNDERLINE_DOUBLE_ON(21, Level.ANSI),
		INTENSITY_OFF(22, Level.VT200),
		STYLE_OFF(23, Level.ANSI),
		UNDERLINE_OFF(24, Level.VT200),
		BLINK_OFF(25, Level.VT200),
		NEGATIVE_OFF(27, Level.VT200),
		INVISIBLE_OFF(28, Level.ANSI),
		CROSSEDOUT_OFF(29, Level.ANSI),
		FOREGROUND_BLACK(30, Level.ANSI),
		FOREGROUND_RED(31, Level.ANSI),
		FOREGROUND_GREEN(32, Level.ANSI),
		FOREGROUND_YELLOW(33, Level.ANSI),
		FOREGROUND_BLUE(34, Level.ANSI),
		FOREGROUND_MAGENTA(35, Level.ANSI),
		FOREGROUND_CYAN(36, Level.ANSI),
		FOREGROUND_WHITE(37, Level.ANSI),
		FOREGROUND_RGB(38, Level.ANSI),
		FOREGROUND_DEFAULT(39, Level.ANSI),
		BACKGROUND_BLACK(40, Level.ANSI),
		BACKGROUND_RED(41, Level.ANSI),
		BACKGROUND_GREEN(42, Level.ANSI),
		BACKGROUND_YELLOW(43, Level.ANSI),
		BACKGROUND_BLUE(44, Level.ANSI),
		BACKGROUND_MAGENTA(45, Level.ANSI),
		BACKGROUND_CYAN(46, Level.ANSI),
		BACKGROUND_WHITE(47, Level.ANSI),
		BACKGROUND_RGB(48, Level.ANSI),
		BACKGROUND_DEFAULT(49, Level.ANSI),
		FRAMED   (51, Level.ANSI),
		ENCIRCLED(52, Level.ANSI),
		OVERLINED(53, Level.ANSI),
		NOT_FRAMED_NOT_ENCIRLED(54, Level.ANSI),
		NOT_OVERLINED(55, Level.ANSI),
		FOREGROUND_BRIGHT_BLACK(90, Level.ANSI),
		FOREGROUND_BRIGHT_RED(91, Level.ANSI),
		FOREGROUND_BRIGHT_GREEN(92, Level.ANSI),
		FOREGROUND_BRIGHT_YELLOW(93, Level.ANSI),
		FOREGROUND_BRIGHT_BLUE(94, Level.ANSI),
		FOREGROUND_BRIGHT_MAGENTA(95, Level.ANSI),
		FOREGROUND_BRIGHT_CYAN(96, Level.ANSI),
		FOREGROUND_BRIGHT_WHITE(97, Level.ANSI),
		BACKGROUND_BRIGHT_BLACK(100, Level.ANSI),
		BACKGROUND_BRIGHT_RED(101, Level.ANSI),
		BACKGROUND_BRIGHT_GREEN(102, Level.ANSI),
		BACKGROUND_BRIGHT_YELLOW(103, Level.ANSI),
		BACKGROUND_BRIGHT_BLUE(104, Level.ANSI),
		BACKGROUND_BRIGHT_MAGENTA(105, Level.ANSI),
		BACKGROUND_BRIGHT_CYAN(106, Level.ANSI),
		BACKGROUND_BRIGHT_WHITE(107, Level.ANSI),
		;
		int val;
		Level level;
		Meaning(int value, Level lvl) {
			val=value;
			level=lvl;
		}
		public int getCode() { return val; }
		public String toTagName() {
			switch (this) {
			case FOREGROUND_CYAN: return "cyan";
			case FOREGROUND_RED : return "red";
			case FOREGROUND_GREEN: return "green";
			case FOREGROUND_YELLOW: return "yellow";
			case RESET: return "reset";
			default:
				return "";
			}
		}
	}

	//-------------------------------------------------------------------
	SelectGraphicRendition() {
		super(0x6D, "SGR", Level.ANSI);
	}

	//-------------------------------------------------------------------
	public SelectGraphicRendition(List<Meaning> params) {
		this();
		this.parameter.clear();
		params.stream().map(m -> m.getCode()).forEach(m -> parameter.add(m));
	}

	//-------------------------------------------------------------------
	public SelectGraphicRendition(Meaning...params) {
		this();
		this.parameter.clear();
		for (Meaning m : params)
			parameter.add(m.getCode());
	}

	//-------------------------------------------------------------------
	public SelectGraphicRendition(int color) {
		this();
		this.parameter.clear();
		parameter.addAll(List.of(Meaning.FOREGROUND_RGB.getCode(), 2,color));
	}

	//-------------------------------------------------------------------
	public SelectGraphicRendition(int r, int g, int b) {
		this();
		this.parameter.clear();
		parameter.addAll(List.of(Meaning.FOREGROUND_RGB.getCode(), 2,r, g, b));
	}

}
