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

	/** Set by {@link #decodingHook()}: true if decoded from an incoming reply. */
	protected boolean reply;
	/** Only the {@code ;OK} success case is confirmed against real behavior
	 * (see CapabilityDetectorMUDEvents' prior string check); anything else
	 * is treated as failure with the raw trailer kept in {@link #errorMessage},
	 * best-effort and not verified against a real terminal's error format. */
	protected boolean ok;
	protected String errorMessage;

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

	//-------------------------------------------------------------------
	public Map<Character,String> getControlData() {
		return controlData;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.StringMessageFragment#getPrefix()
	 */
	@Override
	public String getPrefix() {
		return "G";
	}

	//-------------------------------------------------------------------
	/**
	 * Parses {@code G<key>=<value>,...;OK} (confirmed) or
	 * {@code G<key>=<value>,...;<trailer>} (best-effort: {@link #ok} false,
	 * {@link #errorMessage} set to the raw trailer -- the exact error wire
	 * format isn't confirmed against a real terminal here).
	 * @see org.prelle.ansi.StringMessageFragment#decodingHook()
	 */
	@Override
	protected void decodingHook() {
		reply = true;
		String body = data.substring(1); // strip leading 'G'
		int semi = body.indexOf(';');
		String controlPart = (semi >= 0) ? body.substring(0, semi) : body;
		String rest = (semi >= 0) ? body.substring(semi + 1) : "";
		controlData.clear();
		for (String kv : controlPart.split(",")) {
			int eq = kv.indexOf('=');
			if (eq > 0) {
				controlData.put(kv.charAt(0), kv.substring(eq + 1));
			}
		}
		ok = "OK".equals(rest);
		if (!ok) {
			errorMessage = rest;
		}
	}

	//-------------------------------------------------------------------
	public boolean isReply() {
		return reply;
	}

	//-------------------------------------------------------------------
	public boolean isOk() {
		return ok;
	}

	//-------------------------------------------------------------------
	public String getErrorMessage() {
		return errorMessage;
	}

}
