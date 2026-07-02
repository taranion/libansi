package org.prelle.ansi;

import java.util.List;

/**
 * 
 */
public class LinefeedToCRLFFilter implements ANSIInputStreamFilter {

	//-------------------------------------------------------------------
	/**
	 */
	public LinefeedToCRLFFilter() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.ANSIInputStreamFilter#handles(org.prelle.ansi.AParsedElement)
	 */
	@Override
	public boolean handles(AParsedElement event) {
		return (event instanceof C0Fragment co && co.getCode()==C0Code.LF);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.ansi.ANSIInputStreamFilter#process(org.prelle.ansi.AParsedElement)
	 */
	@Override
	public List<AParsedElement> process(AParsedElement event) {
		if (event instanceof C0Fragment co && co.getCode()==C0Code.LF) {
			return List.of(new PrintableFragment("\r\n").setRaw(new byte[] {0x0D, 0x0A}));
			//return List.of(new C0Fragment(C0Code.CR), new C0Fragment(C0Code.LF));
		}
		return List.of(event);
	}

}
