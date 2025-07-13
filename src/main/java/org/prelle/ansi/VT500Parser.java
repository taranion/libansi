package org.prelle.ansi;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @see <a href="https://vt100.net/emu/dec_ansi_parser">https://vt100.net/emu/dec_ansi_parser</a>
 */
public class VT500Parser {

	private final static Logger logger = System.getLogger(ANSIInputStream.class.getPackageName());

	public static enum ParserState {
		CSI_ENTRY,
		CSI_IGNORE,
		CSI_INTERMEDIATE,
		CSI_PARAM,
		DCS_ENTRY,
		DCS_IGNORE,
		DCS_INTERMEDIATE,
		DCS_PARAM,
		DCS_PASSTHROUGH,
		ESCAPE,
		ESCAPE_INTERMEDIATE,
		GROUND,
		OSC_STRING,
		SOS_PM_APC,
	}

	private VT500ParserListener callback;
	private ParserState state;
	
	private Charset encoding = StandardCharsets.UTF_8;

	/**
	 * If this is enabled, C1 control codes from 0x80 to 0x9F are considered
	 * printable codepoints
	 */
	private boolean utf8Mode = true;

	protected StringBuffer intermediate = new StringBuffer();
	protected StringBuffer parameter = new StringBuffer();
	protected StringBuffer oscData = new StringBuffer();
	protected StringBuffer hook = new StringBuffer();
	protected StringBuffer sosPmApc = new StringBuffer();
	protected int sosPmApcCode;
	protected boolean rcvEscapeinSosPmApc;
	protected boolean rcvEscapeinOSC;
	protected boolean rcvEscapeinDCSPassthrough;
	protected int utf8Codepoint;
	protected int utf8Expect;

	//-------------------------------------------------------------------
	public VT500Parser(VT500ParserListener callback) {
		this.callback = callback;
		state = ParserState.GROUND;
	}

	//-------------------------------------------------------------------
	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
		utf8Mode = (encoding==StandardCharsets.UTF_8);
	}

	//-------------------------------------------------------------------
	public Charset getEncoding() {
		return this.encoding;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the utf8Mode
	 */
	public boolean isUtf8Mode() {
		return utf8Mode;
	}

//	//-------------------------------------------------------------------
//	/**
//	 * @param utf8Mode the utf8Mode to set
//	 */
//	public void setUtf8Mode(boolean utf8Mode) {
//		this.utf8Mode = utf8Mode;
//	}

	//-------------------------------------------------------------------
	public void parse(int code) {
		logger.log(Level.DEBUG, "RCV {0} / {1} / {2} in state {3}", Integer.toHexString(code), code, (char)code, state);

		if (utf8Mode && code>=0x80) {
			// Collect UTF printable
			boolean dontContinueProcessing = true;
			if (((code & 0xC0)>>6)==0x02) {
				// UTF-8 continuation
				utf8Expect--;
				logger.log(Level.TRACE, "UTF-8 continuation ... expect {0} more", utf8Expect);
				utf8Codepoint = (utf8Codepoint<<6) | (code & 0x3F);
				if (utf8Expect==0) {
					logger.log(Level.TRACE, "UTF-8 done ... codepoint is {0}", utf8Codepoint);
					if (utf8Codepoint>=0xA0) {
						String foo = Character.toString(utf8Codepoint);
						if (foo.length()!=1) {
							logger.log(Level.TRACE, "Expect 1 character string for codepoint {0} but got {1}", utf8Codepoint, foo.length());
						} else {
//							logger.log(Level.ERROR, "STOP HERE "+foo.charAt(0));
							callback.print( (char)foo.charAt(0));
							//System.exit(1);
						}
						utf8Codepoint=0;
					} else {
						logger.log(Level.TRACE, "UTF-8 codepoint for a C1 code: {0}", utf8Codepoint);
						code = utf8Codepoint;
						dontContinueProcessing=false;
					}
				}
			} else if (((code & 0xE0)>>5)==0x06) {
				// 2 Byte sequence
				logger.log(Level.TRACE, "2 Byte UTF-8 ... expect 1 more");
				utf8Expect=1;
				utf8Codepoint = code & 0x1F;
			} else if (((code & 0xE0)>>4)==0x0E) {
				// 3 byte sequence
				logger.log(Level.TRACE, "3 Byte UTF-8 ... expect 2 more");
				utf8Expect=2;
				utf8Codepoint = code & 0xF;
			} else if (((code & 0xF8)>>3)==0x1E) {
				// 4 byte sequence
				logger.log(Level.TRACE, "4 Byte UTF-8 ... expect 3 more");
				utf8Expect=3;
				utf8Codepoint = code & 0x7;
			} else {
				logger.log(Level.WARNING, "Broken UTF-8 encoding");
			}

			if (dontContinueProcessing) {
				return;
			}
		}

		// Receiving specific codes has priority over by state decisions
		final int codeF = code;
		switch (code) {
		case 0x18: // CAN
		case 0x1A: // SUB
			callback.execute(C0Code.valueOf(code));
			return;
		case 0x1B:
			if (state==ParserState.SOS_PM_APC)
				break;
			if (state==ParserState.DCS_PASSTHROUGH)
				break;
			if (state==ParserState.OSC_STRING)
				break;
			if (state==ParserState.CSI_INTERMEDIATE)
				csiDispatch(-1);
			enterState(ParserState.ESCAPE);
			return;
		case 0x7F:
			callback.execute(C0Code.valueOf(code));
			return;
		case 0x90:
			enterState(ParserState.DCS_ENTRY);
			return;
		case 0x98:
		case 0x9E:
		case 0x9F:
			sosPmApcCode=code;
			enterState(ParserState.SOS_PM_APC);
			return;
		case 0x9B:
			enterState(ParserState.CSI_ENTRY);
			return;
//		case 0x9C:
//			return;
		case 0x9D:
			enterState(ParserState.OSC_STRING);
			return;
		case 0x80: case 0x81: case 0x82: case 0x83: case 0x84: case 0x85: case 0x86: case 0x87:
		case 0x88: case 0x89: case 0x8A: case 0x8B: case 0x8C: case 0x8D: case 0x8E: case 0x8F:
		           case 0x91: case 0x92: case 0x93: case 0x94: case 0x95: case 0x96: case 0x97:
		           case 0x99: case 0x9A:
		   			callback.execute(C1Code.valueOf(code));
		}

		switch (state) {
		case GROUND:
			switch ( (Integer)code) {
			case Integer x when isExecutableC0(codeF) -> callback.execute( C0Code.valueOf(code));
			case Integer x when x>=0x20 && x<=0x7F -> callback.print( (byte)(int)x);
			case Integer x when x>=0xA0 && x<=0xFF -> callback.print( (byte)(int)x);
			default -> ignore(code);
			}
			return;
		case ESCAPE:
			switch ( (Integer)code) {
			case Integer x when isExecutableC0(codeF) -> callback.execute( C0Code.valueOf(code));
			case Integer x when x==0x50 -> enterState(ParserState.DCS_ENTRY);
			case Integer x when x==0x58 -> {sosPmApcCode=code+64; enterState(ParserState.SOS_PM_APC);}
			case Integer x when x==0x5B -> enterState(ParserState.CSI_ENTRY);
			case Integer x when x==0x5D -> enterState(ParserState.OSC_STRING);
			case Integer x when x==0x5E -> {sosPmApcCode=code+64; enterState(ParserState.SOS_PM_APC);}
			case Integer x when x==0x5F -> {sosPmApcCode=code+64; enterState(ParserState.SOS_PM_APC);}
			case Integer x when x==0x7F -> ignore(code);
			case Integer x when executesESCDIspatch(codeF) -> {escDispatch(code); enterState(ParserState.GROUND);}
			case Integer x when x>=0xA0 && x<=0xFF -> callback.print( (byte)(int)x);
			default -> ignore(code);
			}
			return;
		case ESCAPE_INTERMEDIATE:
			switch ( (Integer)code) {
			case Integer x when isExecutableC0(codeF) -> callback.execute( C0Code.valueOf(code));
			case Integer x when x>=0x20 && x<=0x2F -> collect(code);
			case Integer x when x==0x7F -> ignore(code);
			case Integer x when x>=0x30 && x<=0x7E -> { escDispatch(code); enterState(ParserState.GROUND);}
			default -> ignore(code);
			}
			return;
		case CSI_ENTRY:
			switch ( (Integer)code) {
			case Integer x when isExecutableC0(codeF) -> callback.execute( C0Code.valueOf(code));
			case Integer x when x>=0x20 && x<=0x2F -> {collect(code); enterState(ParserState.CSI_INTERMEDIATE);}
			case Integer x when x==0x3A ->  enterState(ParserState.CSI_IGNORE); // Why ignore?
			case Integer x when x>=0x30 && x<=0x3F -> {enterState(ParserState.CSI_PARAM); param(code); }
			case Integer x when x>=0x40 && x<=0x7E -> {csiDispatch(code); enterState(ParserState.GROUND);}
			case Integer x when x==0x7F -> ignore(code);
			case Integer x when x>=0xA0 && x<=0xAF -> {collect(code); enterState(ParserState.CSI_INTERMEDIATE);}
			case Integer x when x>=0xB0 && x<=0xFF -> callback.print( (byte)(int)x);
			default -> ignore(code);
			}
			return;
		case CSI_INTERMEDIATE:
			switch ( (Integer)code) {
			case Integer x when isExecutableC0(codeF) -> callback.execute( C0Code.valueOf(code));
			case Integer x when x>=0x20 && x<=0x2F -> collect(code);
			case Integer x when x>=0x30 && x<=0x3F -> {param(code); enterState(ParserState.CSI_IGNORE);}
			case Integer x when x>=0x40 && x<=0x7E -> {csiDispatch(code); enterState(ParserState.GROUND);}
			case Integer x when x==0x7F -> ignore(code);
			case Integer x when x>=0xA0 && x<=0xAF -> collect(code);
			case Integer x when x>=0xB0 && x<=0xFF -> {csiDispatch(code); enterState(ParserState.GROUND);}
			default -> ignore(code);
			}
			return;
		case CSI_PARAM:
			switch ( (Integer)code) {
			case Integer x when isExecutableC0(codeF) -> callback.execute( C0Code.valueOf(code));
			case Integer x when x>=0x20 && x<=0x2F -> {collect(code); enterState(ParserState.CSI_INTERMEDIATE);}
			case Integer x when x>=0x30 && x<=0x3F -> param(code);
//			case Integer x when x==0x3A ->  enterState(ParserState.CSI_IGNORE);
//			case Integer x when x==0x3B -> param(code);
//			case Integer x when x>=0x3C && x<=0x3F -> enterState(ParserState.CSI_IGNORE);
			case Integer x when x>=0x40 && x<=0x7E -> {csiDispatch( (char)(int)x); enterState(ParserState.GROUND);}
			case Integer x when x==0x7F -> ignore(code);
			// According to
			// https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h3-Control-Bytes_-Characters_-and-Sequences
			// 8 Bit controls can have 8-Bit intermediates
			case Integer x when x>=0xA0 && x<=0xAF -> {collect(code); enterState(ParserState.CSI_INTERMEDIATE);}
			case Integer x when x>=0xB0 && x<=0xB9 -> {param(code); enterState(ParserState.CSI_PARAM);}
			case Integer x when x>=0xC0 && x<=0xFF -> {csiDispatch( (char)(int)x); enterState(ParserState.GROUND);}
			default -> ignore(code);
			}
			return;
		case DCS_ENTRY:
			switch ( (Integer)code) {
			case Integer x when isExecutableC0(codeF) -> ignore(code);
			case Integer x when x>=0x20 && x<=0x2F -> {collect(code); enterState(ParserState.DCS_INTERMEDIATE);}
			case Integer x when x==0x3A ->  enterState(ParserState.CSI_IGNORE); // Why ignore?
			case Integer x when x>=0x30 && x<=0x3F -> {param(code); enterState(ParserState.DCS_PARAM);}
			case Integer x when x>=0x40 && x<=0x7E -> {oscPut(code); enterState(ParserState.DCS_PASSTHROUGH);}
			case Integer x when x==0x7F -> ignore(code);
			case Integer x when x>=0xA0 && x<=0xAF -> {collect(code); enterState(ParserState.DCS_INTERMEDIATE);}
			case Integer x when x>=0xB0 && x<=0xFF -> {param(code); enterState(ParserState.DCS_PARAM);}
			case Integer x when x>=0xC0 && x<=0xFF -> {oscPut(code); enterState(ParserState.DCS_PASSTHROUGH);}
			default -> ignore(code);
			}
			return;
		case DCS_INTERMEDIATE:
			switch ( (Integer)code) {
			case Integer x when isExecutableC0(codeF) -> callback.execute( C0Code.valueOf(code));
			case Integer x when x>=0x20 && x<=0x2F -> collect(code);
			case Integer x when x>=0x30 && x<=0x3F -> {param(code); enterState(ParserState.DCS_IGNORE);}
			case Integer x when x>=0x40 && x<=0x7E -> {hook(code); enterState(ParserState.DCS_PASSTHROUGH);}
			case Integer x when x==0x7F -> ignore(code);
			case Integer x when x>=0xA0 && x<=0xAF -> collect(code);
			case Integer x when x>=0xB0 && x<=0xFF -> {hook(code); enterState(ParserState.DCS_PASSTHROUGH);}
			default -> ignore(code);
			}
			return;
		case DCS_PARAM:
			switch ( (Integer)code) {
			case Integer x when isExecutableC0(codeF) -> callback.execute( C0Code.valueOf(code));
			case Integer x when x>=0x20 && x<=0x2F -> {collect(code); enterState(ParserState.DCS_INTERMEDIATE);}
			case Integer x when x>=0x30 && x<=0x39 -> param(code);
			case Integer x when x==0x3A ->  enterState(ParserState.DCS_IGNORE);
			case Integer x when x==0x3B -> param(code);
			case Integer x when x>=0x3C && x<=0x3F -> enterState(ParserState.DCS_IGNORE);
			case Integer x when x>=0x40 && x<=0x7E -> {hook(code);  enterState(ParserState.DCS_PASSTHROUGH);}
			case Integer x when x==0x7F -> ignore(code);
			// According to
			// https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h3-Control-Bytes_-Characters_-and-Sequences
			// 8 Bit controls can have 8-Bit intermediates
			case Integer x when x>=0xA0 && x<=0xAF -> {collect(code); enterState(ParserState.DCS_INTERMEDIATE);}
			case Integer x when x>=0xB0 && x<=0xB9 -> {param(code); enterState(ParserState.DCS_PARAM);}
			case Integer x when x>=0xC0 && x<=0xFF -> {hook( (char)(int)x); enterState(ParserState.DCS_PASSTHROUGH);}
			default -> ignore(code);
			}
			return;
		case DCS_PASSTHROUGH:
			switch ( (Integer)code) {
			case Integer x when x==0x1B -> rcvEscapeinDCSPassthrough=true;
			case Integer x when x==0x5C -> {rcvEscapeinDCSPassthrough=false; unhook(x); enterState(ParserState.GROUND);}
			case Integer x when x==0x9C -> {unhook(x); enterState(ParserState.GROUND);}
			case Integer x when x>=0xB0 && x<=0xFF -> {hook(code); enterState(ParserState.DCS_PASSTHROUGH);}
			default -> hook(code);
			}
			return;
		case SOS_PM_APC:
			switch ( (Integer)code) {
			case Integer x when x==0x1B -> rcvEscapeinSosPmApc=true;
			case Integer x when x==0x5C -> {rcvEscapeinSosPmApc=false; stringUnhook(); enterState(ParserState.GROUND);}
			case Integer x when x==0x9C -> {stringUnhook(); enterState(ParserState.GROUND);}
			default -> stringHook(code);
			}
			break;
		case OSC_STRING:
			switch ( (Integer)code) {
			case Integer x when x==0x1B -> rcvEscapeinOSC=true;
			case Integer x when isExecutableC0(codeF) -> ignore(code);
			case Integer x when x==0x5C -> {rcvEscapeinOSC=false; oscEnd(); enterState(ParserState.GROUND);}
			case Integer x when x==0x9C -> {oscEnd(); enterState(ParserState.GROUND);}
			default -> oscPut(code);
			}
			break;
		default:
			logger.log(Level.ERROR, "Unhandled state "+state);
		}
	}

	//-------------------------------------------------------------------
	private boolean isExecutableC0(int code) {
		if (code<=0x17) return true;
		if (code==0x19) return true;
		if (code>=0x1C && code<0x20) return true;
		return false;
	}

	//-------------------------------------------------------------------
	private boolean executesESCDIspatch(int code) {
		if (code>=0x30 && code<=0x4F) return true;
		if (code>=0x51 && code<=0x57) return true;
		if (code==0x59) return true;
		if (code==0x5A) return true;
		if (code==0x5C) return true;
		if (code>=0x60 && code<=0x7E) return true;
		return false;
	}

	//-------------------------------------------------------------------
	private void enterState(ParserState newState) {
		exitState();

		switch (newState) {
		case ESCAPE:
		case CSI_ENTRY:
		case DCS_ENTRY:
			clear();
			break;
		case OSC_STRING:
			oscStart();
			break;
		case SOS_PM_APC:
			sosPmApc.delete(0, hook.length());
			break;
		}
		state = newState;
		logger.log(Level.TRACE, "new state {0}",newState);
	}

	//-------------------------------------------------------------------
	private void exitState() {
		switch (state) {
		case OSC_STRING:
			oscEnd();
			break;
//		case SOS_PM_APC:
//			callback.handleStringMessage(C1Code.valueOf(sosPmApcCode), sosPmApc.toString());
		}
	}

	//-------------------------------------------------------------------
	private void clear() {
		intermediate.delete(0, intermediate.length());
		parameter.delete(0, parameter.length());
	}

	//-------------------------------------------------------------------
	private void collect(int code) {
		intermediate.append( (char)code);
	}

	//-------------------------------------------------------------------
	private void param(int code) {
		parameter.append( (char)code);
	}

	//-------------------------------------------------------------------
	private void csiDispatch(int code) {
		callback.controlSequence(code, intermediate.toString(), parameter.toString());
	}

	//-------------------------------------------------------------------
	private void escDispatch(int code) {
		callback.handleEscape(code, intermediate.toString());
	}

	//-------------------------------------------------------------------
	private void ignore(int code) {
		logger.log(Level.DEBUG, "Ignore code {0} in state {1}", code, state);
	}

	//-------------------------------------------------------------------
	private void oscStart() {
		oscData.delete(0, oscData.length());
	}

	//-------------------------------------------------------------------
	private void oscPut(int code) {
		oscData.append( (char)code);
	}

	//-------------------------------------------------------------------
	private void oscEnd() {
		if (oscData.length()>0) callback.handleOperatingSystemCommand(oscData.toString());
		oscData.delete(0, oscData.length());
	}

	//-------------------------------------------------------------------
	private void hook(int code) {
		hook.append( (char)code);
	}

	//-------------------------------------------------------------------
	private void unhook(int code) {
		callback.handleDeviceControlString(code, intermediate.toString(), parameter.toString(), hook.toString());
		hook.delete(0, hook.length());
	}

	//-------------------------------------------------------------------
	private void stringHook(int code) {
		sosPmApc.append( (char)code);
	}

	//-------------------------------------------------------------------
	private void stringUnhook() {
		String foo = sosPmApc.toString();
		sosPmApc.delete(0, sosPmApc.length());
		callback.handleStringMessage(C1Code.valueOf(sosPmApcCode), foo.toString());
	}
}
