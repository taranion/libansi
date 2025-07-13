package org.prelle.ansi;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.prelle.ansi.commands.AllCommands;

/**
 *
 */
public class ANSIInputStream extends FilterInputStream {

	private final static Logger logger = System.getLogger(ANSIInputStream.class.getPackageName());

	private enum Mode {
		TEXT,
		// Collect more bytes
		UTF_COLLECT,
		ESCAPE,
		STRING_TERMINATED,
		SEQUENCE,
		/** Wait for ST to finish Device Control Sequence */
		DCS_TEXT
	}

	/**
	 * If set to TRUE all printable characters will be collected until a
	 * non-printable fragment is received
	 */
	private boolean collectPrintable = true;
	private transient PrintableFragment collectInto;
	/** Optional. Will receive a fragment mnemonic and a data string */
	private BiConsumer<String,String> loggingListener;

	/** Elements waiting to be returned */
	private List<AParsedElement> queue = new ArrayList<AParsedElement>();

	private VT500Parser parser;

	//-------------------------------------------------------------------
	public ANSIInputStream(InputStream in) {
		super(in);
		if (in==null)
			throw new NullPointerException();
		parser = new VT500Parser(new VT500ParserListener() {
			@Override public void print(byte c) {
//				logger.log(Level.INFO, "print "+c+"  collect="+collectPrintable);
				byte[] foo = new byte[] {c};
				try {
					String foo2 = new String(foo, parser.getEncoding());
					char cc = foo2.charAt(0);
					if (collectPrintable) {
						if (collectInto!=null) collectInto.add(cc); else collectInto=new PrintableFragment().add(cc);
					} else {
						queue.add(new PrintableFragment().add(cc));
					}
				} catch (Exception e) {
					logger.log(Level.INFO, "Error reading "+c,e);
				}
			}
			@Override public void print(char c) {
//				logger.log(Level.INFO, "print "+c+"  collect="+collectPrintable);
				if (collectPrintable) {
					if (collectInto!=null) collectInto.add(c); else collectInto=new PrintableFragment().add(c);
				} else {
					queue.add(new PrintableFragment().add(c));
				}
			}
			@Override public void handleOperatingSystemCommand(String data) {
				queue.add(new StringMessageFragment(C1Code.OSC, data));
			}
			@Override public void handleDeviceControlString(int code, String inter, String param, String data) {
				if (collectPrintable) releasePrintable();
				DeviceControlFragment dcs = AllCommands.parseDeviceControlSequence(code, inter, param, data);
				queue.add(dcs);
 			}
			@Override public void handleEscape(int code, String parameter) {
				try {
					switch (code) {
					case 79: // F1-F4 ... depending on next byte
						int next = in.read();
						queue.add(switch (next) {
						case 80 -> new KeyCodeFragment(0x70, "F1");
						case 81 -> new KeyCodeFragment(0x71, "F2");
						case 82 -> new KeyCodeFragment(0x72, "F3");
						case 83 -> new KeyCodeFragment(0x73, "F4");
						default -> null;
						});
						break;
					default:
						logger.log(Level.WARNING, "ToDo: implement handleEscape: "+code+": "+parameter);
					}
				} catch (IOException e) {
					logger.log(Level.ERROR, "Error reading extended Escape code");
				}
			}
			@Override public void execute(C1Code c1) {
				if (collectPrintable) releasePrintable();
				queue.add(new C1Fragment(c1));
			}
			@Override
			public void execute(C0Code c0) {
				if (collectPrintable) releasePrintable();
				queue.add(new C0Fragment(c0));
			}
			@Override public void controlSequence(int code, String inter, String param) {
				if (collectPrintable) releasePrintable();
				ControlSequenceFragment seq = AllCommands.parseControlSequence(code, inter, param);
				if (seq==null) {
					// Special handling for key codes
					if (code==126) {
						switch (param) {
						case  "5": queue.add(new KeyCodeFragment(0x21, "Page Up")); return;
						case  "5;2": queue.add(new KeyCodeFragment(0x21, "Page Up")); return;
						case  "6": queue.add(new KeyCodeFragment(0x22, "Page Down")); return;
						case  "6;2": queue.add(new KeyCodeFragment(0x22, "Page Down")); return;
						case "15": queue.add(new KeyCodeFragment(0x74, "F5")); return;
						case "17": queue.add(new KeyCodeFragment(0x75, "F6")); return;
						case "18": queue.add(new KeyCodeFragment(0x76, "F7")); return;
						case "19": queue.add(new KeyCodeFragment(0x77, "F8")); return;
						case "20": queue.add(new KeyCodeFragment(0x78, "F9")); return;
						case "21": queue.add(new KeyCodeFragment(0x79, "F10")); return;
						case "23": queue.add(new KeyCodeFragment(0x7A, "F11")); return;
						case "24": queue.add(new KeyCodeFragment(0x7B, "F12")); return;
						}
					}
					logger.log(Level.WARNING, "No control sequence for "+code+"  inter="+inter+"  param="+param);
				} else
					queue.add(seq);
			}
			public void handleStringMessage(C1Code code, String data) {
				logger.log(Level.DEBUG, "You could implement command detection here: {0}: {1}", code.name(), data);
				queue.add(new StringMessageFragment(code, data));
			}
		});
	}

	//-------------------------------------------------------------------
	private void releasePrintable() {
		if (collectInto!=null) {
			queue.add(collectInto);
			collectInto=null;
		}
	}

	//-------------------------------------------------------------------
	public AParsedElement readFragment() throws IOException {
		if (!queue.isEmpty()) {
			if (loggingListener!=null) {
				try {
					loggingListener.accept(queue.get(0).getType().name(), queue.get(0).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return queue.remove(0);
		}

		// If you get here, the queue is empty
		while (queue.isEmpty()) {
			//logger.log(Level.TRACE, "Calling in.read");
			int code = in.read();
			logger.log(Level.TRACE, "Returned from in.read with {0}   (collect: Printable={1} Into={2})",code, collectPrintable, collectInto);
			if (code==-1) {
				logger.log(Level.DEBUG, "Connection lost");
				if (collectInto!=null) {
					releasePrintable();
					if (loggingListener!=null) {
						try {
							loggingListener.accept(queue.get(0).getType().name(), queue.get(0).toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					logger.log(Level.TRACE, "Before removing: {0}",queue);
					return queue.remove(0);
				}
				return null;
			} else if (code==0x1E) {
				logger.log(Level.DEBUG, "Record separator found - flushing collected printables");
				if (collectInto!=null) {
					releasePrintable();
					return queue.remove(0);
				} else
					continue;
			}
			code = (code<0)?(256+code):code;
			parser.parse(code);
		}
		if (!queue.isEmpty()) {
			queue.get(0).readExpectedLateBytes(in);
		}
		if (loggingListener!=null && !queue.isEmpty()) {
			try {
				loggingListener.accept(queue.get(0).getType().name(), queue.get(0).toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.log(Level.DEBUG, "Before removing: "+queue);
		return queue.remove(0);
	}

	//-------------------------------------------------------------------
	/**
	 * @return the collectPrintable
	 */
	public boolean isCollectPrintable() {
		return collectPrintable;
	}

	//-------------------------------------------------------------------
	/**
	 * @param collectPrintable the collectPrintable to set
	 */
	public void setCollectPrintable(boolean collectPrintable) {
		this.collectPrintable = collectPrintable;
	}

	//-------------------------------------------------------------------
	/**
	 * @param loggingListener the loggingListener to set
	 */
	public void setLoggingListener(BiConsumer<String, String> loggingListener) {
		this.loggingListener = loggingListener;
	}

	//-------------------------------------------------------------------
	public void setEncoding(Charset encoding) {
		parser.setEncoding(encoding);
	}

	//-------------------------------------------------------------------
	public Charset getEncoding() {
		return parser.getEncoding();
	}

}
