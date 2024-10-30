package org.prelle.ansi;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DeviceControlFragment extends SequenceFragment {

	protected List<Integer> parameter = new ArrayList<>();
	protected String data;

	//-------------------------------------------------------------------
	public DeviceControlFragment(String intermediate, List<Integer> params, String data) {
		super(C1Code.DCS);
		super.type = Type.COMMAND;
		this.intermediate = intermediate;
		this.data = data;
		this.parameter = params;
	}

	//-------------------------------------------------------------------
	public DeviceControlFragment(String intermediate, int finalChar, List<Integer> params, String data) {
		super(C1Code.DCS);
		super.type = Type.COMMAND;
		this.finalChar = finalChar;
		this.intermediate = intermediate;
		this.data = data;
		this.parameter = params;
	}

	//-------------------------------------------------------------------
	public DeviceControlFragment(int finalChar, String cmdName, Level lvl) {
		super(C1Code.DCS);
		super.type = Type.COMMAND;
		this.finalChar = finalChar;
		this.level     = lvl;
		this.commandName = cmdName;
		this.parameter = new ArrayList<>();
	}

//	//-------------------------------------------------------------------
//	/**
//	 * @see java.lang.Object#toString()
//	 */
//	public String toString() {
//		return "DCS(param="+parameter+",data="+data+" )";
////		return "TEXT("+getText(Charset.defaultCharset())+")";
//	}

	//-------------------------------------------------------------------
	public String toString() {
		StringBuffer ret = new StringBuffer("DCS(");
		ret.append("p="+parameter+" ");
		if (intermediate!=null)
			ret.append(intermediate);
		ret.append( (char)finalChar);
		ret.append(" data="+data+")");
		return ret.toString();
	}

	//-------------------------------------------------------------------
	public static DeviceControlFragment decode(String intermediate, int finalChar, String parameter, String data) {
		return new DeviceControlFragment(intermediate, finalChar, ControlSequenceFragment.parseArguments(parameter,0), data);
	}

	public String getText(Charset charset) {
		return data;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		super.encode(toFill, use7Bit);
		if (parameter!=null) {
			String encoded = String.join(";", parameter.stream().map(x -> String.valueOf(x)).toList());
			toFill.writeBytes(encoded.getBytes(StandardCharsets.US_ASCII));
		}
		if (intermediate!=null) {
			toFill.writeBytes(intermediate.getBytes(StandardCharsets.US_ASCII));
		}
		if (finalChar!=0)
			toFill.write( (byte)super.finalChar);
		toFill.writeBytes(data.getBytes(StandardCharsets.US_ASCII));
		toFill.write(C0Code.ESC.code());
		toFill.write(C1Code.ST.code()-0x40);
	}

}
