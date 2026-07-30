package org.prelle.ansi.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;
import org.prelle.ansi.ANSIInputStreamFilter;
import org.prelle.ansi.AParsedElement;
import org.prelle.ansi.PassthroughANSIInputStream;
import org.prelle.ansi.commands.SelectGraphicRendition;

/**
 * This class contains unit tests for the PassthroughANSIInputStream.
 */
public class PassThruTest {
	
	private static class CSIFilter implements ANSIInputStreamFilter {
		
		private Consumer<AParsedElement> listener;
		public CSIFilter(Consumer<AParsedElement> listener) {
			this.listener = listener;
		}

		@Override
		public boolean handles(AParsedElement event) {
			return event instanceof SelectGraphicRendition;
		}

		@Override
		public List<AParsedElement> process(AParsedElement event) {
			listener.accept(event);
			return List.of();
		}
		
	}
	

	//-------------------------------------------------------------------
	@Test
	public void testUnfiltered() throws IOException {
		byte[] buf = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21, 13, 10};
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		PassthroughANSIInputStream in = new PassthroughANSIInputStream(bais);
		for (int i=0; i<buf.length; i++) {
			int b = in.read();
			if (b==-1) break;
			assertEquals(buf[i], b);
		}
		in.close();
	}

	//-------------------------------------------------------------------
	@Test
	public void testFiltered() throws IOException {
		byte[] buf = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x21, 13, 10, 0x1b, '[', '0', 'm',0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21};
		byte[] expect = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x21, 13, 10, 0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21};
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		List<AParsedElement> parsedElements = new ArrayList<>();
		PassthroughANSIInputStream in = new PassthroughANSIInputStream(bais);
		in.addFilter(new CSIFilter( pe -> parsedElements.add(pe)));
		int expectIndex = 0;
		for (int i=0; i<buf.length; i++) {
			int b = in.read();
			if (b==-1) break;
			System.err.println("Got "+b);
			assertEquals(expect[expectIndex++], b);
		}
		in.close();
		assertEquals(1, parsedElements.size());
	}

	//-------------------------------------------------------------------
	@Test
	public void testFilteredBlock() throws IOException {
		byte[] buf = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x21, 13, 10, 0x1b, '[', '0', 'm',0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21};
		byte[] expect = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x21, 13, 10, 0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21};
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		List<AParsedElement> parsedElements = new ArrayList<>();
		PassthroughANSIInputStream in = new PassthroughANSIInputStream(bais);
		in.addFilter(new CSIFilter( pe -> parsedElements.add(pe)));
		byte[] tmp  = new byte[100];
		
		assertEquals("Did not detect fragment boundary",9, in.read(tmp));
		in.close();
		assertEquals(1, parsedElements.size());
	}
	
}