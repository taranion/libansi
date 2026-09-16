package org.prelle.ansi.commands.rio;

import java.io.ByteArrayOutputStream;

import org.prelle.ansi.C1Code;
import org.prelle.ansi.StringMessageFragment;

/**
 * Glyph Protocol "c" (clear) verb -- removes one registration (given a
 * {@code cp}), or the entire session glossary (no {@code cp}, the
 * default).
 * <p>
 * Wire form: {@code ESC _ 25a1;c[;cp=<hex>] ESC \}. `cp`, when given,
 * MUST be inside a PUA range or the terminal rejects the request with
 * {@code reason=out_of_namespace}.
 *
 * @see <a href="https://github.com/raphamorim/rio/blob/main/specs/glyph-protocol.md">Glyph Protocol specification</a>
 */
public class GlyphClearFragment extends StringMessageFragment {

	/** Codepoint to clear; {@code null} (default) clears the whole glossary. */
	protected Integer codepoint;
	/** Set by {@link #decodingHook()}: true if decoded from an incoming reply. */
	protected boolean reply;
	/** 0 on success; a nonzero code (see {@link #reason}) on failure. -1 until decoded. */
	protected int status = -1;
	/** Error code, e.g. {@code out_of_namespace}. Null on success. */
	protected String reason;

	//-------------------------------------------------------------------
	/** Clears the entire session glossary. */
	public GlyphClearFragment() {
		super(C1Code.APC, null);
	}

	//-------------------------------------------------------------------
	/** Clears a single codepoint's registration. */
	public GlyphClearFragment(int codepoint) {
		super(C1Code.APC, null);
		this.codepoint = codepoint;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		data = (codepoint != null) ? "25a1;c;cp=" + Integer.toHexString(codepoint) : "25a1;c";
		super.encode(toFill, use7Bit);
	}

	//-------------------------------------------------------------------
	public Integer getCodepoint() {
		return codepoint;
	}

	//-------------------------------------------------------------------
	public GlyphClearFragment setCodepoint(Integer codepoint) {
		this.codepoint = codepoint;
		return this;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.StringMessageFragment#getPrefix()
	 */
	@Override
	public String getPrefix() {
		return "25a1;c;";
	}

	//-------------------------------------------------------------------
	/**
	 * Parses {@code 25a1;c;status=0} (success) or
	 * {@code 25a1;c;status=1;reason=out_of_namespace} (failure) -- spec
	 * section 7.2. Does not decode {@code cp} back out since a "clear whole
	 * glossary" reply doesn't echo one; {@link #codepoint} stays whatever it
	 * was on the (unrelated) instance this ran on.
	 * @see org.prelle.ansi.StringMessageFragment#decodingHook()
	 */
	@Override
	protected void decodingHook() {
		reply = true;
		GlyphAck ack = GlyphAck.parse(data.split(";"), 2);
		status = ack.status;
		reason = ack.reason;
	}

	//-------------------------------------------------------------------
	public boolean isReply() {
		return reply;
	}

	//-------------------------------------------------------------------
	public boolean isSuccess() {
		return status == 0;
	}

	//-------------------------------------------------------------------
	public int getStatus() {
		return status;
	}

	//-------------------------------------------------------------------
	public String getReason() {
		return reason;
	}

}
