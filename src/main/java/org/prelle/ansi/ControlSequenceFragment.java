package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class ControlSequenceFragment extends SequenceFragment {

	protected char firstParam;
	protected List<Integer> parameter = new ArrayList<>();

	//-------------------------------------------------------------------
	public ControlSequenceFragment() {
		super(C1Code.CSI);
	}

	//-------------------------------------------------------------------
	public ControlSequenceFragment(int finalChar, String name, Level level) {
		super(C1Code.CSI, finalChar, name, level);
	}

	//-------------------------------------------------------------------
	public ControlSequenceFragment(String intermediate, int finalChar, String name, Level level) {
		super(C1Code.CSI, intermediate, finalChar, name, level);
	}

	//-------------------------------------------------------------------
	public ControlSequenceFragment(String intermediate, char finalChar, String name, Integer...param) {
		super(C1Code.CSI, intermediate, (int)finalChar, name, Level.UNKNOWN);
		parameter = List.of(param);
	}

	//-------------------------------------------------------------------
	public static <T extends ControlSequenceFragment> T decode(Class<T> cls, String parameter) {
		try {
			Constructor<T> cons = cls.getDeclaredConstructor();
			cons.setAccessible(true);
			T instance = cons.newInstance();
			if (parameter!=null && !parameter.isEmpty()) {
				// The first parameter may need a different treatment, because it isn't a digit
				if (!Character.isDigit(parameter.charAt(0)) && parameter.charAt(0)!=';') {
					instance.firstParam = parameter.charAt(0);
					instance.parameter  = parseArguments(parameter,1);
				} else {
					instance.parameter  = parseArguments(parameter,0);
				}
			}
			instance.decodingHook();
			return instance;
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed instantiating "+cls,e);
		}
	}

	//-------------------------------------------------------------------
	/** Called from decode() to allow child classes special treatment
	 */
	protected void decodingHook() {
	}

	//-------------------------------------------------------------------
	static List<Integer> parseArguments(String parameter , int fromIndex) {
		List<Integer> ret = new ArrayList<>();
		String tmp = (fromIndex<1)?parameter:parameter.substring(fromIndex);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<tmp.length(); i++) {
			char c = tmp.charAt(i);
			if (c==';') {
				if (buf.isEmpty()) {
					// Parameter is default
					ret.add(0);
				} else {
					ret.add( Integer.parseInt(buf.toString()));
				}
				buf.delete(0, buf.length());
			} else
				buf.append(c);
		}
		if (!buf.isEmpty()) {
			ret.add( Integer.parseInt(buf.toString()));
		}
		// Assume that a missing parameter means default parameter
		if (ret.isEmpty())
			ret.add(0);

		return ret;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		super.encode(toFill, use7Bit);
		if (firstParam!=0) {
			toFill.write( (byte)firstParam);
		}
		String encoded = String.join(";", parameter.stream().map(x -> String.valueOf(x)).toList());
		toFill.writeBytes(encoded.getBytes(StandardCharsets.US_ASCII));
		if (intermediate!=null)
			toFill.writeBytes(intermediate.getBytes(StandardCharsets.US_ASCII));
		if (super.finalChar>=0)
			toFill.write( (byte)super.finalChar);
	}

	//-------------------------------------------------------------------
	public static byte[] encode(String intermediate, Character firstParam, String paramString, int command) {
		byte[] inter = (intermediate!=null)?intermediate.getBytes(StandardCharsets.ISO_8859_1):new byte[0];
		byte[] toCopy = paramString.getBytes(StandardCharsets.ISO_8859_1);
		int len = 2+inter.length+((firstParam!=null)?1:0)+toCopy.length+1;
		byte[] buf = new byte[len];
		int pos=0;
		buf[pos++]=(byte) C0Code.ESC.code();
		buf[pos++]=(byte) C1Code.CSI.getAsEscapeCode();
		System.arraycopy(inter, 0, buf, pos, inter.length);
		pos+=inter.length;
		if (firstParam!=null)
			buf[pos++]=(byte) (char) firstParam;
		System.arraycopy(toCopy, 0, buf, pos, toCopy.length);
		pos+=toCopy.length;
		buf[pos++]=(byte) command;
		return buf;
//		out.write(C1Code.CSI);
//		out.write(0x3F); // ?
//		ControlSequenceFragment.encodeParameter(out, List.of(modeNumber));
//		out.write( (state==ModeState.RESET)?0x6C:0x68 );
	}

	//-------------------------------------------------------------------
	protected static void encodeParameter(ANSIOutputStream out, List<Integer> parameter) throws IOException {
		String encoded = String.join(";", parameter.stream().map(x -> String.valueOf(x)).toList());
		out.write(encoded.getBytes(StandardCharsets.ISO_8859_1));
	}

	//-------------------------------------------------------------------
	public String toString() {
		StringBuffer ret = new StringBuffer(commandName);
		ret.append("=");
		ret.append(getCode().name());
		ret.append(" ");
		if (intermediate!=null)
			ret.append(intermediate);
		if (firstParam!=0)
			ret.append( (char)firstParam);
		ret.append( String.join(";", parameter.stream().map(x -> String.valueOf(x)).toList()));
		ret.append( (char)finalChar);
		return ret.toString();
	}

	//-------------------------------------------------------------------
	public List<Integer> getArguments() {
		return parameter;
	}

}
