package org.prelle.ansi.commands.rio;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.prelle.ansi.C1Code;
import org.prelle.ansi.StringMessageFragment;

/**
 * Glyph Protocol "q" (query) verb -- asks whether a codepoint is covered
 * by a system font, a prior {@code r} registration, both, or neither.
 * <p>
 * Wire form: {@code ESC _ 25a1;q;cp=<hex> ESC \}. `cp` may be any valid
 * Unicode scalar value, inside or outside the Private Use Area. The
 * reply's {@code status=} value (spec section 5.2) is one of: empty (no
 * coverage), {@code system}, {@code glossary}, or {@code system,glossary}.
 *
 * @see <a href="https://github.com/raphamorim/rio/blob/main/specs/glyph-protocol.md">Glyph Protocol specification</a>
 */
public class GlyphQueryFragment extends StringMessageFragment {

	/** Codepoint to query, e.g. 0xE000. Overwritten with the echoed value on decode. */
	protected int codepoint;
	/** Set by {@link #decodingHook()}: true if decoded from an incoming reply. */
	protected boolean reply;
	/** Coverage sources for {@link #codepoint}: empty, or some of {@code system}/{@code glossary}. */
	protected List<String> status = List.of();

	//-------------------------------------------------------------------
	public GlyphQueryFragment() {
		super(C1Code.APC, null);
	}

	//-------------------------------------------------------------------
	public GlyphQueryFragment(int codepoint) {
		super(C1Code.APC, null);
		this.codepoint = codepoint;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		data = "25a1;q;cp=" + Integer.toHexString(codepoint);
		super.encode(toFill, use7Bit);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.StringMessageFragment#getPrefix()
	 */
	@Override
	public String getPrefix() {
		return "25a1;q;";
	}

	//-------------------------------------------------------------------
	/**
	 * Parses {@code 25a1;q;cp=<hex>;status=<list>} (spec section 5.2).
	 * @see org.prelle.ansi.StringMessageFragment#decodingHook()
	 */
	@Override
	protected void decodingHook() {
		reply = true;
		for (String part : data.split(";")) {
			if (part.startsWith("cp=")) {
				codepoint = Integer.parseInt(part.substring(3), 16);
			} else if (part.startsWith("status=")) {
				String s = part.substring(7);
				status = s.isEmpty() ? List.of() : List.of(s.split(","));
			}
		}
	}

	//-------------------------------------------------------------------
	public int getCodepoint() {
		return codepoint;
	}

	//-------------------------------------------------------------------
	public GlyphQueryFragment setCodepoint(int codepoint) {
		this.codepoint = codepoint;
		return this;
	}

	//-------------------------------------------------------------------
	public boolean isReply() {
		return reply;
	}

	//-------------------------------------------------------------------
	public List<String> getStatus() {
		return status;
	}

}
