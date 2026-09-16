package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.prelle.ansi.commands.AllCommands;

/**
 * 
 */
public class DefaultVT500Listener implements VT500ParserListener {

	private final static Logger logger = System.getLogger(ANSIInputStream.class.getPackageName());

	private boolean collectPrintable = true;
	private PrintableFragment collectInto;
	private final ByteArrayOutputStream collectBuffer = new ByteArrayOutputStream();

	private Charset encoding = Charset.defaultCharset();
	
	private List<AParsedElement> fragments = new ArrayList<>();
	
	//-------------------------------------------------------------------
	public DefaultVT500Listener(Charset encoding) {
		this.encoding = encoding;
	}
	
	//-------------------------------------------------------------------
	public void setCollectPrintable(boolean collectPrintable) {
		this.collectPrintable = collectPrintable;
	}
	
	//-------------------------------------------------------------------
	private void enqueueFragment(AParsedElement element) {
		fragments.add(element);
	}
	
	//-------------------------------------------------------------------
	public List<AParsedElement> consumeFragments() {
		List<AParsedElement> result = new ArrayList<>(fragments);
		fragments.clear();
		return result;
	}

	//-------------------------------------------------------------------
	public void releaseCollectPrintable() {
		if (collectInto != null && !collectInto.isEmpty()) {
			collectInto.setRaw(collectBuffer.toByteArray());
			AParsedElement frag = collectInto;
			collectInto = null;
			collectBuffer.reset();
			enqueueFragment(frag);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.VT500ParserListener#print(byte)
	 */
	@Override public void print(byte c) {
		byte[] raw = new byte[] {c};
		try {
			String str = new String(raw, encoding);
			char ch = str.charAt(0);
			if (collectPrintable) {
				if (collectInto == null) {
					collectInto = new PrintableFragment();
				}
				collectInto.add(ch);
				collectBuffer.write(c);
			} else {
				enqueueFragment(new PrintableFragment().add(ch).setRaw(raw));
			}
		} catch (Exception e) {
			logger.log(Level.INFO, "Error reading byte character " + c, e);
		}
	}

	@Override public void print(int codePoint) {
		char[] chars = Character.toChars(codePoint);
		String str = new String(chars);
		byte[] raw = str.getBytes(StandardCharsets.UTF_8);
		if (collectPrintable) {
			if (collectInto == null) {
				collectInto = new PrintableFragment();
			}
			collectInto.add(codePoint);
			collectBuffer.writeBytes(raw);
		} else {
			enqueueFragment(new PrintableFragment().add(codePoint).setRaw(raw));
		}
	}

	@Override public void handleOperatingSystemCommand(String data, byte[] buf) {
		releaseCollectPrintable();
		enqueueFragment(AllCommands.parseStringMessage(C1Code.OSC, data).setRaw(buf));
	}

	@Override public void handleDeviceControlString(int code, String inter, String param, String data, byte[] buf) {
		releaseCollectPrintable();
		DeviceControlFragment dcs = AllCommands.parseDeviceControlSequence(code, inter, param, data);
		if (dcs != null) {
			dcs.setRaw(buf);
		}
		enqueueFragment(dcs);
	}

	@Override public void handleEscape(int code, String parameter, byte[] buf) {
		releaseCollectPrintable();
		try {
			switch (code) {
			case 55: enqueueFragment(new EscapeSequenceFragment('7', "DECSC", null).setRaw(buf)); break;
			case 56: enqueueFragment(new EscapeSequenceFragment('8', "DECRC", null).setRaw(buf)); break;
//			case 79:
//				int next = in.read();
//				enqueueFragment(switch (next) {
//				case 80 -> new KeyCodeFragment(0x70, "F1").setRaw(buf);
//				case 81 -> new KeyCodeFragment(0x71, "F2").setRaw(buf);
//				case 82 -> new KeyCodeFragment(0x72, "F3").setRaw(buf);
//				case 83 -> new KeyCodeFragment(0x73, "F4").setRaw(buf);
//				default -> null;
//				});
//				break;
			default:
				logger.log(Level.WARNING, "Unhandled Escape code: " + code + ": " + parameter);
			}
		} catch (Exception e) {
			logger.log(Level.ERROR, "Error reading extended Escape code", e);
		}
	}

	@Override public void execute(C1Code c1) {
		releaseCollectPrintable();
		enqueueFragment(new C1Fragment(c1).setRaw(new byte[] {(byte)c1.code}));
	}

	@Override public void execute(C0Code c0) {
		releaseCollectPrintable();
		enqueueFragment(new C0Fragment(c0).setRaw(new byte[] {(byte)c0.code}));
	}

	@Override public void controlSequence(int code, String inter, String param, byte[] buf) {
		releaseCollectPrintable();
		ControlSequenceFragment seq = AllCommands.parseControlSequence(code, inter, param);
		if (seq == null) {
			if (code == 126) {
				switch (param) {
				case "5": enqueueFragment(new KeyCodeFragment(0x21, "Page Up").setRaw(buf)); return;
				case "5;2": enqueueFragment(new KeyCodeFragment(0x21, "Page Up").setRaw(buf)); return;
				case "6": enqueueFragment(new KeyCodeFragment(0x22, "Page Down").setRaw(buf)); return;
				case "6;2": enqueueFragment(new KeyCodeFragment(0x22, "Page Down").setRaw(buf)); return;
				case "15": enqueueFragment(new KeyCodeFragment(0x74, "F5").setRaw(buf)); return;
				case "17": enqueueFragment(new KeyCodeFragment(0x75, "F6").setRaw(buf)); return;
				case "18": enqueueFragment(new KeyCodeFragment(0x76, "F7").setRaw(buf)); return;
				case "19": enqueueFragment(new KeyCodeFragment(0x77, "F8").setRaw(buf)); return;
				case "20": enqueueFragment(new KeyCodeFragment(0x78, "F9").setRaw(buf)); return;
				case "21": enqueueFragment(new KeyCodeFragment(0x79, "F10").setRaw(buf)); return;
				case "23": enqueueFragment(new KeyCodeFragment(0x7A, "F11").setRaw(buf)); return;
				case "24": enqueueFragment(new KeyCodeFragment(0x7B, "F12").setRaw(buf)); return;
				}
			}
			logger.log(Level.WARNING, "No control sequence for " + code + " inter=" + inter + " param=" + param);
		} else {
			seq.setRaw(buf);
			enqueueFragment(seq);
		}
	}

	@Override public void handleStringMessage(C1Code code, String data, byte[] buf) {
		releaseCollectPrintable();
		enqueueFragment(AllCommands.parseStringMessage(code, data).setRaw(buf));
	}

	@Override
	public void handleTwoByteEscape(int first, int second, byte[] buf) {
		// TODO Auto-generated method stub
		switch (first) {
		case 0x4f -> {
			switch (second) {
			case 0x50: enqueueFragment(new KeyCodeFragment(0x70, "F1").setRaw(buf)); break;
			case 0x51: enqueueFragment(new KeyCodeFragment(0x71, "F2").setRaw(buf)); break;
			case 0x52: enqueueFragment(new KeyCodeFragment(0x72, "F3").setRaw(buf)); break;
			case 0x53: enqueueFragment(new KeyCodeFragment(0x73, "F4").setRaw(buf)); break;
			default:
				logger.log(Level.WARNING, "Unhandled two-byte escape sequence: " + first + ", " + second);
			}
		}
		default -> {
			logger.log(Level.WARNING, "Unhandled two-byte escape sequence: " + first + ", " + second);
		}
		}
	}

}
