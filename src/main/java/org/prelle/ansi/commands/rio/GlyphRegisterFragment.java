package org.prelle.ansi.commands.rio;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.prelle.ansi.C1Code;
import org.prelle.ansi.StringMessageFragment;

/**
 * Glyph Protocol "r" (register) verb -- uploads a glyph outline for a
 * Private-Use-Area codepoint. Only parameters that differ from their
 * spec-defined default are put on the wire (spec section 6.1 lists each
 * default).
 * <p>
 * Not enforced here, since this class only builds the wire message (the
 * terminal is the one that validates and replies with a {@code reason=}
 * code on failure, spec section 6.2):
 * <ul>
 * <li>{@code cp} MUST fall inside one of the three PUA ranges (spec
 *     section 4) or the terminal rejects with
 *     {@code reason=out_of_namespace}.</li>
 * <li>The decoded payload is capped at 64&nbsp;KiB per outline (spec
 *     section 8.2, {@code reason=outline_too_large}/{@code
 *     payload_too_large}).</li>
 * </ul>
 *
 * @see <a href="https://github.com/raphamorim/rio/blob/main/specs/glyph-protocol.md">Glyph Protocol specification</a>
 */
public class GlyphRegisterFragment extends StringMessageFragment {

	/** Payload format, spec section 6.1/8. */
	public static enum Format {
		GLYF("glyf"), COLRV0("colrv0"), COLRV1("colrv1");
		private final String wire;
		private Format(String wire) { this.wire = wire; }
		public String wire() { return wire; }
	}

	/** Reply-level control, spec section 6.1 ({@code reply=0|1|2}). */
	public static enum ReplyMode {
		/** Neither success nor failure produces a reply. */
		SILENT(0),
		/** Both success and failure replies are sent (default). */
		ALL(1),
		/** Only failure replies are sent, success registrations are silent. */
		FAILURES_ONLY(2);
		private final int wire;
		private ReplyMode(int wire) { this.wire = wire; }
		public int wire() { return wire; }
	}

	/** Scale policy, spec section 8.5.3. */
	public static enum SizeMode {
		HEIGHT("height"), ADVANCE("advance"), CONTAIN("contain"), COVER("cover"), STRETCH("stretch");
		private final String wire;
		private SizeMode(String wire) { this.wire = wire; }
		public String wire() { return wire; }
	}

	/** Horizontal alignment, spec section 8.5.4. */
	public static enum HAlign {
		START("start"), CENTER("center"), END("end");
		private final String wire;
		private HAlign(String wire) { this.wire = wire; }
		public String wire() { return wire; }
	}

	/** Vertical alignment, spec section 8.5.4. */
	public static enum VAlign {
		START("start"), CENTER("center"), END("end"), BASELINE("baseline");
		private final String wire;
		private VAlign(String wire) { this.wire = wire; }
		public String wire() { return wire; }
	}

	/** Target codepoint. MUST be within one of the three PUA ranges (spec section 4). */
	protected int codepoint;
	protected Format format = Format.GLYF;
	protected ReplyMode replyMode = ReplyMode.ALL;
	/** Units per em -- the coordinate space the outline is authored in. */
	protected int unitsPerEm = 1000;
	/** Authored advance width, in upm units. Defaults to {@link #unitsPerEm}. */
	protected int advanceWidth = -1;
	/** Authored line height (descender-to-ascender), in upm units. Defaults to {@link #unitsPerEm}. */
	protected int lineHeight = -1;
	/** Render span in cells: 1 (default) or 2. Purely a render-time overflow, not the codepoint's wcwidth. */
	protected int renderWidth = 1;
	protected SizeMode size = SizeMode.HEIGHT;
	protected HAlign hAlign = HAlign.CENTER;
	protected VAlign vAlign = VAlign.CENTER;
	/** Insets from the render span edges, as fractions 0.0-1.0: top, right, bottom, left. */
	protected double padTop, padRight, padBottom, padLeft;
	/** Base64-encoded payload for {@link #format}. */
	protected String payloadB64;

	/** Set by {@link #decodingHook()}: true if decoded from an incoming reply. */
	protected boolean reply;
	/** 0 on success; a nonzero code (see {@link #reason}) on failure. -1 until decoded. */
	protected int status = -1;
	/** Error code, e.g. {@code out_of_namespace}, {@code outline_too_large}. Null on success. */
	protected String reason;

	//-------------------------------------------------------------------
	public GlyphRegisterFragment() {
		super(C1Code.APC, null);
	}

	//-------------------------------------------------------------------
	public GlyphRegisterFragment(int codepoint, String payloadB64) {
		super(C1Code.APC, null);
		this.codepoint = codepoint;
		this.payloadB64 = payloadB64;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.SequenceFragment#encode(java.io.ByteArrayOutputStream, boolean)
	 */
	@Override
	public void encode(ByteArrayOutputStream toFill, boolean use7Bit) {
		List<String> params = new ArrayList<>();
		params.add("cp=" + Integer.toHexString(codepoint));
		if (format != Format.GLYF) params.add("fmt=" + format.wire());
		if (replyMode != ReplyMode.ALL) params.add("reply=" + replyMode.wire());
		if (unitsPerEm != 1000) params.add("upm=" + unitsPerEm);
		if (advanceWidth >= 0 && advanceWidth != unitsPerEm) params.add("aw=" + advanceWidth);
		if (lineHeight >= 0 && lineHeight != unitsPerEm) params.add("lh=" + lineHeight);
		if (renderWidth != 1) params.add("width=" + renderWidth);
		if (size != SizeMode.HEIGHT) params.add("size=" + size.wire());
		if (hAlign != HAlign.CENTER || vAlign != VAlign.CENTER)
			params.add("align=" + hAlign.wire() + "," + vAlign.wire());
		if (padTop != 0 || padRight != 0 || padBottom != 0 || padLeft != 0)
			params.add("pad=" + frac(padTop) + "," + frac(padRight) + "," + frac(padBottom) + "," + frac(padLeft));

		data = "25a1;r;" + String.join(";", params) + ";" + (payloadB64 != null ? payloadB64 : "");
		super.encode(toFill, use7Bit);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.StringMessageFragment#getPrefix()
	 */
	@Override
	public String getPrefix() {
		return "25a1;r;";
	}

	//-------------------------------------------------------------------
	/**
	 * Parses {@code 25a1;r;cp=<hex>;status=0} (success) or
	 * {@code 25a1;r;cp=<hex>;status=<n>;reason=<code>} (failure) -- spec
	 * section 6.2. Only fires for {@code reply=1} (default) or
	 * {@code reply=2} (failures only); {@code reply=0} registrations produce
	 * no reply to decode at all.
	 * @see org.prelle.ansi.StringMessageFragment#decodingHook()
	 */
	@Override
	protected void decodingHook() {
		reply = true;
		String[] parts = data.split(";");
		if (parts.length > 2 && parts[2].startsWith("cp=")) {
			codepoint = Integer.parseInt(parts[2].substring(3), 16);
		}
		GlyphAck ack = GlyphAck.parse(parts, 3);
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

	//-------------------------------------------------------------------
	/** Locale-independent fraction formatting -- Double.toString() is always
	 * "."-decimal regardless of the platform default locale, unlike
	 * String.format("%f", ...), which would print a comma on this system. */
	private static String frac(double value) {
		return Double.toString(value);
	}

	//-------------------------------------------------------------------
	public int getCodepoint() {
		return codepoint;
	}

	public GlyphRegisterFragment setCodepoint(int codepoint) {
		this.codepoint = codepoint;
		return this;
	}

	public Format getFormat() {
		return format;
	}

	public GlyphRegisterFragment setFormat(Format format) {
		this.format = format;
		return this;
	}

	public ReplyMode getReplyMode() {
		return replyMode;
	}

	public GlyphRegisterFragment setReplyMode(ReplyMode replyMode) {
		this.replyMode = replyMode;
		return this;
	}

	public int getUnitsPerEm() {
		return unitsPerEm;
	}

	public GlyphRegisterFragment setUnitsPerEm(int unitsPerEm) {
		this.unitsPerEm = unitsPerEm;
		return this;
	}

	public int getAdvanceWidth() {
		return advanceWidth;
	}

	public GlyphRegisterFragment setAdvanceWidth(int advanceWidth) {
		this.advanceWidth = advanceWidth;
		return this;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	public GlyphRegisterFragment setLineHeight(int lineHeight) {
		this.lineHeight = lineHeight;
		return this;
	}

	public int getRenderWidth() {
		return renderWidth;
	}

	public GlyphRegisterFragment setRenderWidth(int renderWidth) {
		this.renderWidth = renderWidth;
		return this;
	}

	public SizeMode getSize() {
		return size;
	}

	public GlyphRegisterFragment setSize(SizeMode size) {
		this.size = size;
		return this;
	}

	public HAlign getHAlign() {
		return hAlign;
	}

	public VAlign getVAlign() {
		return vAlign;
	}

	public GlyphRegisterFragment setAlign(HAlign hAlign, VAlign vAlign) {
		this.hAlign = hAlign;
		this.vAlign = vAlign;
		return this;
	}

	public GlyphRegisterFragment setPad(double top, double right, double bottom, double left) {
		this.padTop = top;
		this.padRight = right;
		this.padBottom = bottom;
		this.padLeft = left;
		return this;
	}

	public String getPayloadB64() {
		return payloadB64;
	}

	public GlyphRegisterFragment setPayload(String payloadB64) {
		this.payloadB64 = payloadB64;
		return this;
	}

}
