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

}
