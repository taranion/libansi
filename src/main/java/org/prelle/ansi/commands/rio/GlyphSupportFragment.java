package org.prelle.ansi.commands.rio;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.prelle.ansi.C1Code;
import org.prelle.ansi.StringMessageFragment;

/**
 * Glyph Protocol "s" (support) verb -- takes no parameters. Doubles as a
 * protocol-detection ping: any reply at all confirms the terminal
 * implements Glyph Protocol, a timeout means it doesn't (APC sequences
 * are required to be silently ignored by terminals that don't recognize
 * them).
 * <p>
 * Wire form: {@code ESC _ 25a1;s ESC \}. The reply carries the terminal's
 * supported payload formats as {@code 25a1;s;fmt=glyf,colrv0,colrv1} --
 * parsing that reply is not this class's job (see
 * {@code CapabilityDetectorMUDEvents} in GraphicMUD for the receiving
 * side, since incoming APC messages arrive as plain
 * {@link StringMessageFragment}, not re-decoded into typed subclasses).
 *
 * @see <a href="https://github.com/raphamorim/rio/blob/main/specs/glyph-protocol.md">Glyph Protocol specification</a>
 */
public class GlyphSupportFragment extends StringMessageFragment {

	/** Set by {@link #decodingHook()}: true if this instance was decoded from
	 * an incoming reply rather than freshly built to send as a request. */
	protected boolean reply;
	/** Payload formats the terminal advertises, e.g. {@code [glyf, colrv0, colrv1]}. */
	protected List<String> formats = List.of();

	//-------------------------------------------------------------------
	public GlyphSupportFragment() {
		super(C1Code.APC, null);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		data = "25a1;s";
		super.encode(toFill, use7Bit);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.StringMessageFragment#getPrefix()
	 */
	@Override
	public String getPrefix() {
		return "25a1;s;";
	}

	//-------------------------------------------------------------------
	/**
	 * Parses {@code 25a1;s;fmt=glyf,colrv0,colrv1} -- verified against a
	 * real Rio terminal (2026-09-16).
	 * @see org.prelle.ansi.StringMessageFragment#decodingHook()
	 */
	@Override
	protected void decodingHook() {
		reply = true;
		String[] parts = data.split(";", 3);
		if (parts.length == 3 && parts[2].startsWith("fmt=")) {
			String fmt = parts[2].substring(4);
			formats = fmt.isEmpty() ? List.of() : List.of(fmt.split(","));
		}
	}

	//-------------------------------------------------------------------
	public boolean isReply() {
		return reply;
	}

	//-------------------------------------------------------------------
	public List<String> getFormats() {
		return formats;
	}

}
