package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.prelle.ansi.commands.AllCommands;

/**
 *
 */
public abstract class AbstractANSIInputStream extends InputStream implements FilteringANSIStream {

	protected final static Logger logger = System.getLogger(AbstractANSIInputStream.class.getPackageName());

	/**
	 * If set to TRUE all printable characters will be collected until a
	 * non-printable fragment is received
	 */
	protected boolean collectPrintable = true;
	private transient PrintableFragment collectInto;
	private ByteArrayOutputStream collectBuffer = new ByteArrayOutputStream();
	/** Optional. Will receive a fragment mnemonic and a data string */
	private BiConsumer<String,String> loggingListener;

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
	private List<Integer> decomposedFragment = new ArrayList<Integer>();

	private VT500Parser parser;
	
	private List<ANSIInputStreamFilter> filters = new ArrayList<ANSIInputStreamFilter>();
	protected InputStream in;
	
	protected boolean filtered = false;

	//-------------------------------------------------------------------
	public AbstractANSIInputStream(InputStream in) {
		this.in = in;
		if (in==null)
			throw new NullPointerException();
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
				try {
					switch (code) {
					case 55: queue.add(new EscapeSequenceFragment('7', "DECSC", null).setRaw(buf)); break;
					case 56: queue.add(new EscapeSequenceFragment('8', "DECRC", null).setRaw(buf)); break;
					case 79: // F1-F4 ... depending on next byte
						int next = in.read();
						queue.add(switch (next) {
						case 80 -> new KeyCodeFragment(0x70, "F1").setRaw(buf);
						case 81 -> new KeyCodeFragment(0x71, "F2").setRaw(buf);
						case 82 -> new KeyCodeFragment(0x72, "F3").setRaw(buf);
						case 83 -> new KeyCodeFragment(0x73, "F4").setRaw(buf);
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
		return "ANSIInput <-- "+in;
	}

	//-------------------------------------------------------------------
	private void releasePrintable() {
		if (collectInto!=null) {
			queue.add(collectInto);
			collectInto=null;
		}
		collectBuffer.reset();
	}

	//-------------------------------------------------------------------
	public void addFilter(ANSIInputStreamFilter filter) {
		filters.add(filter);
	}

	//-------------------------------------------------------------------
	/**
	 * returns TRUE if the fragment was handled by a filter and should not be returned to the caller
	 */
	private List<AParsedElement> checkFilters(AParsedElement frag) {
		for (ANSIInputStreamFilter filter : filters) {
			if (filter.handles(frag)) {
				filtered = true;
				return filter.process(frag);
			}
		}
		return List.of(frag);
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		logger.log(Level.WARNING, "ENTER: AIS.available() called = decomposed="+decomposedFragment.size()+"  in="+in);
		int ret = 0;
		try {
			ret = decomposedFragment.isEmpty()?in.available():decomposedFragment.size();
			return ret;
		} finally {
			logger.log(Level.WARNING, "LEAVE: AIS.available() with "+ret);
		}
	}

	//-------------------------------------------------------------------
	protected AParsedElement readFragment() throws IOException {
		filtered = false;
		do {
			if (!queue.isEmpty()) {
				AParsedElement frag = queue.remove(0);
				if (loggingListener!=null) {
					try {
						loggingListener.accept(frag.getType().name(), frag.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// Consume or replace the fragment with the result of a filter
				List<AParsedElement> replacedOrConsumed = checkFilters(frag);
				if (replacedOrConsumed.isEmpty()) {
					// The fragment was consumed by a filter and should not be returned to the caller. Continue reading
					logger.log(Level.DEBUG, "Fragment {0} was handled by a filter", frag);
					continue;
				}
				collectBuffer.reset();
				// If there are multiple fragments returned by the filter, add them to the queue and return the first one
				if (replacedOrConsumed.size()>1) {
					logger.log(Level.DEBUG, "Fragment {0} was split into {1} fragments by a filter", frag, replacedOrConsumed.size());
					queue.addAll(replacedOrConsumed.subList(1, replacedOrConsumed.size()));
				}
				logger.log(Level.INFO, replacedOrConsumed.getFirst());
				return replacedOrConsumed.getFirst();
			} else {
				int code = -1; 
//				try {
//					logger.log(Level.WARNING, "Calling in.read on {0}", in);
					code = in.read();
//					logger.log(Level.WARNING, "Called in.read = {0} / {1}", (char)code, code);
//				} catch (SocketTimeoutException e) {
//					logger.log(Level.TRACE, "SocketTimeoutException in.read");
//					continue;
//				}
//				logger.log(Level.WARNING, "Returned from in.read with {0}   (collect: Printable={1} Into={2})",code, collectPrintable, collectInto);
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
						logger.log(Level.WARNING, queue.getFirst());
						return queue.remove(0);
					}
					logger.log(Level.WARNING, "collectBuffer.reset() called");
					collectBuffer.reset();
					return null;
				} else if (code==0x1E) {
					logger.log(Level.DEBUG, "Record separator found - flushing collected printables");
					if (collectInto!=null) {
						releasePrintable();
						//logger.log(Level.WARNING, queue.getFirst());
						return queue.remove(0);
					} else
						continue;
				}
				code = (code<0)?(256+code):code;
				parser.parse(code);
			}
			if (!queue.isEmpty()) {
				queue.get(0).readExpectedLateBytes(in);
			} else {
//				collectInto.readExpectedLateBytes(in);
				continue;
			} 
			logger.log(Level.DEBUG, queue.get(0));
			if (loggingListener!=null && !queue.isEmpty()) {
				try {
					loggingListener.accept(queue.get(0).getType().name(), queue.get(0).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (queue.isEmpty()) continue;
						
			AParsedElement frag = queue.remove(0);
			List<AParsedElement> filtered = checkFilters(frag);
			if (filtered.isEmpty()) {
				logger.log(Level.DEBUG, "Fragment {0} was handled by a filter", frag);
				continue;
			}

			collectBuffer.reset();
			if (filtered.size()>1) {
				logger.log(Level.DEBUG, "Fragment {0} was split into {1} fragments by a filter", frag, filtered.size());
				queue.addAll(filtered.subList(1, filtered.size()));
			}
//			logger.log(Level.INFO, filtered.getFirst());
			return filtered.getFirst();
		} while (true);
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

	public void releaseBuffer() {
		logger.log(Level.DEBUG, "releaseBuffer() ");
		if (collectInto!=null) {
			collectInto.rawData = collectBuffer.toByteArray();
			queue.add(collectInto);
		}
//		PrintableFragment frag = (collectInto!=null)?collectInto:new PrintableFragment();
//		frag.rawData = collectBuffer.toByteArray();
//		queue.add(frag);
		collectInto=null;
		collectBuffer.reset();
	}

}
