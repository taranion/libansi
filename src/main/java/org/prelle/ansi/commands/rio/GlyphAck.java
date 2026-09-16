package org.prelle.ansi.commands.rio;

/**
 * Shared {@code status=<int>[;reason=<code>]} parsing for the "r" and "c"
 * verb replies (spec sections 6.2/7.2), whose ack shape is identical.
 * Package-private -- an implementation detail of decoding, not public API.
 */
final class GlyphAck {

	final int status;
	/** null on success (status == 0). */
	final String reason;

	//-------------------------------------------------------------------
	private GlyphAck(int status, String reason) {
		this.status = status;
		this.reason = reason;
	}

	//-------------------------------------------------------------------
	/** @param fromIndex index into {@code parts} (a ';'-split reply body) to start scanning at, past cp/verb. */
	static GlyphAck parse(String[] parts, int fromIndex) {
		int status = -1;
		String reason = null;
		for (int i = fromIndex; i < parts.length; i++) {
			if (parts[i].startsWith("status=")) {
				status = Integer.parseInt(parts[i].substring(7));
			} else if (parts[i].startsWith("reason=")) {
				reason = parts[i].substring(7);
			}
		}
		return new GlyphAck(status, reason);
	}

}
