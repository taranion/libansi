package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.function.BiConsumer;

import org.prelle.ansi.commands.SelectGraphicRendition;

/**
 * FilterOutputStream implementation for writing ANSI escape sequences, control fragments, and encoded text.
 */
public class ANSIOutputStream extends OutputStream {

	private final static Logger logger = System.getLogger(ANSIOutputStream.class.getPackageName());

	private boolean sendAs7Bit = true;
	/** Optional. Will receive a fragment mnemonic and a data string */
	private BiConsumer<String,String> loggingListener;

	private boolean utf8Mode = true;
	
	private OutputStream out;

	//-------------------------------------------------------------------
	/**
	 * Creates a new ANSIOutputStream wrapping the specified underlying output stream.
	 *
	 * @param out The underlying OutputStream
	 */
	public ANSIOutputStream(OutputStream out) {
		//super(out);
		this.out = out;
	}

	//-------------------------------------------------------------------
	@Override
	public String toString() {
		return "ANSIOutput --> "+out;
	}

	//-------------------------------------------------------------------
	/**
	 * Sets the optional logging listener for written fragments.
	 *
	 * @param loggingListener The BiConsumer to receive fragment type names and values
	 */
	public void setLoggingListener(BiConsumer<String, String> loggingListener) {
		this.loggingListener = loggingListener;
	}

	//-------------------------------------------------------------------
	/**
	 * Writes a single byte to the output stream.
	 *
	 * @param value The byte value to write
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public void write(int value) throws IOException {
		if (sendAs7Bit && value>=0x80 && value<0xA0) {
			out.write(0x1B); // ESC
			out.write(value-64); // C1 as C0
		} else {
			out.write(value);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * Writes an array of bytes to the output stream.
	 *
	 * @param values The byte array to write
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public void write(byte[] values) throws IOException {
		out.write(values);
	}

	//-------------------------------------------------------------------
	/**
	 * Writes an array of bytes with a descriptive name for logging.
	 *
	 * @param values The byte array to write
	 * @param name The descriptive name of the payload
	 * @throws IOException If an I/O error occurs
	 */
	public void write(byte[] values, String name) throws IOException {
		if (loggingListener!=null)
			loggingListener.accept(name, "");
		System.err.println("ANSIOut.write "+Arrays.toString(values));
		this.write(values);
	}

	//-------------------------------------------------------------------
	/**
	 * Writes a text string encoded according to current utf8Mode.
	 *
	 * @param value The string to write
	 * @throws IOException If an I/O error occurs
	 */
	public void write(String value) throws IOException {
		if (utf8Mode) {
			this.write(value.getBytes(StandardCharsets.UTF_8));
		} else {
			this.write(value.getBytes(StandardCharsets.ISO_8859_1));
		}
	}

	//-------------------------------------------------------------------
	/**
	 * Writes a C1 control code sequence.
	 *
	 * @param code The C1Code to write
	 * @throws IOException If an I/O error occurs
	 */
	public void write(C1Code code) throws IOException {
		this.write(C0Code.ESC.code);
		this.write(code.code-64);
		if (loggingListener!=null)
			loggingListener.accept(code.name(), "");
	}

	//-------------------------------------------------------------------
	/**
	 * Writes a C0 control code.
	 *
	 * @param code The C0Code to write
	 * @throws IOException If an I/O error occurs
	 */
	public void write(C0Code code) throws IOException {
		this.write(code.code());
		if (loggingListener!=null)
			loggingListener.accept(code.name(), "");
	}

	//-------------------------------------------------------------------
	/**
	 * Writes a CSI sequence with numeric parameters and final character code.
	 *
	 * @param n Final character code
	 * @param param Parameter numbers
	 * @throws IOException If an I/O error occurs
	 */
	public void writeCSI(int n, int...param) throws IOException {
		write(C1Code.CSI);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<param.length; i++) {
			buf.append(param[i]);
			if ( (i+1)<param.length)
				buf.append(";");
		}
		buf.append((char)n);
		write(buf.toString());
		logger.log(Level.DEBUG,"writeCSI: "+buf);
		if (loggingListener!=null)
			loggingListener.accept("CSI", Arrays.toString(param)+" "+(char)n);
	}

	//-------------------------------------------------------------------
	/**
	 * Sets the foreground text color using SGR color code.
	 *
	 * @param color 256-color palette index
	 * @throws IOException If an I/O error occurs
	 */
	public void setTextColor(int color) throws IOException {
		write(new SelectGraphicRendition(38,5,color));
	}

	//-------------------------------------------------------------------
	/**
	 * Resets graphic rendition attributes to default (SGR 0).
	 */
	public void reset() {
		try {
			writeCSI( (int)'m',0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	/**
	 * Writes a parsed AParsedElement fragment to the stream.
	 *
	 * @param toWrite The fragment to write
	 * @throws IOException If an I/O error occurs
	 */
	public void write(AParsedElement toWrite) throws IOException {
		switch (toWrite) {
		case ControlSequenceFragment csi -> writeCSI(csi);
		case EscapeSequenceFragment esc -> writeESC(esc);
		case StringMessageFragment mess -> writeString(mess);
		case DeviceControlFragment csi -> writeDCS(csi);
		case C0Fragment c0 -> out.write(c0.getCode().code);
		case C1Fragment c1 -> writeC1(c1);
		case PrintableFragment text -> out.write(text.getData());
		default -> logger.log(Level.ERROR, "No handling for {0} fragments", toWrite.type);
		}
		if (loggingListener!=null)
			loggingListener.accept(toWrite.getName(), toWrite.toString());
	}

	//-------------------------------------------------------------------
	private void writeC1(C1Fragment c1) throws IOException {
		if (sendAs7Bit) {
			this.write(C0Code.ESC.code);
			this.write(c1.code.code - 0x40);
		} else {
			this.write(c1.code.code);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * Writes a ControlSequenceFragment (CSI) to the stream.
	 *
	 * @param csi The control sequence fragment to encode and write
	 * @throws IOException If an I/O error occurs
	 */
	public void writeCSI(ControlSequenceFragment csi) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		csi.encode(baos, sendAs7Bit);
		super.write(baos.toByteArray());

		logger.log(Level.DEBUG, HexFormat.ofDelimiter(" ").formatHex(baos.toByteArray()));
		baos.close();
	}

	//-------------------------------------------------------------------
	/**
	 * Writes a DeviceControlFragment (DCS) to the stream.
	 *
	 * @param dcs The device control fragment to encode and write
	 * @throws IOException If an I/O error occurs
	 */
	public void writeDCS(DeviceControlFragment dcs) throws IOException {
		logger.log(Level.DEBUG, "writeDCS "+dcs);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		dcs.encode(baos, sendAs7Bit);
		super.write(baos.toByteArray());

		logger.log(Level.DEBUG, HexFormat.ofDelimiter(" ").formatHex(baos.toByteArray()));
		baos.close();
	}

	//-------------------------------------------------------------------
	/**
	 * Writes an EscapeSequenceFragment (ESC) to the stream.
	 *
	 * @param esc The escape sequence fragment to encode and write
	 * @throws IOException If an I/O error occurs
	 */
	public void writeESC(EscapeSequenceFragment esc) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		esc.encode(baos, sendAs7Bit);
		super.write(baos.toByteArray());

		logger.log(Level.DEBUG, HexFormat.ofDelimiter(" ").formatHex(baos.toByteArray()));
		baos.close();
	}

	//-------------------------------------------------------------------
	/**
	 * Writes a StringMessageFragment (OSC / SOS / PM / APC) to the stream.
	 *
	 * @param value The string message fragment to encode and write
	 * @throws IOException If an I/O error occurs
	 */
	public void writeString(StringMessageFragment value) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		value.encode(baos, sendAs7Bit);
		super.write(baos.toByteArray());
		baos.close();
	}

	//-------------------------------------------------------------------
	/**
	 * Checks if UTF-8 encoding mode is enabled.
	 *
	 * @return true if UTF-8 mode is active; false if ISO-8859-1 mode is active
	 */
	public boolean isUtf8Mode() {
		return utf8Mode;
	}

	//-------------------------------------------------------------------
	/**
	 * Sets whether UTF-8 encoding mode is active.
	 *
	 * @param utf8Mode true to enable UTF-8 mode; false for ISO-8859-1 mode
	 */
	public void setUtf8Mode(boolean utf8Mode) {
		this.utf8Mode = utf8Mode;
	}

}
