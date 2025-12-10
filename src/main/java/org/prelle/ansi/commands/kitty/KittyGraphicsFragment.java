package org.prelle.ansi.commands.kitty;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.prelle.ansi.C1Code;
import org.prelle.ansi.StringMessageFragment;

/**
 *
 */
public class KittyGraphicsFragment extends StringMessageFragment {

	public final static Character ACION_TRANSMIT = 't';
	public final static Character ACION_TRANSMIT_AND_DISPLAY = 'T';
	public final static Character ACION_QUERY    = 'q';
	public final static Character ACION_PUT      = 'p';
	public final static Character ACION_DELETE   = 'd';
	public final static Character ACION_TRANSMIT_FRAME = 'f';
	public final static Character ACION_CONTROL_ANIM = 'a';
	public final static Character ACION_COMPOSE_ANIM = 'c';

	protected Map<Character,String> controlData = new HashMap<>();
	protected String payloadB64;

	//-------------------------------------------------------------------
	public KittyGraphicsFragment() {
		super(C1Code.APC, null);
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	public KittyGraphicsFragment set(char key, int value) {
		controlData.put(key, String.valueOf(value));
		return this;
	}

	//-------------------------------------------------------------------
	public KittyGraphicsFragment set(char key, char value) {
		controlData.put(key, String.valueOf(value));
		return this;
	}

	//-------------------------------------------------------------------
	@Override
	public String toString() {
		// Prepare line
		StringBuffer full = new StringBuffer("G");
		full.append( controlData.entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(Collectors.joining(",")) );
		full.append(';');
		if (payloadB64!=null) {
			full.append(payloadB64);
		}
		data = full.toString();
		return super.toString();

	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		// Prepare line
		StringBuffer full = new StringBuffer("G");
		full.append( controlData.entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(Collectors.joining(",")) );
		full.append(';');
		if (payloadB64!=null) {
			full.append(payloadB64);
		}
		data = full.toString();
		super.encode(toFill, use7Bit);

	}

	//-------------------------------------------------------------------
	public KittyGraphicsFragment setPayload(String value) {
		this.payloadB64 = value;
		return this;
	}

}
