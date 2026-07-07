package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.prelle.ansi.commands.AllCommands;

/**
 * 
 */
public class PassOutANSIOutputStream extends OutputStream {

	private final static Logger logger = System.getLogger(ANSIInputStream.class.getPackageName());
	
	private OutputStream out;

	private boolean collectPrintable = false;
	private transient PrintableFragment collectInto;
	private ByteArrayOutputStream collectBuffer = new ByteArrayOutputStream();
	private VT500Parser parser;

	/** Elements waiting to be returned */
	private List<AParsedElement> queue = new ArrayList<AParsedElement>() {
		@Override
		public boolean add(AParsedElement e) {
			if (e instanceof PrintableFragment print && print.getText().isEmpty()) {
				throw new RuntimeException("Attempt to add empty PrintableFragment to queue");
			}
			return super.add(e);
			
		}
	};
	
	private Consumer<AParsedElement> sendListener;

	//-------------------------------------------------------------------
	/**
	 */
	public PassOutANSIOutputStream(OutputStream out, Consumer<AParsedElement> sendListener) {
		this.out = out;
		this.sendListener = sendListener;
		parser = new VT500Parser(new VT500ParserListener() {
			@Override public void print(byte c) {
				logger.log(Level.TRACE, "print "+c+"/"+(char)c+"  collect="+collectPrintable+"  enc="+parser.getEncoding());
				byte[] foo = new byte[] {c};
				try {
					String foo2 = new String(foo, parser.getEncoding());
					char cc = foo2.charAt(0);
					if (collectPrintable) {
						if (collectInto!=null) {
							collectInto.add(cc);
							collectBuffer.write(c);
							collectInto.setRaw(collectBuffer.toByteArray());
						} else {
							collectInto=new PrintableFragment().add(cc);
							collectBuffer.write(c);
							collectInto.setRaw(collectBuffer.toByteArray());
						};
					} else {
						queue.add(new PrintableFragment().add(cc).setRaw(foo));
					}
				} catch (Exception e) {
					logger.log(Level.INFO, "Error reading "+c,e);
				}
			}
			@Override public void print(int codePoint) {
				char[] chars = Character.toChars(codePoint);
				String s = new String(chars);
				logger.log(Level.TRACE, "print "+s+" / "+codePoint+"  collect="+collectPrintable);
				if (collectPrintable) {
					try {
						collectBuffer.write(s.getBytes(StandardCharsets.UTF_8));
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (collectInto!=null) {
						collectInto.add(codePoint);
					} else {
						collectInto=new PrintableFragment().add(codePoint);
					};
					collectInto.setRaw(collectBuffer.toByteArray());
				} else {
					queue.add(new PrintableFragment().add(codePoint).setRaw(s.getBytes(StandardCharsets.UTF_8)));
				}
			}
			@Override public void handleOperatingSystemCommand(String data, byte[] buf) {
				queue.add(new StringMessageFragment(C1Code.OSC, data).setRaw(buf));
			}
			@Override public void handleDeviceControlString(int code, String inter, String param, String data, byte[] buf) {
				if (collectPrintable) releasePrintable();
				DeviceControlFragment dcs = AllCommands.parseDeviceControlSequence(code, inter, param, data);
				dcs.setRaw(buf);
				queue.add(dcs);
 			}
			@Override public void handleEscape(int code, String parameter, byte[] buf) {
					switch (code) {
					case 55: queue.add(new EscapeSequenceFragment('7', "DECSC", null).setRaw(buf)); break;
					case 56: queue.add(new EscapeSequenceFragment('8', "DECRC", null).setRaw(buf)); break;
//					case 79: // F1-F4 ... depending on next byte
//						int next = in.read();
//						queue.add(switch (next) {
//						case 80 -> new KeyCodeFragment(0x70, "F1").setRaw(buf);
//						case 81 -> new KeyCodeFragment(0x71, "F2").setRaw(buf);
//						case 82 -> new KeyCodeFragment(0x72, "F3").setRaw(buf);
//						case 83 -> new KeyCodeFragment(0x73, "F4").setRaw(buf);
//						default -> null;
//						});
//						break;
					default:
						logger.log(Level.WARNING, "ToDo: implement handleEscape: "+code+": "+parameter);
					}
			}
			@Override public void execute(C1Code c1) {
				if (collectPrintable) releasePrintable();
				queue.add(new C1Fragment(c1).setRaw(new byte[] {(byte)c1.code}));
			}
			@Override
			public void execute(C0Code c0) {
				if (collectPrintable) releasePrintable();
				queue.add(new C0Fragment(c0).setRaw(new byte[] {(byte)c0.code}));
			}
			@Override public void controlSequence(int code, String inter, String param, byte[] buf) {
				if (collectPrintable) releasePrintable();
				ControlSequenceFragment seq = AllCommands.parseControlSequence(code, inter, param);
				if (seq==null) {
					// Special handling for key codes
					if (code==126) {
						switch (param) {
						case  "5": queue.add(new KeyCodeFragment(0x21, "Page Up").setRaw(buf)); return;
						case  "5;2": queue.add(new KeyCodeFragment(0x21, "Page Up").setRaw(buf)); return;
						case  "6": queue.add(new KeyCodeFragment(0x22, "Page Down").setRaw(buf)); return;
						case  "6;2": queue.add(new KeyCodeFragment(0x22, "Page Down").setRaw(buf)); return;
						case "15": queue.add(new KeyCodeFragment(0x74, "F5").setRaw(buf)); return;
						case "17": queue.add(new KeyCodeFragment(0x75, "F6").setRaw(buf)); return;
						case "18": queue.add(new KeyCodeFragment(0x76, "F7").setRaw(buf)); return;
						case "19": queue.add(new KeyCodeFragment(0x77, "F8").setRaw(buf)); return;
						case "20": queue.add(new KeyCodeFragment(0x78, "F9").setRaw(buf)); return;
						case "21": queue.add(new KeyCodeFragment(0x79, "F10").setRaw(buf)); return;
						case "23": queue.add(new KeyCodeFragment(0x7A, "F11").setRaw(buf)); return;
						case "24": queue.add(new KeyCodeFragment(0x7B, "F12").setRaw(buf)); return;
						}
					}
					logger.log(Level.WARNING, "No control sequence for "+code+"  inter="+inter+"  param="+param);
				} else {
					seq.setRaw(buf);
					queue.add(seq);
				}
			}
			@Override public void handleStringMessage(C1Code code, String data, byte[] buf) {
				logger.log(Level.DEBUG, "You could implement command detection here: {0}: {1}", code.name(), data);
				queue.add(new StringMessageFragment(code, data).setRaw(buf));
			}
		});
	}

	//-------------------------------------------------------------------
	public String toString() {
		return "POANSI --> "+out;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		System.err.println("POANSI.write "+b+"/"+Integer.toHexString(b));
		if (b==13) {
			System.err.println("POANSI.write: CR detected, releasing printable");
//			releasePrintable();
		}
		parser.parse((byte)b);
		while (!queue.isEmpty()) {
			AParsedElement e = queue.remove(0);
			System.err.println("POANSI.write: parsed "+e+"  raw="+e.getRaw());
			logger.log(Level.WARNING, "Pass out {0}  raw={1}", e, e.getRaw());
			out.write(e.getRaw());
			if (sendListener!=null) sendListener.accept(e);
		}
		System.err.println("POANSI.write done");
	}

	//-------------------------------------------------------------------
	private void releasePrintable() {
		if (collectInto!=null) {
			queue.add(collectInto);
			collectInto=null;
		}
		collectBuffer.reset();
	}

}
