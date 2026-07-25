package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.prelle.ansi.AParsedElement.Type;
import org.prelle.ansi.commands.AllCommands;

/**
 *
 */
public class ANSIInputStream extends InputStream {

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
	private byte[] blockFragment;
	private int blockOffset;

	private VT500Parser parser;
	
	private List<ANSIInputStreamFilter> filters = new ArrayList<ANSIInputStreamFilter>();
	private InputStream in;

	private boolean disconnected;
	
	//-------------------------------------------------------------------
	public ANSIInputStream(InputStream in) {
		this.in = in;
		if (in==null)
			throw new NullPointerException();
		parser = new VT500Parser(new VT500ParserListener() {
			@Override public void print(byte c) {
//				logger.log(Level.WARNING, "print "+c+"/"+(char)c+"  collect="+collectPrintable+"  enc="+parser.getEncoding());
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
				queue.add(new C1Fragment(c1));
			}
			@Override
			public void execute(C0Code c0) {
				if (collectPrintable) releasePrintable();
				queue.add(new C0Fragment(c0));
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
	public boolean hasFilter(ANSIInputStreamFilter filter) {
		return filters.contains(filter);
	}

	//-------------------------------------------------------------------
	public boolean addFilter(ANSIInputStreamFilter filter) {
		return filters.add(filter);
	}

	//-------------------------------------------------------------------
	public boolean addFilter(int index, ANSIInputStreamFilter filter) {
		filters.add(index, filter);
		return true;
	}

	//-------------------------------------------------------------------
	public boolean removeFilter(ANSIInputStreamFilter filter) {
		return filters.remove(filter);
	}

	//-------------------------------------------------------------------
	/**
	 * returns TRUE if the fragment was handled by a filter and should not be returned to the caller
	 */
	private List<AParsedElement> checkFilters(AParsedElement frag) {
		for (ANSIInputStreamFilter filter : filters) {
			if (filter.handles(frag)) {
				return filter.process(frag);
			}
		}
		return List.of(frag);
	}
	
	@Override
	public int available() throws IOException {
//		logger.log(Level.DEBUG, "AIS.available() called");
		return decomposedFragment.isEmpty()?in.available():decomposedFragment.size();
	}
	
//	@Override
//    public int read(byte[] b, int off, int len) throws IOException {
//		AParsedElement frag = queue.isEmpty()?readFragment():queue.remove(0);
//		byte[] data = (frag.getRaw()!=null)?frag.getRaw():null;
//		if (data==null) {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			frag.encode(baos, true);
//			data = baos.toByteArray();
//		}
//		int lenToCopy = Math.min(len, data.length);
//		System.arraycopy(data, 0, b, off, Math.min(len, data.length));
//    }

	
	//-------------------------------------------------------------------
	@Override
	public int read() throws IOException {
		logger.log(Level.DEBUG, "AIS.read() called");
		while (true) {
			logger.log(Level.DEBUG, "AIS.read() in loop");
			if (!decomposedFragment.isEmpty()) {
				int code = decomposedFragment.remove(0);
				return code;
			} else {
				AParsedElement frag = queue.isEmpty()?readFragment():queue.remove(0);
				byte[] data = (frag.getRaw()!=null)?frag.getRaw():null;
				if (data==null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					frag.encode(baos, true);
					data = baos.toByteArray();
				}
				if (frag instanceof PrintableFragment print) {
					String text = print.getText();
					for (int i=0; i<text.length(); i++) {
						decomposedFragment.add(text.codePointAt(i));					
					}
				} else {
					if (data==null) {
						System.err.println("AIS.read() returned a fragment with null raw data: "+frag.getClass());
						continue;
					}
					for (byte b : data) {
						decomposedFragment.add((b<0)?(256+b):b);
					}
				}
				return decomposedFragment.remove(0);
			}
		}
		
		
//		while (true) {
//			try {
//				if (!decomposedFragment.isEmpty()) {
//					int code = decomposedFragment.remove(0);
//					return code;
//				} else {
//					AParsedElement frag = readFragment();
//					if (frag instanceof PrintableFragment print) {
//						String text = print.getText();
//						for (int i=0; i<text.length(); i++) {
//							decomposedFragment.add(text.codePointAt(i));					
//						}
//						continue;
//					} else {
//						byte[] data = frag.getRaw();
//						if (data==null) {
//							System.err.println("AIS.read() returned a fragment with null raw data: "+frag.getClass());
//							continue;
//						}
//						for (byte b : data) {
//							decomposedFragment.add((b<0)?(256+b):b);
//						}
//					}
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return super.read();
	}

	//-------------------------------------------------------------------
	@Override
	public int read(byte[] buf, int offset, int length) throws IOException {
		logger.log(Level.INFO,"ENTER: AIS.read(byte[],"+offset+","+length+") called");
		int len = 0;
		try {
			AParsedElement frag = readFragment(false);
			if (frag==null) return 0;
			logger.log(Level.ERROR, frag);
			byte[] data = frag.getRaw();
			if (data==null) {
				if (frag.getType()==Type.C0) { buf[0]=(byte)((C0Fragment)frag).getCode().code(); return 1; }
				if (frag.getType()==Type.C1) { buf[0]=(byte)((C1Fragment)frag).getCode().code(); return 1; }
				System.err.println("AIS.read(byte[],int,int) returned a fragment with null raw data: "+frag.getClass());
				len=0;
				return 0;
			}
			len = Math.min(data.length, buf.length-offset);
			if (len<data.length)
				System.err.println("AIS.read(byte[]) returned "+len+" bytes, but buffer is "+buf.length);
			System.arraycopy(data, 0, buf, offset, len);
//			System.err.println("WAS: AIS.read(byte[],"+offset+","+length+") called");
//			logger.log(Level.ERROR, "Converted {0} to string \"{1}\" ", frag, new String(data, 0, len, StandardCharsets.UTF_8));
//			if (frag instanceof ControlSequenceFragment csi) {
//				logger.log(Level.ERROR, "ControlSequenceFragment text: \"{0}\"", csi);
//			}
			
			
			return len;
		} finally {
			logger.log(Level.INFO,"LEAVE: AIS.read(byte[]) = {0}",len);
			if (len==0) {
				try {
					throw new RuntimeException("Trace");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.log(Level.WARNING, "AIS.read(byte[]) returned 0 bytes ");
			}
		}
	}

	//-------------------------------------------------------------------
	public AParsedElement readFragment() throws IOException {
		return readFragment(true);
	}

	//-------------------------------------------------------------------
	public AParsedElement readFragment(boolean blocking) throws IOException {
//		logger.log(Level.INFO, "readFragment(blocking={0}) called - queue is {1}  this= {2}", blocking, queue, this);
		if (disconnected) return null;
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
				// Filter
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
				logger.log(Level.DEBUG, filtered.getFirst());
				return filtered.getFirst();
			} else {
				// Queue was empty
				int code = -1; 
				try {
					code = in.read();
					logger.log(Level.DEBUG, "  returned {0} / {1}", (char)code, code);
				} catch (SocketTimeoutException e) {
//					logger.log(Level.WARNING, "SocketTimeoutException in.read");
					if (!blocking) return null;
					continue;
				} catch (SocketException e) {
					disconnected=true;
					logger.log(Level.WARNING, "SocketException in.read: {0}", e.getMessage());
					throw e;
				}
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
//						logger.log(Level.WARNING, queue.getFirst());
						return queue.remove(0);
					}
					logger.log(Level.WARNING, "collectBuffer.reset() called");
					collectBuffer.reset();
					disconnected=true;
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
			logger.log(Level.DEBUG, filtered.getFirst());
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
