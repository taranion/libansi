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
 *
 */
public class ANSIOutputStream extends FilterOutputStream {

	private final static Logger logger = System.getLogger(ANSIOutputStream.class.getPackageName());

	private boolean sendAs7Bit = true;
	/** Optional. Will receive a fragment mnemonic and a data string */
	private BiConsumer<String,String> loggingListener;

	private boolean utf8Mode = true;

	//-------------------------------------------------------------------
	public ANSIOutputStream(OutputStream out) {
		super(out);
	}

	//-------------------------------------------------------------------
	public String toString() {
		return "ANSIOutput --> "+out;
	}

	//-------------------------------------------------------------------
	/**
	 * @param loggingListener the loggingListener to set
	 */
	public void setLoggingListener(BiConsumer<String, String> loggingListener) {
		this.loggingListener = loggingListener;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.io.FilterOutputStream#write(int)
	 */
	public void write(int value) throws IOException {
//		System.err.println("ANSIOut.write "+value+"/"+Integer.toHexString(value));
		if (sendAs7Bit && value>=0x80 && value<0xA0) {
			out.write(0x1B); // ESC
			out.write(value-64); // C1 as C0
		} else {
			out.write(value);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.io.FilterOutputStream#write(byte[])
	 */
	public void write(byte[] values) throws IOException {
		out.write(values);
//		if (sendAs7Bit) {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream(values.length);
//
//			for (int i=0; i<values.length; i++) {
//				byte b = values[i];
//				int value = (b<0)?(256+b):b;
//				if (value>=0x80 && value<0xA0) {
//					baos.write(0x1B);
//					baos.write(value-64);
//					System.err.println("ANSIOut.write replaced "+Integer.toHexString(value)+" ("+((char)value)+" with "+Integer.toHexString(value-64)+" ("+((char)(value-64)));
//				} else
//					baos.write(value);
//			}
//			out.write(baos.toByteArray());
//			System.err.println("ANSIOut.write "+Arrays.toString(baos.toByteArray()));
//			baos.close();
//		} else {
//			out.write(values);
//		}
	}

	//-------------------------------------------------------------------
	public void write(byte[] values, String name) throws IOException {
		if (loggingListener!=null)
			loggingListener.accept(name, "");
		System.err.println("ANSIOut.write "+Arrays.toString(values));
		this.write(values);
	}

	//-------------------------------------------------------------------
	public void write(String value) throws IOException {
		if (utf8Mode) {
			this.write(value.getBytes(StandardCharsets.UTF_8));
		} else {
			this.write(value.getBytes(StandardCharsets.ISO_8859_1));
		}
	}

	//-------------------------------------------------------------------
	public void write(C1Code code) throws IOException {
//		if (sendAs7Bit) {
			this.write(C0Code.ESC.code);
			this.write(code.code-64);
//		} else {
//			this.write(code.code);
//		}
		if (loggingListener!=null)
			loggingListener.accept(code.name(), "");
	}

	//-------------------------------------------------------------------
	public void write(C0Code code) throws IOException {
		this.write(code.code());
		if (loggingListener!=null)
			loggingListener.accept(code.name(), "");
	}

	//-------------------------------------------------------------------
	public void writeCSI(int n, int...param) throws IOException {
		write(C1Code.CSI);
//		StringBuffer buf = new StringBuffer(((char)0x9B)+"");
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
	public void setTextColor(int color) throws IOException {
		write(new SelectGraphicRendition(38,5,color));
//		writeSGR(38,5,color);
	}

	//-------------------------------------------------------------------
	public void reset() {
		try {
			writeCSI( (int)'m',0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
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
	public void writeCSI(ControlSequenceFragment csi) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		csi.encode(baos, sendAs7Bit);
		super.write(baos.toByteArray());

		logger.log(Level.DEBUG, HexFormat.ofDelimiter(" ").formatHex(baos.toByteArray()));
		baos.close();
	}

	//-------------------------------------------------------------------
	public void writeDCS(DeviceControlFragment dcs) throws IOException {
		logger.log(Level.DEBUG, "writeDCS "+dcs);
//		if (loggingListener==null)
//			loggingListener = (type,text) -> {if (!"PRINT".equals(type)) logger.log(Level.INFO, "MUD --> {0} = {1}", type,text);};
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		dcs.encode(baos, sendAs7Bit);
		super.write(baos.toByteArray());

		logger.log(Level.DEBUG, HexFormat.ofDelimiter(" ").formatHex(baos.toByteArray()));
		baos.close();
	}

	//-------------------------------------------------------------------
	public void writeESC(EscapeSequenceFragment esc) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		esc.encode(baos, sendAs7Bit);
		super.write(baos.toByteArray());

		logger.log(Level.DEBUG, HexFormat.ofDelimiter(" ").formatHex(baos.toByteArray()));
		baos.close();
	}

	//-------------------------------------------------------------------
	public void writeString(StringMessageFragment value) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		value.encode(baos, sendAs7Bit);
//		if (sendAs7Bit) {
//			baos.write(C0Code.ESC.code);
//			baos.write((byte)0x5c);
//		} else {
//			baos.write((byte) C1Code.ST.code);
//		}
		super.write(baos.toByteArray());
		baos.close();
//		System.err.println(Arrays.toString(baos.toByteArray()));
	}

	//-------------------------------------------------------------------
	/**
	 * @return the utf8Mode
	 */
	public boolean isUtf8Mode() {
		return utf8Mode;
	}

	//-------------------------------------------------------------------
	/**
	 * @param utf8Mode the utf8Mode to set
	 */
	public void setUtf8Mode(boolean utf8Mode) {
		this.utf8Mode = utf8Mode;
	}

}
