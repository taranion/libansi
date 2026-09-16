package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.prelle.ansi.commands.AllCommands;

/**
 * An ANSI Input Stream implementation that parses VT500 sequences,
 * supports filtering, fragment-level reading, and configurable printable collection.
 */
public class ANSIInputStream extends InputStream implements FilteringANSIStream {

	private final static Logger logger = System.getLogger(ANSIInputStream.class.getPackageName());

	private final InputStream in;
	private final VT500Parser parser;
	private boolean collectPrintable = true;

	private PrintableFragment collectInto;
	private final ByteArrayOutputStream collectBuffer = new ByteArrayOutputStream();

	private final List<ANSIInputStreamFilter> filters = new ArrayList<>();
	private final List<AParsedElement> queue = new ArrayList<>();

	private byte[] pendingRaw;
	private int pendingOffset;
	/** Optional. Will receive a fragment mnemonic and a data string */
	private BiConsumer<String,String> loggingListener;

	//-------------------------------------------------------------------
	/**
	 * Creates a new ANSIInputStream wrapping the specified underlying input stream.
	 *
	 * @param in The underlying InputStream source
	 */
	public ANSIInputStream(InputStream in) {
		this.in = Objects.requireNonNull(in, "InputStream cannot be null");
		this.parser = new VT500Parser(new VT500ParserListener() {
			@Override public void print(byte c) {
				byte[] raw = new byte[] {c};
				try {
					String str = new String(raw, parser.getEncoding());
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
					case 79:
						int next = in.read();
						enqueueFragment(switch (next) {
						case 80 -> new KeyCodeFragment(0x70, "F1").setRaw(buf);
						case 81 -> new KeyCodeFragment(0x71, "F2").setRaw(buf);
						case 82 -> new KeyCodeFragment(0x72, "F3").setRaw(buf);
						case 83 -> new KeyCodeFragment(0x73, "F4").setRaw(buf);
						default -> null;
						});
						break;
					default:
						logger.log(Level.WARNING, "Unhandled Escape code: " + code + ": " + parameter);
					}
				} catch (IOException e) {
					logger.log(Level.ERROR, "Error reading extended Escape code", e);
				}
			}

			@Override
			public void handleTwoByteEscape(int first, int second, byte[] buf) {
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
		});
	}
	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	/**
	 * Sets the listener for logging parsed fragments.
	 *
	 * @param loggingListener the loggingListener to set
	 */
	public void setLoggingListener(BiConsumer<String, String> loggingListener) {
		this.loggingListener = loggingListener;
	}

	//-------------------------------------------------------------------
	private void releaseCollectPrintable() {
		if (collectInto != null && !collectInto.isEmpty()) {
			collectInto.setRaw(collectBuffer.toByteArray());
			AParsedElement frag = collectInto;
			collectInto = null;
			collectBuffer.reset();
			enqueueFragment(frag);
		}
	}

	//-------------------------------------------------------------------
	private void enqueueFragment(AParsedElement element) {
		if (element == null) return;
		List<AParsedElement> current = List.of(element);
		for (ANSIInputStreamFilter filter : filters) {
			List<AParsedElement> nextList = new ArrayList<>();
			for (AParsedElement item : current) {
				if (filter.handles(item)) {
					List<AParsedElement> processed = filter.process(item);
					if (processed != null) {
						nextList.addAll(processed);
					}
				} else {
					nextList.add(item);
				}
			}
			current = nextList;
		}
		queue.addAll(current);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.FilteringANSIStream#hasFilter(org.prelle.ansi.ANSIInputStreamFilter)
	 */
	@Override
	public boolean hasFilter(ANSIInputStreamFilter filter) {
		return filters.contains(filter);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.FilteringANSIStream#addFilter(org.prelle.ansi.ANSIInputStreamFilter)
	 */
	@Override
	public boolean addFilter(ANSIInputStreamFilter filter) {
		if (filter != null && !filters.contains(filter)) {
			filters.add(filter);
		}
		return true;
	}

	//-------------------------------------------------------------------
	@Override
	public boolean addFilter(int index, ANSIInputStreamFilter filter) {
		filters.add(index, filter);
		return true;
	}

	//-------------------------------------------------------------------
	@Override
	public boolean removeFilter(ANSIInputStreamFilter filter) {
		return filters.remove(filter);
	}

	//-------------------------------------------------------------------
	/**
	 * Checks if printable characters are collected into a single PrintableFragment.
	 *
	 * @return true if printable characters are collected; false otherwise
	 */
	public boolean isCollectPrintable() {
		return collectPrintable;
	}

	//-------------------------------------------------------------------
	/**
	 * Sets whether printable characters should be collected into a single PrintableFragment.
	 *
	 * @param collectPrintable true to collect printable characters; false otherwise
	 */
	public void setCollectPrintable(boolean collectPrintable) {
		this.collectPrintable = collectPrintable;
	}

	//-------------------------------------------------------------------
	/**
	 * Sets the character encoding used by the underlying VT500 parser.
	 *
	 * @param encoding The Charset encoding to use
	 */
	public void setEncoding(Charset encoding) {
		parser.setEncoding(encoding);
	}

	//-------------------------------------------------------------------
	/**
	 * Returns the character encoding used by the underlying VT500 parser.
	 *
	 * @return The current Charset encoding
	 */
	public Charset getEncoding() {
		return parser.getEncoding();
	}

	//-------------------------------------------------------------------
	/**
	 * Blocking read for the next parsed fragment.
	 *
	 * @return Next AParsedElement fragment, or null on EOF.
	 * @throws IOException If an I/O error occurs while reading from the underlying stream
	 */
	public AParsedElement readFragment() throws IOException {
		while (queue.isEmpty()) {
			int b = in.read();
			if (b == -1) {
				releaseCollectPrintable();
				if (!queue.isEmpty()) {
					AParsedElement frag = queue.remove(0);
					// Eventually log
					if (loggingListener!=null) {
						try {
							loggingListener.accept(frag.getType().name(), frag.toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return frag;
				}
				return null;
			}
			b = (b < 0) ? (256 + b) : b;
			parser.parse(b);
			if (!queue.isEmpty()) {
				queue.get(0).readExpectedLateBytes(in);
			}
		}
		AParsedElement frag = queue.remove(0);
		// Eventually log
		if (loggingListener!=null) {
			try {
				loggingListener.accept(frag.getType().name(), frag.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return frag;
	}

	//-------------------------------------------------------------------
	private AParsedElement peekOrReadAvailableFragment() throws IOException {
		if (!queue.isEmpty()) {
			return queue.remove(0);
		}
		while (queue.isEmpty() && in.available() > 0) {
			int b = in.read();
			if (b == -1) break;
			b = (b < 0) ? (256 + b) : b;
			parser.parse(b);
			if (!queue.isEmpty()) {
				queue.get(0).readExpectedLateBytes(in);
			}
		}
		if (queue.isEmpty()) {
			releaseCollectPrintable();
		}
		if (!queue.isEmpty()) {
			return queue.remove(0);
		}
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * Reads the next byte of data from the stream.
	 *
	 * @return The next byte of data (0-255), or -1 if the end of the stream is reached
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public int read() throws IOException {
		if (pendingRaw != null && pendingOffset < pendingRaw.length) {
			int b = pendingRaw[pendingOffset++] & 0xFF;
			if (pendingOffset == pendingRaw.length) {
				pendingRaw = null;
				pendingOffset = 0;
			}
			return b;
		}

		AParsedElement frag = readFragment();
		if (frag == null) {
			return -1;
		}

		byte[] raw = frag.getRaw();
		if (raw == null || raw.length == 0) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			frag.encode(baos, true);
			raw = baos.toByteArray();
		}

		if (raw.length == 1) {
			return raw[0] & 0xFF;
		}

		pendingRaw = raw;
		pendingOffset = 1;
		return raw[0] & 0xFF;
	}

	//-------------------------------------------------------------------
	/**
	 * Reads up to len bytes of data from the stream into an array of bytes.
	 *
	 * @param b Destination byte buffer
	 * @param off Start offset in array b
	 * @param len Maximum number of bytes to read
	 * @return Total number of bytes read into buffer, or -1 if end of stream is reached
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		Objects.checkFromIndexSize(off, len, b.length);
		if (len == 0) {
			return 0;
		}

		int bytesRead = 0;

		// 1. Drain any pending bytes from a previous byte-wise read() call
		if (pendingRaw != null && pendingOffset < pendingRaw.length) {
			int avail = pendingRaw.length - pendingOffset;
			int toCopy = Math.min(len, avail);
			System.arraycopy(pendingRaw, pendingOffset, b, off, toCopy);
			pendingOffset += toCopy;
			if (pendingOffset == pendingRaw.length) {
				pendingRaw = null;
				pendingOffset = 0;
			}
			return toCopy;
		}

		// 2. Read first fragment blocking
		AParsedElement frag = readFragment();
		if (frag == null) {
			return -1;
		}

		byte[] raw = frag.getRaw();
		if (raw == null || raw.length == 0) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			frag.encode(baos, true);
			raw = baos.toByteArray();
		}

		int toCopy = Math.min(len, raw.length);
		System.arraycopy(raw, 0, b, off, toCopy);
		bytesRead += toCopy;

		if (toCopy < raw.length) {
			// Buffer was smaller than single fragment raw bytes
			pendingRaw = raw;
			pendingOffset = toCopy;
			return bytesRead;
		}

		// Non-printable or collectPrintable is false -> return after 1 fragment
		if (!(frag instanceof PrintableFragment) || !collectPrintable) {
			return bytesRead;
		}

		// Collect further printable fragments if data is available
		while (bytesRead < len && (queue.size() > 0 || in.available() > 0)) {
			if (queue.isEmpty() && in.available() <= 0) {
				break;
			}

			AParsedElement nextFrag = peekOrReadAvailableFragment();
			if (nextFrag == null) {
				break;
			}

			if (!(nextFrag instanceof PrintableFragment)) {
				queue.add(0, nextFrag);
				break;
			}

			byte[] nextRaw = nextFrag.getRaw();
			if (nextRaw == null || nextRaw.length == 0) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				nextFrag.encode(baos, true);
				nextRaw = baos.toByteArray();
			}

			if (bytesRead + nextRaw.length > len) {
				queue.add(0, nextFrag);
				break;
			}

			System.arraycopy(nextRaw, 0, b, off + bytesRead, nextRaw.length);
			bytesRead += nextRaw.length;
		}

		return bytesRead;
	}

	//-------------------------------------------------------------------
	/**
	 * Returns an estimate of the number of bytes that can be read without blocking.
	 *
	 * @return Number of available bytes
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public int available() throws IOException {
		if (pendingRaw != null) {
			return (pendingRaw.length - pendingOffset) + in.available();
		}
		return in.available();
	}

	//-------------------------------------------------------------------
	/**
	 * Closes this input stream and releases any system resources associated with it.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		in.close();
	}
}
